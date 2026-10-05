package net.quarrymod.packets;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.quarrymod.QuarryMod;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import reborncore.common.network.BlockPosPayload;

public class QuarryManagerServerPacket {

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(QuarryMineAllPayload.ID, QuarryMineAllPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(QuarryMineAllPayload.ID, (payload, context) -> {
            if (!payload.isWithinDistance(context.player(), 64)) {
                return;
            }
            BlockEntity blockEntity = context.player().level().getBlockEntity(payload.pos());
            if (blockEntity instanceof QuarryBlockEntity quarryBlockEntity) {
                quarryBlockEntity.setMineAll(payload.mineAll());
            }
        });
    }

    public static QuarryMineAllPayload createPacketQuarryMineAll(QuarryBlockEntity machine, boolean mineAll) {
        return new QuarryMineAllPayload(machine.getBlockPos(), mineAll);
    }

    public record QuarryMineAllPayload(BlockPos pos, boolean mineAll) implements CustomPacketPayload, BlockPosPayload {

        public static final CustomPacketPayload.Type<QuarryMineAllPayload> ID = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(QuarryMod.MOD_ID, "quarry_mine_all"));
        public static final StreamCodec<RegistryFriendlyByteBuf, QuarryMineAllPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, QuarryMineAllPayload::pos,
            ByteBufCodecs.BOOL, QuarryMineAllPayload::mineAll,
            QuarryMineAllPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
