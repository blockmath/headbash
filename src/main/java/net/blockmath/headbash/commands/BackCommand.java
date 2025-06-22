package net.blockmath.headbash.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.blockmath.headbash.Config;
import net.blockmath.headbash.commands.helpers.AttachmentTypes;
import net.blockmath.headbash.commands.helpers.ServerCommandScheduler;
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

import static net.blockmath.headbash.commands.helpers.CommandHelpers.perms;

public class BackCommand {
    public static int requiredPermissionLevel = Commands.LEVEL_OWNERS;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("back")
                        .requires(perms(requiredPermissionLevel))
                        .executes(context -> returnBack(context.getSource()))
        );
    }

    private static int returnBack(CommandSourceStack source) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayer player) {
            AttachmentTypes.AttBlockPos backPos = player.getData(AttachmentTypes.BACK_POS);

            if (backPos.isValid()) {
                BlockPos playerPos = BlockPos.containing(player.getPosition(0));
                source.sendSuccess(() -> Component.literal("Teleporting in " + Config.teleportDelayTime + " seconds. Don't move!"), true);

                ServerCommandScheduler.get(source.getServer()).schedule(() -> HomeCommand.doTeleport(source, playerPos, backPos), (int) (Config.teleportDelayTime * source.getServer().tickRateManager().tickrate()));

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
