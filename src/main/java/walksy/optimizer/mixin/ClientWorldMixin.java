package walksy.optimizer.mixin;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import walksy.optimizer.mixinterface.IClientWorld;


@Mixin(ClientWorld.class)
public class ClientWorldMixin implements IClientWorld {



    @Shadow
    private PendingUpdateManager pendingUpdateManager;

    @Override
    public PendingUpdateManager obtainPendingUpdateManager() {
        return this.pendingUpdateManager;
    }
}
