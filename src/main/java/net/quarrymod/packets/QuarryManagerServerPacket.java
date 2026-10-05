package net.quarrymod.packets;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.quarrymod.QuarryMod;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import reborncore.common.network.BlockPosPayload;

public class QuarryManagerServerPacket {

    public static void init() {
        PayloadTypeRegistry.playC2S().register(QuarryMineAllPayload.ID, QuarryMineAllPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(QuarryMineAllPayload.ID, (payload, context) -> {
            if (!payload.isWithinDistance(context.player(), 64)) {
                return;
            }
            BlockEntity blockEntity = context.player().getWorld().getBlockEntity(payload.pos());
            if (blockEntity instanceof QuarryBlockEntity quarryBlockEntity) {
                quarryBlockEntity.setMineAll(payload.mineAll());
            }
        });
    }

    public static QuarryMineAllPayload createPacketQuarryMineAll(QuarryBlockEntity machine, boolean mineAll) {
        return new QuarryMineAllPayload(machine.getPos(), mineAll);
    }

    public record QuarryMineAllPayload(BlockPos pos, boolean mineAll) implements CustomPayload, BlockPosPayload {

        public static final CustomPayload.Id<QuarryMineAllPayload> ID = new CustomPayload.Id<>(
            Identifier.of(QuarryMod.MOD_ID, "quarry_mine_all"));
        public static final PacketCodec<RegistryByteBuf, QuarryMineAllPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, QuarryMineAllPayload::pos,
            PacketCodecs.BOOL, QuarryMineAllPayload::mineAll,
            QuarryMineAllPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
