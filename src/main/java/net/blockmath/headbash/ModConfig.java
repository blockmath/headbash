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

    private static final ModConfigSpec.IntValue BASH_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to use /bash")
            .defineInRange("command_permission_bash", Commands.LEVEL_ALL, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

    private static final ModConfigSpec.IntValue COPYPASTE_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to use /save and /load")
            .defineInRange("command_permission_saveload", Commands.LEVEL_MODERATORS, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {

        CopyPasteCommand.requiredPermissionLevel = COPYPASTE_PERMISSION_LEVEL.get();
        BashCommand.requiredPermissionLevel = BASH_PERMISSION_LEVEL.get();

    }
}
