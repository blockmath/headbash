package net.blockmath.headbash.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.blockmath.headbash.HeadBashCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class HomeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("home")
                        .executes(context -> {
                            return returnHome(context.getSource());
                        })
        );
    }

    private static int returnHome(CommandSourceStack source) throws CommandSyntaxException {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayer player) {
            int[] homePos = player.getPersistentData().getIntArray(HeadBashCommands.MODID + "homepos");

            if (homePos.length != 0) {
                source.sendSuccess(() -> Component.literal("Returning home..."), true);

                player.teleportTo(homePos[0] + 0.5, homePos[1], homePos[2] + 0.5);

                return Command.SINGLE_SUCCESS;
            } else {
                source.sendFailure(Component.literal("No home set"));
                return 0;
            }

        } else {
            return 0;
        }
    }
}
