package walksy.optimizer.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.optimizer.command.EnableOptimizerCommand;
import walksy.optimizer.utils.PacketUtils;

import static walksy.optimizer.WalksyCrystalOptimizerMod.mc;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    /**
     * @Author Walksy
     */

    private int hitCount;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", ordinal = 0), method = "tick()V")
    private void useOwnTicks(CallbackInfo ci) {
        if (EnableOptimizerCommand.fastCrystal) {
            boolean isUseKeyPressed = mc.options.useKey.isPressed();
            boolean isAttackKeyPressed = mc.options.attackKey.isPressed();
            int stopInt = PacketUtils.shouldStopInt();
            ItemStack mainHandStack = mc.player.getMainHandStack();
            Vec3d camPos = mc.player.getEyePos();
            Vec3d clientLookVec = PacketUtils.getClientLookVec();
            BlockHitResult lookPos = mc.world.raycast(new RaycastContext(camPos, camPos.add(clientLookVec.multiply(4.5)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (isAttackKeyPressed && PacketUtils.isBlock(Blocks.OBSIDIAN,lookPos.getBlockPos()))
                return;
            if (!isUseKeyPressed) {
                hitCount = 0;
            }
            if (hitCount == stopInt) {
                return;
            }
            if (!mainHandStack.isOf(Items.END_CRYSTAL)) {
                return;
            }
            if (mc.crosshairTarget instanceof EntityHitResult entity && (entity.getEntity() instanceof EndCrystalEntity crystal || entity.getEntity() instanceof MagmaCubeEntity magma || entity.getEntity() instanceof SlimeEntity slime)) {
                if (isAttackKeyPressed) {
                    hitCount++;
                }
            }
            if (isUseKeyPressed && (PacketUtils.isLookingAt(Blocks.OBSIDIAN, lookPos.getBlockPos()) || PacketUtils.isLookingAt(Blocks.BEDROCK, lookPos.getBlockPos()))) {
                ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, lookPos);
                if (result.isAccepted() && result.shouldSwingHand()) {
                    PacketUtils.interact(lookPos.getBlockPos(), lookPos.getSide());
                }
            }
        }
    }
}
