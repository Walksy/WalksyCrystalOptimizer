package walksy.optimizer.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
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
    private int stopAnomalyInt;

    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V",
            ordinal = 0), method = "tick()V")
    private void useOwnTicks(CallbackInfo ci) {
        //Used a tickMethod as it's generally faster than sending packets through a ClientConnectionMixin
        if (!EnableOptimizerCommand.fastCrystal)
            return;
        if (!mc.options.useKey.isPressed()) {
            hitCount = 0;
        }
        if (!mc.options.attackKey.isPressed()) {
            stopAnomalyInt = 0;
        }
        if (hitCount == PacketUtils.shouldStopInt()) {
            return;
        }
        ItemStack mainHandStack = mc.player.getMainHandStack();
        if (!mainHandStack.isOf(Items.END_CRYSTAL))
            return;
        if (mc.crosshairTarget instanceof EntityHitResult entity) {
            if (entity.getEntity() instanceof EndCrystalEntity
                    || entity.getEntity() instanceof MagmaCubeEntity //checks magma cubes so the tracking int is counting attacks on servers like donutsmp and eastpvp
                    || entity.getEntity() instanceof SlimeEntity) //same reason for magma
            {
                if (mc.options.attackKey.isPressed()) {
                    if (stopAnomalyInt == 1) return; //sometimes the int is counted more than once, this stops that
                    hitCount++;
                    stopAnomalyInt++;
                    //records the amount of times a crystal is destroyed by the player
                    //making sure crystals don't stack by limiting sent packets
                }
            }
        }
        Vec3d camPos = mc.player.getEyePos();
        BlockHitResult lookPos = mc.world.raycast(new RaycastContext(camPos, camPos.add(PacketUtils.getClientLookVec().multiply(4.5)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
        if (mc.options.useKey.isPressed()) {
            if (PacketUtils.isLookingAt(Blocks.OBSIDIAN, lookPos.getBlockPos()) || PacketUtils.isLookingAt(Blocks.BEDROCK, lookPos.getBlockPos())) {
                ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, lookPos);
                if (result.isAccepted() && result.shouldSwingHand()) {
                    //cancels custom interactBlock packets being sent while a crystal is placed
                    if (PacketUtils.canPlaceCrystalServer(lookPos.getBlockPos())) {
                        PacketUtils.interact(lookPos.getBlockPos(), lookPos.getSide());
                    }
                }
            }
        }
    }
}
