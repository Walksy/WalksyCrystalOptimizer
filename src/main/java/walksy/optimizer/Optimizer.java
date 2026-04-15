package walksy.optimizer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class Optimizer {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private int hitCount = 0;
    private int breakingBlockTick = 0;

    public void tick() {
        if (this.client.player == null || this.client.world == null) {
            return;
        }
        this.handleBlockBreakingState();
        if (this.breakingBlockTick > 2) {
            return;
        }
        if (!client.options.useKey.isPressed()) {
            this.hitCount = 0;
        }
        if (this.hitCount >= this.getPacketLimit()) {
            return;
        }
        this.processEntityRemoval();
        this.processCrystalPlacement();
    }

    private void handleBlockBreakingState() {
        if (this.client.options.attackKey.isPressed()) {
            this.breakingBlockTick++;
        } else {
            this.breakingBlockTick = 0;
        }
    }

    private void processEntityRemoval() {
        if (!this.client.options.attackKey.isPressed()) {
            return;
        }

        Entity target = this.getValidTarget(this.client.crosshairTarget);
        if (target != null) {
            if (this.hitCount >= 1) {
                target.setRemoved(Entity.RemovalReason.KILLED);
            }
            this.hitCount++;
        }
    }

    private void processCrystalPlacement() {
        if (!this.client.options.useKey.isPressed()) {
            return;
        }
        if (!this.client.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
            return;
        }
        BlockHitResult hit = Raycast.cast(client, 4.5);
        if (hit == null) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        if (WorldContext.isObsidianOrBedrock(this.client.world, pos)) {
            this.client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);

            if (WorldContext.canPlaceCrystal(client.world, pos)) {
                this.client.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    private Entity getValidTarget(HitResult hitResult) {
        if (hitResult instanceof EntityHitResult entityHit) {
            Entity e = entityHit.getEntity();
            if (e instanceof EndCrystalEntity || e instanceof SlimeEntity || e instanceof MagmaCubeEntity) {
                return e;
            }
        }
        return null;
    }

    public boolean stopItemUse(ItemStack stack) {
        if (!stack.isOf(Items.END_CRYSTAL)) {
            return false;
        }
        return this.hitCount < this.getPacketLimit();
    }

    //I don't even remember what this does...
    private int getPacketLimit() {
        int ping = this.getPing();
        return (ping < 50) ? 1 : 2;
    }

    //probably taken from meteor
    private int getPing() {
        if (client.getNetworkHandler() == null || this.client.player == null) {
            return 0;
        }
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry != null ? entry.getLatency() : 0;
    }
}