package walksy.optimizer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class Raycast {

    public static BlockHitResult cast(MinecraftClient client, double range) {
        Vec3d start = client.player.getEyePos();
        Vec3d look = client.player.getRotationVec(1.0F);
        Vec3d end = start.add(look.multiply(range));

        return client.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, client.player));
    }
}
