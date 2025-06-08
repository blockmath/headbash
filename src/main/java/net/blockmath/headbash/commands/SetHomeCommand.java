package net.blockmath.headbash.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.blockmath.headbash.HeadBashCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SetHomeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("sethome")
                        .executes(context -> setHome(context.getSource()))
        );
    }

    private static int setHome(CommandSourceStack source) throws CommandSyntaxException {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayer player) {
            BlockPos playerPos = BlockPos.containing(player.getPosition(0));
            String pos = "(" + playerPos.getX() + ", " + playerPos.getY() + ", " + playerPos.getZ() + ")";

            player.getPersistentData().putIntArray(HeadBashCommands.MODID + "homepos", new int[]{playerPos.getX(), playerPos.getY(), playerPos.getZ()});

            source.sendSuccess(() -> Component.literal("Set home at " + pos), true);

            return Command.SINGLE_SUCCESS;
        } else {
            return 0;
        }


    }
}
