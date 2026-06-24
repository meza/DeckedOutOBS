package gg.meza.doobs.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record Location(Direction dungeonDirection, BlockPos dungeonLocation) {
}
