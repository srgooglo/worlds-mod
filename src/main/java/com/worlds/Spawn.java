package com.worlds;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldProperties;

public class Spawn {
    public static BlockPos findSafePos(ServerWorld w, BlockPos sp) {
        BlockPos pos = sp;

        while (w.getBlockState(pos) != Blocks.AIR.getDefaultState()) {
            pos = pos.add(0, 1, 0);
        }

        return pos;
    }

    public static BlockPos getDefaultWorldPos(ServerWorld world) {
        WorldProperties prop = world.getLevelProperties();
        BlockPos pos = new BlockPos(prop.getSpawnX(), prop.getSpawnY(), prop.getSpawnZ());

        if (!world.getWorldBorder().contains(pos)) {
            BlockPos pp = get_pos(world.getWorldBorder().getCenterX(), 0.0, world.getWorldBorder().getCenterZ());

            pos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, new BlockPos(pp));
        }

        return pos;
    }

    public static BlockPos get_pos(double x, double y, double z) {
        return BlockPos.ofFloored(x, y, z);
    }
}
