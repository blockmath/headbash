package net.blockmath.headbash.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.blockmath.headbash.commands.helpers.AttachmentTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class BackCommand {
    public static int requiredPermissionLevel = Commands.LEVEL_OWNERS;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("back")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(requiredPermissionLevel))
                        .executes(context -> returnBack(context.getSource()))
        );
    }

    private static int returnBack(CommandSourceStack source) throws CommandSyntaxException {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayer player) {
            AttachmentTypes.AttBlockPos homePos = player.getData(AttachmentTypes.BACK_POS);

            if (homePos.isValid()) {
                BlockPos playerPos = BlockPos.containing(player.getPosition(0));
                source.sendSuccess(() -> Component.literal("Returning..."), true);

                player.setData(AttachmentTypes.BACK_POS, new AttachmentTypes.AttBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ()));

                player.teleportTo(homePos.getX() + 0.5, homePos.getY(), homePos.getZ() + 0.5);

                return Command.SINGLE_SUCCESS;
            } else {
                source.sendFailure(Component.literal("No position to return to"));
                return 0;
            }

        } else {
            return 0;
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            BlockPos playerPos = BlockPos.containing(player.getPosition(0));
            player.setData(AttachmentTypes.BACK_POS, new AttachmentTypes.AttBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ()));
        }
    }
}
