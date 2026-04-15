package walksy.optimizer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class WorldContext {

    public static boolean isObsidianOrBedrock(World world, BlockPos pos) {
        return world.getBlockState(pos).isOf(Blocks.OBSIDIAN) || world.getBlockState(pos).isOf(Blocks.BEDROCK);
    }

    public static boolean canPlaceCrystal(World world, BlockPos block) {
        if (!isObsidianOrBedrock(world, block)) {
            return false;
        }
        BlockPos up = block.up();
        if (!world.isAir(up)) {
            return false;
        }
        Box box = new Box(up.getX(), up.getY(), up.getZ(), up.getX() + 1.0, up.getY() + 2.0, up.getZ() + 1.0);
        List<Entity> entities = world.getOtherEntities(null, box);
        return entities.isEmpty();
    }

    public static boolean isBlock(World world, BlockPos pos, Block... blocks) {
        BlockState state = world.getBlockState(pos);
        for (Block block : blocks) {
            if (state.isOf(block)) {
                return true;
            }
        }
        return false;
    }
}
