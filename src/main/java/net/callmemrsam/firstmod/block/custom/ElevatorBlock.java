package net.callmemrsam.firstmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ElevatorBlock extends Block {

    private int MAX_RANGE = 25;
    public ElevatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.isSteppingCarefully()) {
                if (livingEntity.getXRot() > 20) {
                    teleportDown(level, pos, livingEntity);
                } else if (livingEntity.getXRot() < -20) {
                    teleportUp(level, pos, livingEntity);
                }
            }

        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND && player.isSteppingCarefully()) {
            if (!isValidElevator(level, pos)) {
                player.sendSystemMessage(Component.literal("This elevator is not usable. Break the two blocks on it."));
                return super.use(state, level, pos, player, hand, result);
            }
            int countUp = 0;
            int countDown = 0;
            for (int y = pos.getY() + 1; y <= pos.getY() + MAX_RANGE; y++) {
                if (isValidElevator(level, pos.atY(y))) {
                    countUp++;
                }
            }
            for (int y = pos.getY() - 1; y >= pos.getY() - MAX_RANGE; y--) {
                if (isValidElevator(level, pos.atY(y))) {
                    countDown++;
                }
            }
            player.sendSystemMessage(Component.literal(
                    "Elevator connected: " + countUp + " upward / " + countDown + " downward"
            ));
        }
        return super.use(state, level, pos, player, hand, result);
    }

    private void teleportDown(Level level, BlockPos pos, LivingEntity livingEntity) {
        for (int y = pos.getY() - 1; y >= pos.getY() - MAX_RANGE; y--) {
            if (isValidElevator(level, pos.atY(y))) {
                livingEntity.setXRot(0F);
                livingEntity.setPos(livingEntity.getX(), livingEntity.getY() - (pos.getY() - y), livingEntity.getZ());
                break;
            }
        }
    }

    private void teleportUp(Level level, BlockPos pos, LivingEntity livingEntity) {
        for (int y = pos.getY() + 1; y <= pos.getY() + MAX_RANGE; y++) {
            if (isValidElevator(level, pos.atY(y))) {
                livingEntity.setXRot(0F);
                livingEntity.setPos(livingEntity.getX(), livingEntity.getY() + (y - pos.getY()), livingEntity.getZ());
                break;
            }
        }
    }

    private boolean isValidElevator(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(this)
        && level.getBlockState(pos.atY(pos.getY() + 1)).is(Blocks.AIR)
        && level.getBlockState(pos.atY(pos.getY() + 2)).is(Blocks.AIR)) {
            return true;
        }
        return false;
    }
}
