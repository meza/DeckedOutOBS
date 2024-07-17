package gg.meza.doobs.cardProcessing;

import gg.meza.doobs.data.Location;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardCalloutsTest {

    @Test
    void piratesBootyLocationWhenFacingWest() {
        assertCalloutPosition(new BlockPos(-538, 112, 1980), Direction.WEST, "piratesBooty", new BlockPos(-549, -55, 1928));
        assertCalloutPosition(new BlockPos(-538, 112, 1980), Direction.WEST, "eyesOnThePrize", new BlockPos(-539, -55, 1924));
        assertCalloutPosition(new BlockPos(-538, 112, 1980), Direction.WEST, "avalanche", new BlockPos(-524, -55, 1924));
    }

    @Test
    void piratesBootyLocationWhenFacingSouth() {
        assertCalloutPosition(new BlockPos(19, 309, 50), Direction.SOUTH, "piratesBooty", new BlockPos(-33, 142, 61));
        assertCalloutPosition(new BlockPos(19, 309, 50), Direction.SOUTH, "eyesOnThePrize", new BlockPos(-37, 142, 51));
        assertCalloutPosition(new BlockPos(19, 309, 50), Direction.SOUTH, "brilliance", new BlockPos(-38, 142, 46));
    }

    @Test
    void piratesBootyLocationWhenFacingEast() {
        assertCalloutPosition(new BlockPos(-100, 309, -25), Direction.EAST, "piratesBooty", new BlockPos(-89, 142, 27));
        assertCalloutPosition(new BlockPos(-100, 309, -25), Direction.EAST, "eyesOnThePrize", new BlockPos(-99, 142, 31));
        assertCalloutPosition(new BlockPos(-100, 309, -25), Direction.EAST, "brilliance", new BlockPos(-104, 142, 32));
    }

    @Test
    void piratesBootyLocationWhenFacingNorth() {
        assertCalloutPosition(new BlockPos(-62, 309, -6), Direction.NORTH, "piratesBooty", new BlockPos(-10, 142, -17));
        assertCalloutPosition(new BlockPos(-62, 309, -6), Direction.NORTH, "eyesOnThePrize", new BlockPos(-6, 142, -7));
        assertCalloutPosition(new BlockPos(-62, 309, -6), Direction.NORTH, "brilliance", new BlockPos(-5, 142, -2));
    }

    private void assertCalloutPosition(BlockPos startingLocation, Direction direction, String callout, BlockPos expectedPosition) {
        Map<BlockPos, String> callouts = CardCallouts.getCallouts(new Location(direction, startingLocation));
        BlockPos actualPosition = callouts.entrySet().stream()
                .filter(entry -> callout.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        assertEquals(expectedPosition, actualPosition);
    }
}
