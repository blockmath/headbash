package net.blockmath.headbash.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.blockmath.headbash.HeadBashCommands;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.Collection;


public class WeirdgeDoubleArgumentType implements ArgumentType<Double> {
    public static final DeferredRegister<ArgumentTypeInfo<?,?>> REGISTER = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, HeadBashCommands.MODID);

    public static final DeferredHolder<ArgumentTypeInfo<?,?>,ArgumentTypeInfo<WeirdgeDoubleArgumentType, WeirdgeDoubleArgumentInfo.Template>> HOLDER =
            REGISTER.register("weirdgedouble", () -> {
                WeirdgeDoubleArgumentInfo wo = new WeirdgeDoubleArgumentInfo();
                ArgumentTypeInfos.registerByClass(WeirdgeDoubleArgumentType.class, wo);
                return wo;
            });


    private static final Collection<String> EXAMPLES = Arrays.asList("0", "1.2", ".5", "-1", "-.5", "-1234.56");

    private final double minimum;
    private final double maximum;

    private WeirdgeDoubleArgumentType(final double minimum, final double maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public static WeirdgeDoubleArgumentType doubleArg() {
        return doubleArg(-Double.MAX_VALUE);
    }

    public static WeirdgeDoubleArgumentType doubleArg(final double min) {
        return doubleArg(min, Double.MAX_VALUE);
    }

    public static WeirdgeDoubleArgumentType doubleArg(final double min, final double max) {
        return new WeirdgeDoubleArgumentType(min, max);
    }

    public static double getDouble(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Double.class);
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    @Override
    public Double parse(final StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        if (reader.peek() == '$') {
            while (reader.peek() != ' ') {
                reader.read();
            }
            return Double.NaN;
        } else {
            final double result = reader.readDouble();
            if (result < minimum) {
                reader.setCursor(start);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooLow().createWithContext(reader, result, minimum);
            }
            if (result > maximum) {
                reader.setCursor(start);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooHigh().createWithContext(reader, result, maximum);
            }
            return result;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof WeirdgeDoubleArgumentType that)) return false;

        return maximum == that.maximum && minimum == that.minimum;
    }

    @Override
    public int hashCode() {
        return (int) (31 * minimum + maximum);
    }

    @Override
    public String toString() {
        if (minimum == -Double.MAX_VALUE && maximum == Double.MAX_VALUE) {
            return "double()";
        } else if (maximum == Double.MAX_VALUE) {
            return "double(" + minimum + ")";
        } else {
            return "double(" + minimum + ", " + maximum + ")";
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}