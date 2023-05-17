package walksy.optimizer;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import walksy.optimizer.command.EnableOptimizerCommand;



public class WalksyCrystalOptimizerMod implements ClientModInitializer {
    public static MinecraftClient mc;

    @Override
    public void onInitializeClient() {
        mc = MinecraftClient.getInstance();
        EnableOptimizerCommand command = new EnableOptimizerCommand();
        command.initializeToggleCommands();
    }
}
