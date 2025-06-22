package net.blockmath.headbash.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashMap;

import static net.blockmath.headbash.commands.helpers.CommandHelpers.perms;

public class CopyPasteCommand {
    public static int requiredPermissionLevel = Commands.LEVEL_OWNERS;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("copy")
                        .requires(perms(requiredPermissionLevel))
                        .then(
                                Commands.argument("pos1", BlockPosArgument.blockPos()).then(
                                        Commands.argument("pos2", BlockPosArgument.blockPos())
                                                .then(
                                                        Commands.literal("noentities")
                                                                .executes(context -> copy(
                                                                        context.getSource(),
                                                                        BlockPosArgument.getBlockPos(context, "pos1"),
                                                                        BlockPosArgument.getBlockPos(context, "pos2"),
                                                                        false
                                                                ))
                                                )
                                                .executes(context -> copy(
                                                        context.getSource(),
                                                        BlockPosArgument.getBlockPos(context, "pos1"),
                                                        BlockPosArgument.getBlockPos(context, "pos2"),
                                                        true
                                                ))
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("paste")
                        .requires(perms(requiredPermissionLevel))
                        .then(
                                Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> paste(
                                                context.getSource(),
                                                BlockPosArgument.getBlockPos(context, "pos")
                                        ))
                        )
        );
    }

    public static int copy(CommandSourceStack source, BlockPos pos1, BlockPos pos2, boolean includeEntities) {

        BlockPos pos_min, pos_max;

        pos_min = BlockPos.min(pos1, pos2);
        pos_max = BlockPos.max(pos1, pos2);

        StructureTemplate template = clipboards.getOrDefault(source.getEntity(), new StructureTemplate());
        source.sendSuccess(() -> Component.literal("Copying..."), true);
        template.fillFromWorld(source.getLevel(), pos_min, BlockPos.containing(1 + pos_max.getX() - pos_min.getX(), 1 + pos_max.getY() - pos_min.getY(), 1 + pos_max.getZ() - pos_min.getZ()), includeEntities, Blocks.STRUCTURE_VOID);

        clipboards.put(source.getEntity(), template);

        return Command.SINGLE_SUCCESS;
    }

    public static int paste(CommandSourceStack source, BlockPos pos) {
        StructureTemplate template = clipboards.get(source.getEntity());

        if (template != null) {
            source.sendSuccess(() -> Component.literal("Pasting..."), true);
            template.placeInWorld(source.getLevel(), pos, pos, new StructurePlaceSettings().setIgnoreEntities(false), RandomSource.create(), 2);
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendFailure(Component.literal("No data in clipboard"));
            return 0;
        }
    }

    public static final HashMap<Entity, StructureTemplate> clipboards = new HashMap<>();


}
