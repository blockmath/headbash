package net.blockmath.headbash;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.blockmath.headbash.commands.BackCommand;
import net.blockmath.headbash.commands.BashCommand;
import net.blockmath.headbash.commands.HomeCommand;
import net.blockmath.headbash.commands.SetHomeCommand;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = HeadBashCommands.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue HOME_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to use /home")
            .defineInRange("command_permission_home", Commands.LEVEL_ALL, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

    private static final ModConfigSpec.IntValue SETHOME_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to use /sethome")
            .defineInRange("command_permission_sethome", Commands.LEVEL_ALL, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

    private static final ModConfigSpec.IntValue BASH_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to use /bash")
            .defineInRange("command_permission_bash", Commands.LEVEL_ALL, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

    private static final ModConfigSpec.IntValue BACK_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to use /back")
            .defineInRange("command_permission_back", Commands.LEVEL_ALL, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        BashCommand.requiredPermissionLevel = BASH_PERMISSION_LEVEL.get();
        HomeCommand.requiredPermissionLevel = HOME_PERMISSION_LEVEL.get();
        SetHomeCommand.requiredPermissionLevel = SETHOME_PERMISSION_LEVEL.get();
        BackCommand.requiredPermissionLevel = BACK_PERMISSION_LEVEL.get();
    }
}
