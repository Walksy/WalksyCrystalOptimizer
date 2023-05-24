package walksy.optimizer.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.optimizer.command.EnableOptimizerCommand;

import static walksy.optimizer.WalksyCrystalOptimizerMod.mc;
import static walksy.optimizer.command.EnableOptimizerCommand.displayMessage;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    /**
     * @Author Walksy
     */


    private static Vec3d getPlayerLookVec(PlayerEntity player) {
        float f = (float) Math.PI / 180;
        float pi = (float) Math.PI;
        float f1 = MathHelper.cos(-player.getYaw() * f - pi);
        float f2 = MathHelper.sin(-player.getYaw() * f - pi);
        float f3 = -MathHelper.cos(-player.getPitch() * f);
        float f4 = MathHelper.sin(-player.getPitch() * f);
        return new Vec3d(f2 * f3, f4, f1 * f3).normalize();
    }

    private static Vec3d getClientLookVec() {
        return getPlayerLookVec(mc.player);
    }


    private int getPing() {
        if (mc.getNetworkHandler() == null) return 0;

        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }

    private int shouldStopInt() {
        int stop = 2;
        if (getPing() > 70) stop = 2;
        if (getPing() < 70) stop = 1;
        return stop;
    }

    private int hasCrystaled;
    private int stopAutoCrystalFix;


    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V",
            ordinal = 0), method = "tick()V")
    private void useOwnTicks(CallbackInfo ci) {
        if (!EnableOptimizerCommand.fastCrystal)
            return;
        if (!mc.options.useKey.isPressed()) {
            hasCrystaled = 0;
        }
        if (!mc.options.attackKey.isPressed()) {
            stopAutoCrystalFix = 0;
        }

        if (hasCrystaled == shouldStopInt()) {
            return;
        }


        ItemStack mainHandStack = mc.player.getMainHandStack();
        if (!mainHandStack.isOf(Items.END_CRYSTAL))
            return;
        Vec3d camPos = mc.player.getEyePos();
        BlockHitResult lookPos = mc.world.raycast(new RaycastContext(camPos, camPos.add(getClientLookVec().multiply(4.5)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
        if (mc.crosshairTarget instanceof EntityHitResult hitEntity) {
            if (mc.options.attackKey.isPressed()) {
                if (stopAutoCrystalFix == 1)
                    return;
                //This code might not even be necessary but who cares
                if (hitEntity.getEntity() instanceof EndCrystalEntity crystal) {
                    mc.interactionManager.attackEntity(mc.player, crystal);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    stopAutoCrystalFix++;
                    hasCrystaled++;

                //DonutSMP + EastPVP bypass
                } else if (hitEntity.getEntity() instanceof MagmaCubeEntity magma) {
                    mc.interactionManager.attackEntity(mc.player, magma);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    stopAutoCrystalFix++;
                    hasCrystaled++;

                } else if (hitEntity.getEntity() instanceof SlimeEntity slime) {
                    mc.interactionManager.attackEntity(mc.player, slime);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    stopAutoCrystalFix++;
                    hasCrystaled++;
                }
            }
        }

        if (mc.options.useKey.isPressed()) {
            if (isLookingAt(Blocks.OBSIDIAN, lookPos.getBlockPos()) || isLookingAt(Blocks.BEDROCK, lookPos.getBlockPos())) {
                ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, lookPos);
                if (result.isAccepted() && result.shouldSwingHand())
                    //sends interactBlock Packets while the crystal is still placed, so when broken placement speed is much faster
                    interact(lookPos.getBlockPos(), lookPos.getSide());
            }
        }
    }

    private BlockState getBlockState(BlockPos pos) {
        return mc.world.getBlockState(pos);
    }
    private boolean isLookingAt(Block block, BlockPos pos) {
        return getBlockState(pos).getBlock() == block;
    }

    private ActionResult interact(BlockPos pos, Direction dir) {
        Vec3d vec = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        return interact(vec,dir);
    }

    private ActionResult interact(Vec3d vec3d, Direction dir) {
        Vec3i vec3i = new Vec3i((int) vec3d.x, (int) vec3d.y, (int) vec3d.z);
        BlockPos pos = new BlockPos(vec3i);
        BlockHitResult result = new BlockHitResult(vec3d, dir,pos,false);
        return mc.interactionManager.interactBlock(mc.player,mc.player.getActiveHand(),result);
    }
}
