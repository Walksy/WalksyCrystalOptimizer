package walksy.optimizer.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.optimizer.command.EnableOptimizerCommand;

import java.util.List;

import static walksy.optimizer.WalksyCrystalOptimizerMod.mc;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    /**
     * @Author Walksy
     */

    private int hitCount;
    private int breakingBlockTick;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", ordinal = 0), method = "tick()V")
    private void useOwnTicks(CallbackInfo ci) {
        if (EnableOptimizerCommand.fastCrystal) {
            ItemStack mainHandStack = mc.player.getMainHandStack();

            if (mc.interactionManager.isBreakingBlock() && isLookingAt(Blocks.OBSIDIAN, generalLookPos().getBlockPos())
                    || isLookingAt(Blocks.BEDROCK, generalLookPos().getBlockPos())) {
                breakingBlockTick++;
            } else breakingBlockTick = 0;

            if (breakingBlockTick > 5)
                return;

            if (!mc.options.useKey.isPressed()) {
                hitCount = 0;
            }
            if (hitCount == limitPackets())
                return;
            if (lookingAtSaidEntity()) {
                if (mc.options.attackKey.isPressed()) {
                    //removeSaidEntity().kill();
                    //removeSaidEntity().setRemoved(Entity.RemovalReason.KILLED);
                    //removeSaidEntity().onRemoved();
                    //mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(removeSaidEntity(), mc.player.isSneaking()));
                    hitCount++;
                }
            }
            if (!mainHandStack.isOf(Items.END_CRYSTAL)) {
                return;
            }
            if (mc.options.useKey.isPressed()
                    && isLookingAt(Blocks.OBSIDIAN, generalLookPos().getBlockPos())
                    || isLookingAt(Blocks.BEDROCK, generalLookPos().getBlockPos()))
            {
                    sendInteractBlockPacket(generalLookPos().getBlockPos(), generalLookPos().getSide());
                    if (canPlaceCrystalServer(generalLookPos().getBlockPos())) {
                        mc.player.swingHand(mc.player.getActiveHand());
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

    private Entity removeSaidEntity() {
        Entity entity = null;
        if (mc.crosshairTarget instanceof EntityHitResult hit) {
            if (hit.getEntity() instanceof EndCrystalEntity crystalEntity) {
                entity = crystalEntity;
            } else if (hit.getEntity() instanceof SlimeEntity slimeEntity) {
                entity = slimeEntity;
            } else if (hit.getEntity() instanceof MagmaCubeEntity magmaCubeEntity) {
                entity = magmaCubeEntity;
            }
        }
        return entity;
    }

    private boolean lookingAtSaidEntity() {
        return
         mc.crosshairTarget instanceof EntityHitResult entity && (entity.getEntity() instanceof EndCrystalEntity
                || entity.getEntity() instanceof MagmaCubeEntity
                || entity.getEntity() instanceof SlimeEntity);
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

    private ActionResult sendInteractBlockPacket(BlockPos pos, Direction dir) {
        Vec3d vec = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        return setPacket(vec,dir);
    }

    private ActionResult setPacket(Vec3d vec3d, Direction dir) {
        Vec3i vec3i = new Vec3i((int) vec3d.x, (int) vec3d.y, (int) vec3d.z);
        BlockPos pos = new BlockPos(vec3i);
        BlockHitResult result = new BlockHitResult(vec3d, dir,pos,false);
        return mc.interactionManager.interactBlock(mc.player,mc.player.getActiveHand(),result);
    }

    private int limitPackets() {
        int stop = 2;
        if (getPing() > 50) stop = 2;
        if (getPing() < 50) stop = 1;
        return stop;
    }

    private int getPing() {
        if (mc.getNetworkHandler() == null) return 0;

        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
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

