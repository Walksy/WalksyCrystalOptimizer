package walksy.optimizer;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import walksy.optimizer.command.EnableOptimizerCommand;



public class WalksyCrystalOptimizerMod implements ClientModInitializer {
    public static MinecraftClient mc;


    /**
     * just because his mod works, doesn't mean it should be banned - Walksy's Mother
     */

    @Override
    public void onInitializeClient() {
        mc = MinecraftClient.getInstance();
        EnableOptimizerCommand command = new EnableOptimizerCommand();
        command.initializeToggleCommands();
    }
}
