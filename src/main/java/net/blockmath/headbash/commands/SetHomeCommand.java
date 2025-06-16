package net.blockmath.headbash.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.blockmath.headbash.commands.helpers.AttachmentTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SetHomeCommand {
    public static int requiredPermissionLevel = Commands.LEVEL_OWNERS;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("sethome")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(requiredPermissionLevel))
                            .executes(context -> setHome(context.getSource()))
        );
    }

    private static int setHome(CommandSourceStack source) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayer player) {
            BlockPos playerPos = BlockPos.containing(player.getPosition(0));
            String pos = "(" + playerPos.getX() + ", " + playerPos.getY() + ", " + playerPos.getZ() + ")";

            player.setData(AttachmentTypes.HOME_POS, new AttachmentTypes.AttBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ()));

            source.sendSuccess(() -> Component.literal("Set home at " + pos), true);

            return Command.SINGLE_SUCCESS;
        } else {
            return 0;
        }


    }
}
