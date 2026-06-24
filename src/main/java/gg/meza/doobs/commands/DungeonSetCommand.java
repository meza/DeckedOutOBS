package gg.meza.doobs.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import gg.meza.doobs.DeckedOutOBS;
import gg.meza.doobs.data.Location;
import gg.meza.doobs.data.Settings;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.RedstoneTorchBlock;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos;

public class DungeonSetCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, Settings settings, Runnable beginProcessing) {
        dispatcher.register(
                literal("deckedout")
                .then(literal("dungeon")
                .then(literal("set")
                .then(argument("keyBarrel", blockPos())
                .executes(context -> {
                    BlockPos pos = getBlockPos(context, "keyBarrel");
                    DeckedOutOBS.LOGGER.info(Component.translatable("decked-out-obs.system.setting_dungeon", pos).getString());
                    settings.setDungeonPosition(pos, getDungeonDirection(context, pos));
                    beginProcessing.run();
                    return 1;
                })))));

        dispatcher.register(
                literal("deckedout")
                .then(literal("dungeon")
                .executes(context -> {
                    Location dungeon = settings.getDungeonPosition();
                    context.getSource().getPlayer().sendSystemMessage(
                            Component.translatable("decked-out-obs.message.current_dungeon", dungeon.dungeonLocation().toShortString(), dungeon.dungeonDirection()));
                    return 1;
                })));
    }

    // Client version of BlockPosArgument.getBlockPos
    private static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name) {
        CommandSourceStack s = new CommandSourceStack(null, context.getSource().getPosition(), context.getSource().getRotation(), null, perm -> false, null, null, null, null);

        return context.getArgument(name, WorldCoordinates.class).getBlockPos(s);
    }

    private static Direction getDungeonDirection(CommandContext<FabricClientCommandSource> context, BlockPos dungeonCenter) {
        ClientLevel world = context.getSource().getLevel();
        BlockPos hopper = dungeonCenter.below();

        if(world.getBlockState(hopper.east()).getBlock() instanceof RedstoneTorchBlock) return Direction.WEST;
        if(world.getBlockState(hopper.west()).getBlock() instanceof RedstoneTorchBlock) return Direction.EAST;
        if(world.getBlockState(hopper.north()).getBlock() instanceof RedstoneTorchBlock) return Direction.SOUTH;
        if(world.getBlockState(hopper.south()).getBlock() instanceof RedstoneTorchBlock) return Direction.NORTH;

        throw new IllegalArgumentException(Component.translatable("decked-out-obs.message.calibration_failed").getString());
    }
}
