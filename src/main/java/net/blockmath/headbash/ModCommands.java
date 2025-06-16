package net.blockmath.headbash;

import com.mojang.brigadier.CommandDispatcher;
import net.blockmath.headbash.commands.BackCommand;
import net.blockmath.headbash.commands.BashCommand;
import net.blockmath.headbash.commands.HomeCommand;
import net.blockmath.headbash.commands.SetHomeCommand;
import net.minecraft.commands.CommandSourceStack;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        HomeCommand.register(dispatcher);
        SetHomeCommand.register(dispatcher);
        BackCommand.register(dispatcher);
        BashCommand.register(dispatcher);
    }
}
