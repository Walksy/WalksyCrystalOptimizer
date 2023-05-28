package walksy.optimizer.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import walksy.optimizer.utils.PacketUtils;

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
            Vec3d camPos = mc.player.getEyePos();
            BlockHitResult blockHit = mc.world.raycast(new RaycastContext(camPos, camPos.add(PacketUtils.getClientLookVec().multiply(4.5)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (PacketUtils.isBlock(Blocks.OBSIDIAN, blockHit.getBlockPos()) || PacketUtils.isBlock(Blocks.BEDROCK, blockHit.getBlockPos())) {
                HitResult hitResult = mc.crosshairTarget;
                if (hitResult instanceof BlockHitResult) {
                    BlockHitResult hit = (BlockHitResult)hitResult;
                    BlockPos block = hit.getBlockPos();
                    if (PacketUtils.canPlaceCrystalServer(block))
                        context.getStack().decrement(-1);
                }
            }
        }
    }
}

