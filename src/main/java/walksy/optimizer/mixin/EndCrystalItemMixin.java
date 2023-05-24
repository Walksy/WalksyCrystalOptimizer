package walksy.optimizer.mixin;

import com.sun.jna.platform.win32.Crypt32Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;

import static walksy.optimizer.WalksyCrystalOptimizerMod.mc;

@Mixin({EndCrystalItem.class})
public class EndCrystalItemMixin {


    /**
     * @Author Walksy
     */

    private Vec3d getPlayerLookVec(PlayerEntity player) {
        float f = 0.017453292F;
        float pi = 3.1415927F;
        float f1 = MathHelper.cos(-player.getYaw() * f - pi);
        float f2 = MathHelper.sin(-player.getYaw() * f - pi);
        float f3 = -MathHelper.cos(-player.getPitch() * f);
        float f4 = MathHelper.sin(-player.getPitch() * f);
        return (new Vec3d((f2 * f3), f4, (f1 * f3))).normalize();
    }

    private Vec3d getClientLookVec() {
        return getPlayerLookVec((PlayerEntity) mc.player);
    }

    private boolean isBlock(Block block, BlockPos pos) {
        return (getBlockState(pos).getBlock() == block);
    }

    private BlockState getBlockState(BlockPos pos) {
        return mc.world.getBlockState(pos);
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




    /**
     * Stops crystals from decreasing too much
     * PS: does not work on singleplayer
     */

    @Inject(method = {"useOnBlock"}, at = {@At("HEAD")}, cancellable = true)
    private void modifyDecrementAmount(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack mainHandStack = mc.player.getMainHandStack();
        if (mainHandStack.isOf(Items.END_CRYSTAL)) {
            Vec3d camPos = mc.player.getEyePos();
            BlockHitResult blockHit = mc.world.raycast(new RaycastContext(camPos, camPos.add(getClientLookVec().multiply(4.5)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (isBlock(Blocks.OBSIDIAN, blockHit.getBlockPos()) || isBlock(Blocks.BEDROCK, blockHit.getBlockPos())) {
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
}

