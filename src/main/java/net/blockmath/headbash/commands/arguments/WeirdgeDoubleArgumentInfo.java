package net.blockmath.headbash.commands.arguments;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class WeirdgeDoubleArgumentInfo implements ArgumentTypeInfo<WeirdgeDoubleArgumentType, WeirdgeDoubleArgumentInfo.Template> {
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

        byte[] buf = new byte[8];

        buffer.getBytes(buffer.writerIndex() - 8, buf);

    }

    public @NotNull Template deserializeFromNetwork(FriendlyByteBuf buffer) {

        byte b0 = buffer.readByte();
        double d0 = ArgumentUtils.numberHasMin(b0) ? buffer.readDouble() : -Double.MAX_VALUE;
        double d1 = ArgumentUtils.numberHasMax(b0) ? buffer.readDouble() : Double.MAX_VALUE;

        byte[] buf = new byte[8];

        buffer.getBytes(buffer.writerIndex() - 8, buf);

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
