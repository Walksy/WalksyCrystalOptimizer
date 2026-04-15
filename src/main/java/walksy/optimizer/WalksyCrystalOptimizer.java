package walksy.optimizer;

import net.fabricmc.api.ClientModInitializer;

public class WalksyCrystalOptimizer implements ClientModInitializer {

    private static Optimizer optimizer;

    @Override
    public void onInitializeClient() {
        optimizer = new Optimizer();
    }

    public static Optimizer getOptimizer() {
        return optimizer;
    }
}
