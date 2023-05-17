package walksy.optimizer.mixinterface;

import net.minecraft.client.network.PendingUpdateManager;

public interface IClientWorld {
    public PendingUpdateManager obtainPendingUpdateManager();
}
