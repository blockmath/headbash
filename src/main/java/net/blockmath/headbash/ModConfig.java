package net.blockmath.headbash;


import net.blockmath.headbash.commands.*;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = HeadBashCommands.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_TELEPORTATION_COMMANDS = BUILDER
            .comment("Enable teleportation commands like /home and /back")
            .define("enable_teleportation_commands", true);

    private static final ModConfigSpec.IntValue HOME_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to use /home")
            .defineInRange("command_permission_home", Commands.LEVEL_MODERATORS, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

    private static final ModConfigSpec.IntValue SETHOME_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to use /sethome")
            .defineInRange("command_permission_sethome", Commands.LEVEL_MODERATORS, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

    private static final ModConfigSpec.IntValue BACK_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to use /back")
            .defineInRange("command_permission_back", Commands.LEVEL_MODERATORS, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

    private static final ModConfigSpec.DoubleValue TELEPORT_TIME_DELAY = BUILDER
            .comment("Delay of teleporting commands like /home and /back")
            .defineInRange("teleport_delay_time", 5.0, 0.0, 60.0);

    private static final ModConfigSpec.DoubleValue TELEPORT_DELAY_DISTANCE = BUILDER
            .comment("Maximum distance the player can move before cancelling a teleport")
            .defineInRange("teleport_delay_distance", 1.0, 0.0, Double.POSITIVE_INFINITY);

    private static final ModConfigSpec.IntValue BASH_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to use /bash")
            .defineInRange("command_permission_bash", Commands.LEVEL_ALL, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

    private static final ModConfigSpec.IntValue COPYPASTE_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to use /save and /load")
            .defineInRange("command_permission_saveload", Commands.LEVEL_MODERATORS, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

    static final ModConfigSpec SPEC = BUILDER.build();


    public static double maxTeleportDelayDistance = 0.0;
    public static double teleportDelayTime = 5.0;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {

        CopyPasteCommand.requiredPermissionLevel = COPYPASTE_PERMISSION_LEVEL.get();
        BashCommand.requiredPermissionLevel = BASH_PERMISSION_LEVEL.get();

        if (ENABLE_TELEPORTATION_COMMANDS.get()) {
            HomeCommand.requiredPermissionLevel = HOME_PERMISSION_LEVEL.get();
            SetHomeCommand.requiredPermissionLevel = SETHOME_PERMISSION_LEVEL.get();
            BackCommand.requiredPermissionLevel = BACK_PERMISSION_LEVEL.get();
        } else {
            HomeCommand.requiredPermissionLevel = Commands.LEVEL_OWNERS;
            SetHomeCommand.requiredPermissionLevel = Commands.LEVEL_OWNERS;
            BackCommand.requiredPermissionLevel = Commands.LEVEL_OWNERS;
        }

        teleportDelayTime = TELEPORT_TIME_DELAY.get();
        maxTeleportDelayDistance = TELEPORT_DELAY_DISTANCE.get();
    }
}
