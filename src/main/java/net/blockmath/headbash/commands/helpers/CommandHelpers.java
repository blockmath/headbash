package net.blockmath.headbash.commands.helpers;

import net.minecraft.commands.CommandSourceStack;

import java.util.function.Predicate;

public class CommandHelpers {
    protected CommandHelpers() {}

    public static Predicate<CommandSourceStack> perms(int permissionLevel) {
        return (CommandSourceStack commandSourceStack) -> commandSourceStack.hasPermission(permissionLevel);
    }
}
