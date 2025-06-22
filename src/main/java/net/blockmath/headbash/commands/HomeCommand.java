package net.blockmath.headbash.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.blockmath.headbash.ModConfig;
import net.blockmath.headbash.commands.helpers.AttachmentTypes;
import net.blockmath.headbash.commands.helpers.ServerCommandScheduler;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import static net.blockmath.headbash.commands.helpers.CommandHelpers.perms;

public class HomeCommand {
    public static int requiredPermissionLevel = Commands.LEVEL_OWNERS;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("home")
                        .requires(perms(requiredPermissionLevel))
                            .executes(context -> returnHome(context.getSource()))
        );
    }

    private static int returnHome(CommandSourceStack source) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayer player) {
            AttachmentTypes.AttBlockPos homePos = player.getData(AttachmentTypes.HOME_POS);

            if (homePos.isValid()) {
                BlockPos playerPos = BlockPos.containing(player.getPosition(0));
                source.sendSuccess(() -> Component.literal("Teleporting in " + (int) Math.ceil(ModConfig.teleportDelayTime) + " seconds. Don't move!"), true);

                ServerCommandScheduler.get(source.getServer()).schedule(() -> doTeleport(source, playerPos, homePos), (int) (ModConfig.teleportDelayTime * SharedConstants.TICKS_PER_SECOND));

                return Command.SINGLE_SUCCESS;
            } else {
                source.sendFailure(Component.literal("No home set"));
                return 0;
            }

        } else {
            return 0;
        }
    }

    private static double blockDistance(BlockPos a, BlockPos b) {
        return new Vec3(a.getX() - b.getX(), a.getY() - b.getY(), a.getZ() - b.getZ()).length();
    }

    public static void doTeleport(CommandSourceStack source, BlockPos from, AttachmentTypes.AttBlockPos to) {
        if (source.getEntity() instanceof ServerPlayer player) {
            BlockPos playerPos = BlockPos.containing(player.getPosition(0));

            if (blockDistance(from, playerPos) <= ModConfig.maxTeleportDelayDistance) {
                source.sendSuccess(() -> Component.literal("Teleporting..."), true);

                player.setData(AttachmentTypes.BACK_POS, new AttachmentTypes.AttBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ()));

                player.teleportTo(to.getX() + 0.5, to.getY(), to.getZ() + 0.5);
            } else {
                source.sendFailure(Component.literal("Teleport cancelled."));
            }
        }
    }
}
