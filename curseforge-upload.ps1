param(
    [string]$Version,
    [string[]]$Only,
    [ValidateSet('alpha', 'beta', 'release')]
    [string]$ReleaseType = 'release',
    [string]$ChangelogFile,
    [switch]$DryRun,
    [string]$ApiBase = 'https://minecraft.curseforge.com/api'
)

$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$ProjectId = 511254
$Root = $PSScriptRoot
$Relations = @(
    @{ slug = 'techreborn'; type = 'requiredDependency' },
    @{ slug = 'fabric-api'; type = 'requiredDependency' }
)

$uploaded = @()

function Stop-Upload([string]$Message) {
    Write-Host $Message -ForegroundColor Red
    if ($uploaded) { Write-Host "Already uploaded: $($uploaded -join ', '); rerun with -Only for the rest" }
    exit 1
}

function ConvertTo-VersionParts([string]$Name) {
    if ($Name -notmatch '^\d+(\.\d+)*$') { return $null }
    return , [int[]]($Name -split '\.')
}

function Compare-VersionParts([int[]]$A, [int[]]$B) {
    $length = [Math]::Max($A.Length, $B.Length)
    for ($i = 0; $i -lt $length; $i++) {
        $x = if ($i -lt $A.Length) { $A[$i] } else { 0 }
        $y = if ($i -lt $B.Length) { $B[$i] } else { 0 }
        if ($x -ne $y) { return [Math]::Sign($x - $y) }
    }
    return 0
}

function Test-LabelMatch([string]$Label, [string]$Name) {
    $parts = ConvertTo-VersionParts $Name
    if ($null -eq $parts) { return $false }
    if ($Label.Contains('-')) {
        $low, $high = $Label.Split('-', 2) | ForEach-Object { ConvertTo-VersionParts $_ }
        return (Compare-VersionParts $parts $low) -ge 0 -and (Compare-VersionParts $parts $high) -le 0
    }
    return $Name -eq $Label -or $Name.StartsWith("$Label.")
}

function Invoke-CurseForge([string]$Method, [string]$Path, [string]$Token, $Body, [string]$ContentType) {
    $request = @{
        Method  = $Method
        Uri     = "$ApiBase$Path"
        Headers = @{ 'X-Api-Token' = $Token }
    }
    if ($null -ne $Body) {
        $request.Body = $Body
        $request.ContentType = $ContentType
    }
    try {
        return Invoke-RestMethod @request
    } catch {
        $response = $_.Exception.Response
        $status = if ($response) { [int]$response.StatusCode } else { 'no response' }
        $message = $_.ErrorDetails.Message
        if (-not $message -and $response) {
            $reader = New-Object IO.StreamReader($response.GetResponseStream())
            $message = $reader.ReadToEnd()
            $reader.Dispose()
        }
        if (-not $message) { $message = $_.Exception.Message }
        Stop-Upload "$Method $Path failed: $status $message"
    }
}

function New-MultipartBody([string]$Boundary, [string]$MetadataJson, [IO.FileInfo]$Jar) {
    $utf8 = New-Object Text.UTF8Encoding($false)
    $stream = New-Object IO.MemoryStream
    $write = { param([string]$Text) $bytes = $utf8.GetBytes($Text); $stream.Write($bytes, 0, $bytes.Length) }
    & $write "--$Boundary`r`nContent-Disposition: form-data; name=`"metadata`"`r`nContent-Type: application/json`r`n`r`n"
    & $write "$MetadataJson`r`n"
    & $write "--$Boundary`r`nContent-Disposition: form-data; name=`"file`"; filename=`"$($Jar.Name)`"`r`nContent-Type: application/java-archive`r`n`r`n"
    $fileBytes = [IO.File]::ReadAllBytes($Jar.FullName)
    $stream.Write($fileBytes, 0, $fileBytes.Length)
    & $write "`r`n--$Boundary--`r`n"
    return , $stream.ToArray()
}

if (-not $Version) {
    $line = Select-String -Path (Join-Path $Root 'gradle.properties') -Pattern '^mod_version\s*=\s*(\S+)' | Select-Object -First 1
    $Version = $line.Matches[0].Groups[1].Value
}

$jars = @()
$dist = Join-Path $Root 'dist'
if (Test-Path $dist) {
    $folders = Get-ChildItem $dist -Directory | Sort-Object {
        ((ConvertTo-VersionParts $_.Name.Split('-')[0]) | ForEach-Object { '{0:D5}' -f $_ }) -join '.'
    }
    foreach ($folder in $folders) {
        if ($Only -and $Only -notcontains $folder.Name) { continue }
        foreach ($jar in Get-ChildItem $folder.FullName -Filter "quarry-reborn-*-$Version.jar") {
            $jars += [pscustomobject]@{ Label = $folder.Name; Jar = $jar }
        }
    }
}
if (-not $jars) { Stop-Upload "No jars for $Version in dist\" }

if ($ChangelogFile) {
    $changelog = [IO.File]::ReadAllText((Resolve-Path $ChangelogFile))
} else {
    $changelog = (& gh release view $Version --json body -q .body 2>$null) -join "`n"
}
if (-not $changelog.Trim()) { Stop-Upload 'No changelog: pass -ChangelogFile or create the GitHub release first' }

if ([Console]::IsInputRedirected) {
    Stop-Upload 'The hidden token prompt needs a PowerShell or cmd window; Git Bash cannot pass keystrokes to it'
}
$prompt = if ($DryRun) { 'CurseForge API token (empty to skip version lookup)' } else { 'CurseForge API token' }
$secure = Read-Host -Prompt $prompt -AsSecureString
$bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try {
    $token = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr).Trim()
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
}
if ($token -match '[\x00-\x1F\x7F]') {
    Stop-Upload 'Token contains control characters; paste it with right-click instead'
}
if (-not $token -and -not $DryRun) { Stop-Upload 'No token entered' }

$minecraft = $null
if ($token) {
    $types = Invoke-CurseForge 'GET' '/game/version-types' $token
    $versions = Invoke-CurseForge 'GET' '/game/versions' $token
    $minecraftTypes = @($types | Where-Object { $_.slug -like 'minecraft-*' } | ForEach-Object { $_.id })
    $loaderTypes = @($types | Where-Object { $_.slug -eq 'modloader' } | ForEach-Object { $_.id })
    $minecraft = @($versions | Where-Object { $minecraftTypes -contains $_.gameVersionTypeID })
    $fabric = $versions | Where-Object { $loaderTypes -contains $_.gameVersionTypeID -and $_.name -eq 'Fabric' } | Select-Object -First 1
    $environment = @($versions | Where-Object { $minecraftTypes -notcontains $_.gameVersionTypeID -and @('Client', 'Server') -contains $_.name })
    if (-not $fabric) { Stop-Upload 'Fabric loader version not found on CurseForge' }
    if ($environment.Count -lt 2) { Stop-Upload 'Client/Server environment versions not found on CurseForge' }
}

Write-Host "Changelog:`n$changelog`n"
foreach ($entry in $jars) {
    $display = "Quarry Reborn $($entry.Label) - $Version"
    $relative = $entry.Jar.FullName.Substring($Root.Length + 1)
    if ($null -eq $minecraft) {
        Write-Host "${display}: $relative (game versions resolved when a token is entered)"
        continue
    }
    $matched = @($minecraft | Where-Object { Test-LabelMatch $entry.Label $_.name } |
        Sort-Object { $parts = ConvertTo-VersionParts $_.name; ($parts | ForEach-Object { '{0:D5}' -f $_ }) -join '.' })
    if (-not $matched) { Stop-Upload "No CurseForge Minecraft version matches $($entry.Label)" }
    $names = ($matched | ForEach-Object { $_.name }) -join ', '
    Write-Host "${display}: $relative -> Minecraft $names + Fabric + Client, Server"
    if ($DryRun) { continue }

    $ids = [int[]](@($matched | ForEach-Object { $_.id }) + $fabric.id + @($environment | ForEach-Object { $_.id }))
    $metadata = @{
        changelog     = $changelog
        changelogType = 'markdown'
        displayName   = $display
        releaseType   = $ReleaseType
        gameVersions  = $ids
        relations     = @{ projects = $Relations }
    } | ConvertTo-Json -Depth 5 -Compress
    $boundary = [Guid]::NewGuid().ToString('N')
    $body = New-MultipartBody $boundary $metadata $entry.Jar
    $result = Invoke-CurseForge 'POST' "/projects/$ProjectId/upload-file" $token $body "multipart/form-data; boundary=$boundary"
    Write-Host "  uploaded file id $($result.id)" -ForegroundColor Green
    $uploaded += $entry.Label
}
if ($uploaded) { Write-Host "Uploaded: $($uploaded -join ', ')" }
