package gg.meza.doobs.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import gg.meza.doobs.data.Settings;
import gg.meza.doobs.server.BasicHttpServer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ServerPortCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, Settings settings, Runnable beginProcessing) {
        dispatcher.register(
                literal("deckedout")
                .then(literal("port")
                .then(literal("set")
                .then(argument("serverPort", IntegerArgumentType.integer(1024, 65535))
                .executes(context -> {
                    int port = IntegerArgumentType.getInteger(context, "serverPort");
                    if ((!BasicHttpServer.isPortAvailable(port)) && (port != settings.getPort())) {
                        context.getSource().getPlayer().sendMessage(Text.translatable("decked-out-obs.message.port_in_use", port), false);
                        return 0;
                    }

                    settings.setPort(port);
                    beginProcessing.run();
                    return 1;
                })))));

        dispatcher.register(
                literal("deckedout")
                .then(literal("port")
                .executes(context -> {
                    context.getSource().getPlayer().sendMessage(Text.translatable("decked-out-obs.message.current_port", settings.getPort()), false);
                    return 1;
                })));
    }
}
