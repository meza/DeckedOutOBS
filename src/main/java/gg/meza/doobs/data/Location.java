package gg.meza.doobs.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record Location(Direction dungeonDirection, BlockPos dungeonLocation) {
}
