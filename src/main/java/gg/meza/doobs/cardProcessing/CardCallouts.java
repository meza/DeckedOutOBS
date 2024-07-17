package gg.meza.doobs.cardProcessing;

import gg.meza.doobs.data.Location;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CardCallouts {

    public static Map<BlockPos, String> getCallouts(Location dungeonLocation) {
        ConcurrentHashMap<Vec3i, String> relative = new ConcurrentHashMap<>() {{
            put(new Vec3i(-11, -167, 52), "piratesBooty");
            put(new Vec3i(-11, -167, 53), "momentOfClarity");
            put(new Vec3i(-11, -167, 54), "chillStep");
            put(new Vec3i(-11, -167, 55), "treasureHunter");
            put(new Vec3i(-11, -167, 56), "eerieSilence");
            put(new Vec3i(-11, -167, 57), "emberSeeker");
            put(new Vec3i(-11, -167, 58), "sneak");
            put(new Vec3i(-11, -167, 59), "stability");

            put(new Vec3i(-6, -167, 52), "adrenalineRush");
            put(new Vec3i(-6, -167, 53), "payToWin");
            put(new Vec3i(-6, -167, 54), "smashAndGrab");
            put(new Vec3i(-6, -167, 55), "recklessCharge");
            put(new Vec3i(-6, -167, 56), "swagger");
            put(new Vec3i(-6, -167, 57), "frostFocus");
            put(new Vec3i(-6, -167, 58), "empty");
            put(new Vec3i(-6, -167, 59), "treadLightly");

            put(new Vec3i(-1, -167, 52), "empty");
            put(new Vec3i(-1, -167, 53), "empty");
            put(new Vec3i(-1, -167, 54), "dungeonRepairs");
            put(new Vec3i(-1, -167, 55), "empty");
            put(new Vec3i(-1, -167, 56), "eyesOnThePrize");
            put(new Vec3i(-1, -167, 57), "nimbleLooting");
            put(new Vec3i(-1, -167, 58), "empty");
            put(new Vec3i(-1, -167, 59), "evasion");

            put(new Vec3i(4, -167, 52), "stumble");
            put(new Vec3i(4, -167, 53), "empty");
            put(new Vec3i(4, -167, 54), "empty");
            put(new Vec3i(4, -167, 55), "empty");
            put(new Vec3i(4, -167, 56), "empty");
            put(new Vec3i(4, -167, 57), "brilliance");
            put(new Vec3i(4, -167, 58), "quickstep");
            put(new Vec3i(4, -167, 59), "deepfrost");

            put(new Vec3i(9, -167, 52), "cashCow");
            put(new Vec3i(9, -167, 53), "empty");
            put(new Vec3i(9, -167, 54), "sprint");
            put(new Vec3i(9, -167, 55), "empty");
            put(new Vec3i(9, -167, 56), "beastSense");
            put(new Vec3i(9, -167, 57), "empty");
            put(new Vec3i(9, -167, 58), "lootAndScoot");
            put(new Vec3i(9, -167, 59), "empty");

            put(new Vec3i(14, -167, 52), "coldSnap");
            put(new Vec3i(14, -167, 53), "empty");
            put(new Vec3i(14, -167, 54), "secondWind");
            put(new Vec3i(14, -167, 55), "boundingStrides");
            put(new Vec3i(14, -167, 56), "avalanche");
            put(new Vec3i(14, -167, 57), "empty");
            put(new Vec3i(14, -167, 58), "empty");
            put(new Vec3i(14, -167, 59), "empty");
        }};

        Map<BlockPos, String> callouts = new ConcurrentHashMap<>();
        for (Map.Entry<Vec3i, String> callout : relative.entrySet()) {
            Vec3i relativePos = getRelativePosition(dungeonLocation.dungeonDirection(), callout);
            BlockPos absolutePos = dungeonLocation.dungeonLocation().add(relativePos);
            callouts.put(absolutePos, callout.getValue());
        }

        return callouts;
    }

    private static Vec3i getRelativePosition(Direction dungeonDirection, Map.Entry<Vec3i, String> callout) {
        Vec3i relativePos = callout.getKey();
        relativePos = switch (dungeonDirection) {
            case NORTH -> new Vec3i(relativePos.getZ(), relativePos.getY(), relativePos.getX());
            case EAST -> new Vec3i(-relativePos.getX(), relativePos.getY(), relativePos.getZ());
            case SOUTH -> new Vec3i(-relativePos.getZ(), relativePos.getY(), -relativePos.getX());
            case WEST -> new Vec3i(relativePos.getX(), relativePos.getY(), -relativePos.getZ());
            default -> relativePos;
        };
        return relativePos;
    }
}
