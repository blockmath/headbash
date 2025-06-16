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

public class HomeCommand {
    public static int requiredPermissionLevel = Commands.LEVEL_OWNERS;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("home")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(requiredPermissionLevel))
                            .executes(context -> returnHome(context.getSource()))
        );
    }

    private static int returnHome(CommandSourceStack source) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayer player) {
            AttachmentTypes.AttBlockPos homePos = player.getData(AttachmentTypes.HOME_POS);

            if (homePos.isValid()) {
                BlockPos playerPos = BlockPos.containing(player.getPosition(0));
                source.sendSuccess(() -> Component.literal("Returning home..."), true);

                player.setData(AttachmentTypes.BACK_POS, new AttachmentTypes.AttBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ()));

                player.teleportTo(homePos.getX() + 0.5, homePos.getY(), homePos.getZ() + 0.5);

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
