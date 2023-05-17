package walksy.optimizer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ItemStack.class})
public abstract class ItemStackMixin {


    @Inject(method = {"getBobbingAnimationTime"}, at = {@At("HEAD")}, cancellable = true)
    private void reducePlaceDelay(CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(0);
    }
}
