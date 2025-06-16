package net.blockmath.headbash.commands.helpers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.blockmath.headbash.HeadBashCommands;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;


public class AttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HeadBashCommands.MODID);

    public static final Codec<AttBlockPos> ATT_BLOCK_POS_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("x").forGetter(AttBlockPos::getX),
                    Codec.DOUBLE.fieldOf("y").forGetter(AttBlockPos::getY),
                    Codec.DOUBLE.fieldOf("z").forGetter(AttBlockPos::getZ)
            ).apply(instance, AttBlockPos::new));

    public static final Supplier<AttachmentType<AttBlockPos>> HOME_POS = ATTACHMENT_TYPES.register(
            "homepos", () -> AttachmentType.builder(() -> new AttBlockPos(Double.NaN, Double.NaN, Double.NaN)).serialize(ATT_BLOCK_POS_CODEC).copyOnDeath().build()
    );

    public static final Supplier<AttachmentType<AttBlockPos>> BACK_POS = ATTACHMENT_TYPES.register(
            "backpos", () -> AttachmentType.builder(() -> new AttBlockPos(Double.NaN, Double.NaN, Double.NaN)).serialize(ATT_BLOCK_POS_CODEC).copyOnDeath().build()
    );

    public static class AttBlockPos {
        public double x, y, z;
        public AttBlockPos(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }

        public boolean isValid() {
            return !(Double.isNaN(x) && Double.isNaN(y) && Double.isNaN(z));
        }
    }

}
