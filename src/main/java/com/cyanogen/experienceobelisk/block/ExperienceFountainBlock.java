package com.cyanogen.experienceobelisk.block;

import com.cyanogen.experienceobelisk.block_entities.ExperienceFountainEntity;
import com.cyanogen.experienceobelisk.registries.RegisterBlockEntities;
import com.cyanogen.experienceobelisk.registries.RegisterItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExperienceFountainBlock extends Block implements EntityBlock {

    public ExperienceFountainBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
                .strength(9f)
                .destroyTime(1.2f)
                .requiresCorrectToolForDrops()
                .explosionResistance(8f)
                .noOcclusion()
                .emissiveRendering((state, getter, pos) -> true)
        );
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        BlockEntity entity = level.getBlockEntity(pos);
        ItemStack heldItem = player.getItemInHand(hand);

        if(entity instanceof ExperienceFountainEntity fountain){

            if(heldItem.is(RegisterItems.ATTUNEMENT_STAFF.get()) && fountain.isBound){
                player.displayClientMessage(new TranslatableComponent("message.experienceobelisk.binding_wand.reveal_bound_pos",
                        new TextComponent(fountain.getBoundPos().toShortString()).withStyle(ChatFormatting.GREEN)), true);
            }
            else{
                fountain.cycleActivityState();
                TextComponent message = new TextComponent("Experience Fountain set to: ");

                switch (fountain.getActivityState()) {
                    case 0 -> message.append(new TextComponent("Slow").withStyle(ChatFormatting.RED));
                    case 1 -> message.append(new TextComponent("Moderate").withStyle(ChatFormatting.YELLOW));
                    case 2 -> message.append(new TextComponent("Fast").withStyle(ChatFormatting.GREEN));
                    case 3 -> message.append(new TextComponent("Hyperspeed").withStyle(ChatFormatting.LIGHT_PURPLE));
                }
                player.displayClientMessage(message, true);
                level.sendBlockUpdated(pos, state, state, 2);
            }

        }
        return InteractionResult.CONSUME;
    }


    VoxelShape shape = Shapes.create(new AABB(0 / 16D,0 / 16D,0 / 16D,16 / 16D,9 / 16D,16 / 16D));
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return shape;
    }

    public ItemStack stack;
    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        if (!pLevel.isClientSide) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof ExperienceFountainEntity entity && pPlayer.hasCorrectToolForDrops(pState)) {

                stack = new ItemStack(RegisterItems.EXPERIENCE_FOUNTAIN_ITEM.get(), 1);
                entity.saveToItem(stack);
            }
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        List<ItemStack> drops = new ArrayList<>();
        if(stack != null){
            drops.add(stack);
        }
        return drops;
    }


    //-----BLOCK ENTITY-----//

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pBlockEntityType == RegisterBlockEntities.EXPERIENCEFOUNTAIN_BE.get() ? ExperienceFountainEntity::tick : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return RegisterBlockEntities.EXPERIENCEFOUNTAIN_BE.get().create(pPos, pState);
    }

}