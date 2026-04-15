package walksy.optimizer.mixin;

import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import walksy.optimizer.WorldContext;


@Mixin({EndCrystalItem.class})
public class EndCrystalItemMixin {

    @Inject(method = {"useOnBlock"}, at = {@At("HEAD")}, cancellable = true)
    private void modifyDecrementAmount(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = context.getStack();
        if (stack.isOf(Items.END_CRYSTAL)) {
            if (WorldContext.isObsidianOrBedrock(context.getWorld(), context.getBlockPos())) {
                if (WorldContext.canPlaceCrystal(context.getWorld(), context.getBlockPos())) {
                    context.getStack().decrement(-1);
                }
            }
        }
    }
}

