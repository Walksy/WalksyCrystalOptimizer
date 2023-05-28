package walksy.optimizer;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
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
