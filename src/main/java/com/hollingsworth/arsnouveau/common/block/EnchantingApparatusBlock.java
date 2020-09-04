package com.hollingsworth.arsnouveau.common.block;

import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EnchantingApparatusBlock extends ModBlock{
    public static final Property<Integer> stage = IntegerProperty.create("stage", 1, 47);

    public EnchantingApparatusBlock() {
        super(ModBlock.defaultProperties().notSolid(),"enchanting_apparatus");
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!world.isRemote) {
            EnchantingApparatusTile tile = (EnchantingApparatusTile) world.getTileEntity(pos);
            if(player.isSneaking()){
                tile.attemptCraft();
                return ActionResultType.SUCCESS;
            }
            if (tile.catalystItem != null && player.getHeldItem(handIn).isEmpty()) {
                ItemEntity item = new ItemEntity(world, player.getPosX(), player.getPosY(), player.getPosZ(), tile.catalystItem);
                world.addEntity(item);
                tile.catalystItem = null;
            } else if (!player.inventory.getCurrentItem().isEmpty()) {
                if(tile.catalystItem != null){
                    ItemEntity item = new ItemEntity(world, player.getPosX(), player.getPosY(), player.getPosZ(), tile.catalystItem);
                    world.addEntity(item);
                }
                tile.catalystItem = player.inventory.decrStackSize(player.inventory.currentItem, 1);;

            }
            world.notifyBlockUpdate(pos, state, state, 2);
        }
        return ActionResultType.SUCCESS;
    }


    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnchantingApparatusTile();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<net.minecraft.block.Block, BlockState> builder) { builder.add(stage); }
}
