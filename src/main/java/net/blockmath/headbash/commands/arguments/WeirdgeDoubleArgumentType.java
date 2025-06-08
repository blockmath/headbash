package net.blockmath.headbash.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.blockmath.headbash.HeadBashCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

class WeirdgeDoubleArgumentInfo implements ArgumentTypeInfo<WeirdgeDoubleArgumentType, WeirdgeDoubleArgumentInfo.Template> {
    public WeirdgeDoubleArgumentInfo() {
    }

    public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
        boolean flag = template.min != -Double.MAX_VALUE;
        boolean flag1 = template.max != Double.MAX_VALUE;
        buffer.writeByte(ArgumentUtils.createNumberFlags(flag, flag1));
        if (flag) {
            buffer.writeDouble(template.min);
        }

        if (flag1) {
            buffer.writeDouble(template.max);
        }

    }

    public @NotNull Template deserializeFromNetwork(FriendlyByteBuf buffer) {
        byte b0 = buffer.readByte();
        double d0 = ArgumentUtils.numberHasMin(b0) ? buffer.readDouble() : -Double.MAX_VALUE;
        double d1 = ArgumentUtils.numberHasMax(b0) ? buffer.readDouble() : Double.MAX_VALUE;
        return new Template(d0, d1);
    }

    public void serializeToJson(Template template, @NotNull JsonObject json) {
        if (template.min != -Double.MAX_VALUE) {
            json.addProperty("min", template.min);
        }

        if (template.max != Double.MAX_VALUE) {
            json.addProperty("max", template.max);
        }

    }

    public @NotNull Template unpack(WeirdgeDoubleArgumentType argument) {
        return new Template(argument.getMinimum(), argument.getMaximum());
    }

    public final class Template implements ArgumentTypeInfo.Template<WeirdgeDoubleArgumentType> {
        final double min;
        final double max;

        Template(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public @NotNull WeirdgeDoubleArgumentType instantiate(@NotNull CommandBuildContext context) {
            return WeirdgeDoubleArgumentType.doubleArg(this.min, this.max);
        }

        public @NotNull ArgumentTypeInfo<WeirdgeDoubleArgumentType, ?> type() {
            return WeirdgeDoubleArgumentInfo.this;
        }
    }
}


public class WeirdgeDoubleArgumentType implements ArgumentType<Double> {
    public static final DeferredRegister<ArgumentTypeInfo<?,?>> REGISTER = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, HeadBashCommands.MODID);

    public static final DeferredHolder<ArgumentTypeInfo<?,?>,?> HOLDER =
            REGISTER.register("weirdgedouble", () -> {
                ArgumentTypeInfos.registerByClass(WeirdgeDoubleArgumentType.class, new WeirdgeDoubleArgumentInfo());
                return new WeirdgeDoubleArgumentInfo();
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
            reader.readStringUntil(' ');
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