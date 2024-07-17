package gg.meza.doobs.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import gg.meza.doobs.DeckedOutOBS;
import gg.meza.doobs.data.Location;
import gg.meza.doobs.data.Settings;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;

public class DungeonSetCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, Settings settings, Runnable beginProcessing) {
        dispatcher.register(
                literal("deckedout")
                .then(literal("dungeon")
                .then(literal("set")
                .then(argument("keyBarrel", blockPos())
                .executes(context -> {
                    BlockPos pos = getBlockPos(context, "keyBarrel");
                    DeckedOutOBS.LOGGER.info(Text.translatable("system.setting_dungeon", pos).getString());
                    settings.setDungeonPosition(pos, getDungeonDirection(context, pos));
                    beginProcessing.run();
                    return 1;
                })))));

        dispatcher.register(
                literal("deckedout")
                .then(literal("dungeon")
                .executes(context -> {
                    Location dungeon = settings.getDungeonPosition();
                    context.getSource().getPlayer().sendMessage(
                            Text.translatable("message.current_dungeon", dungeon.dungeonLocation().toShortString(), dungeon.dungeonDirection()), false);
                    return 1;
                })));
    }

    // Client version of BlockPosArgument.getBlockPos
    private static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource().getPlayer().getCommandSource());
    }

    private static Direction getDungeonDirection(CommandContext<FabricClientCommandSource> context, BlockPos dungeonCenter) {
        ClientWorld world = context.getSource().getWorld();
        BlockPos hopper = dungeonCenter.down();

        if(world.getBlockState(hopper.east()).getBlock() instanceof RedstoneTorchBlock) return Direction.WEST;
        if(world.getBlockState(hopper.west()).getBlock() instanceof RedstoneTorchBlock) return Direction.EAST;
        if(world.getBlockState(hopper.north()).getBlock() instanceof RedstoneTorchBlock) return Direction.SOUTH;
        if(world.getBlockState(hopper.south()).getBlock() instanceof RedstoneTorchBlock) return Direction.NORTH;

        throw new IllegalArgumentException(Text.translatable("message.calibration_failed").getString());
    }
}
