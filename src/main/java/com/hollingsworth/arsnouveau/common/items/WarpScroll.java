package com.hollingsworth.arsnouveau.common.items;

import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.hollingsworth.arsnouveau.common.advancement.ANCriteriaTriggers;
import com.hollingsworth.arsnouveau.common.items.data.WarpScrollData;
import com.hollingsworth.arsnouveau.common.network.Networking;
import com.hollingsworth.arsnouveau.common.network.PacketWarpPosition;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

public class WarpScroll extends ModItem {
    public WarpScroll() {
        super(ItemsRegistry.defaultItemProperties());
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.getCommandSenderWorld().isClientSide)
            return false;

        String displayName = stack.hasCustomHoverName() ? stack.getHoverName().getString() : "";
        WarpScrollData data = WarpScrollData.get(stack);
        if (data.isValid()
            && data.canTeleportWithDim(entity.getCommandSenderWorld().dimension().location().toString())
            && SourceUtil.hasSourceNearby(entity.blockPosition(), entity.getCommandSenderWorld(), 10, 9000)
            && BlockRegistry.PORTAL_BLOCK.get().trySpawnPortal(entity.getCommandSenderWorld(), entity.blockPosition(), data, displayName)
            && SourceUtil.takeSourceWithParticles(entity.blockPosition(), entity.getCommandSenderWorld(), 10, 9000) != null) {
            BlockPos pos = entity.blockPosition();
            ServerLevel world = (ServerLevel) entity.getCommandSenderWorld();
            world.sendParticles(ParticleTypes.PORTAL, pos.getX(), pos.getY() + 1.0, pos.getZ(),
                    10, (world.random.nextDouble() - 0.5D) * 2.0D, -world.random.nextDouble(), (world.random.nextDouble() - 0.5D) * 2.0D, 0.1f);
            world.playSound(null, pos, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.NEUTRAL, 1.0f, 1.0f);
            ANCriteriaTriggers.rewardNearbyPlayers(ANCriteriaTriggers.CREATE_PORTAL, world, pos, 4);
            stack.shrink(1);
            return true;
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        WarpScrollData data = WarpScrollData.get(stack);
        if (hand == InteractionHand.OFF_HAND)
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);

        if (!(world instanceof ServerLevel serverLevel))
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);

        if (data.isValid()) {
            if (!data.canTeleportWithDim(player.getCommandSenderWorld().dimension().location().toString())) {
                player.sendSystemMessage(Component.translatable("ars_nouveau.warp_scroll.wrong_dim"));
                return InteractionResultHolder.fail(stack);
            }
            BlockPos pos = data.pos();
            player.teleportToWithTicket(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            Vec2 rotation = data.rotation();
            player.setXRot(rotation.x);
            player.setYRot(rotation.y);
            Networking.sendToNearbyClient(world, player, new PacketWarpPosition(player.getId(),pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, rotation.x, rotation.y));
            serverLevel.sendParticles(ParticleTypes.PORTAL, pos.getX(), pos.getY() + 1.0, pos.getZ(),
                    10, (world.random.nextDouble() - 0.5D) * 2.0D, -world.random.nextDouble(), (world.random.nextDouble() - 0.5D) * 2.0D, 0.1f);
            world.playSound(null, pos, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.NEUTRAL, 1.0f, 1.0f);
            stack.shrink(1);
            return InteractionResultHolder.pass(stack);
        }
        if (player.isShiftKeyDown()) {
            ItemStack newWarpStack = new ItemStack(ItemsRegistry.WARP_SCROLL.get());
            WarpScrollData newData = new WarpScrollData(newWarpStack);
            newData.setData(player.blockPosition(), player.getCommandSenderWorld().dimension().location().toString(), player.getRotationVector());
            boolean didAdd;
            if (stack.getCount() == 1) {
                stack = newWarpStack;
                didAdd = true;
            } else {
                didAdd = player.addItem(newWarpStack);
                if (didAdd)
                    stack.shrink(1);
            }
            if (!didAdd) {
                player.sendSystemMessage(Component.translatable("ars_nouveau.warp_scroll.inv_full"));
                return InteractionResultHolder.fail(stack);
            } else {
                player.sendSystemMessage(Component.translatable("ars_nouveau.warp_scroll.recorded"));
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
