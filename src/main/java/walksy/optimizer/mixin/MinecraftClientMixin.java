package walksy.optimizer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import walksy.optimizer.WalksyCrystalOptimizer;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"), method = "doItemUse", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void onDoItemUse(CallbackInfo ci, Hand[] var1, int var2, int var3, Hand hand, ItemStack itemStack, EntityHitResult entityHitResult, Entity entity, ActionResult actionResult, BlockHitResult blockHitResult, int i) {
        if (itemStack.isOf(Items.END_CRYSTAL) && WalksyCrystalOptimizer.getOptimizer().stopItemUse(itemStack)) {
            ci.cancel();
        }
    }
}
