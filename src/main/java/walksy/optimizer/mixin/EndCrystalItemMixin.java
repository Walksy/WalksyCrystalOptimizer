package walksy.optimizer.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static walksy.optimizer.WalksyCrystalOptimizerMod.mc;

@Mixin({EndCrystalItem.class})
public class EndCrystalItemMixin {


    /**
     * @Author Walksy
     */


    /**
     * Stops crystals from decreasing too much
     * PS: does not work on singleplayer
     */
    @Inject(method = {"useOnBlock"}, at = {@At("HEAD")}, cancellable = true)
    private void modifyDecrementAmount(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack mainHandStack = mc.player.getMainHandStack();
        if (mainHandStack.isOf(Items.END_CRYSTAL)) {
            if (isLookingAt(Blocks.OBSIDIAN, generalLookPos().getBlockPos())
                    || isLookingAt(Blocks.BEDROCK, generalLookPos().getBlockPos())) {
                HitResult hitResult = mc.crosshairTarget;
                if (hitResult instanceof BlockHitResult) {
                    BlockHitResult hit = (BlockHitResult)hitResult;
                    BlockPos block = hit.getBlockPos();
                    if (canPlaceCrystalServer(block))
                        context.getStack().decrement(-1);
                }
            }
        }
    }


    private BlockState getBlockState(BlockPos pos) {
        return mc.world.getBlockState(pos);
    }
    private boolean isLookingAt(Block block, BlockPos pos) {
        return getBlockState(pos).getBlock() == block;
    }
    private BlockHitResult generalLookPos() {
        Vec3d camPos = mc.player.getEyePos();
        Vec3d clientLookVec = lookVec();
        return mc.world.raycast(new RaycastContext(camPos, camPos.add(clientLookVec.multiply(4.5)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));

    }
    private Vec3d lookVec() {
        float f = (float) Math.PI / 180;
        float pi = (float) Math.PI;
        float f1 = MathHelper.cos(-mc.player.getYaw() * f - pi);
        float f2 = MathHelper.sin(-mc.player.getYaw() * f - pi);
        float f3 = -MathHelper.cos(-mc.player.getPitch() * f);
        float f4 = MathHelper.sin(-mc.player.getPitch() * f);
        return new Vec3d(f2 * f3, f4, f1 * f3).normalize();
    }

    private boolean canPlaceCrystalServer(BlockPos block) {
        BlockState blockState = mc.world.getBlockState(block);
        if (!blockState.isOf(Blocks.OBSIDIAN) && !blockState.isOf(Blocks.BEDROCK))
            return false;
        BlockPos blockPos2 = block.up();
        if (!mc.world.isAir(blockPos2))
            return false;
        double d = blockPos2.getX();
        double e = blockPos2.getY();
        double f = blockPos2.getZ();
        List<Entity> list = mc.world.getOtherEntities((Entity)null, new Box(d, e, f, d + 1.0D, e + 2.0D, f + 1.0D));
        return list.isEmpty();
    }
}

