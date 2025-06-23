package net.blockmath.headbash.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.CrashReportCategory;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

import static net.blockmath.headbash.commands.helpers.CommandHelpers.perms;

public class CopyPasteCommand {
    public static int requiredPermissionLevel = Commands.LEVEL_OWNERS;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("save")
                        .requires(perms(requiredPermissionLevel))
                        .then(
                                Commands.argument("path", StringArgumentType.string()).then(
                                        Commands.argument("pos1", BlockPosArgument.blockPos()).then(
                                                Commands.argument("pos2", BlockPosArgument.blockPos())
                                                        .then(
                                                                Commands.literal("noentities")
                                                                        .executes(context -> struct_save(
                                                                                context.getSource(),
                                                                                BlockPosArgument.getBlockPos(context, "pos1"),
                                                                                BlockPosArgument.getBlockPos(context, "pos2"),
                                                                                StringArgumentType.getString(context, "path"),
                                                                                false
                                                                        ))
                                                        )
                                                        .executes(context -> struct_save(
                                                                context.getSource(),
                                                                BlockPosArgument.getBlockPos(context, "pos1"),
                                                                BlockPosArgument.getBlockPos(context, "pos2"),
                                                                StringArgumentType.getString(context, "path"),
                                                                true
                                                        ))
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("load")
                        .requires(perms(requiredPermissionLevel))
                        .then(
                                Commands.argument("path", StringArgumentType.string()).then(
                                        Commands.argument("pos", BlockPosArgument.blockPos()).then(
                                                        Commands.argument("integrity", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                                .executes(context -> struct_load(
                                                                        context.getSource(),
                                                                        BlockPosArgument.getBlockPos(context, "pos"),
                                                                        StringArgumentType.getString(context, "path"),
                                                                        FloatArgumentType.getFloat(context, "integrity")
                                                                ))
                                                )
                                                .executes(context -> struct_load(
                                                        context.getSource(),
                                                        BlockPosArgument.getBlockPos(context, "pos"),
                                                        StringArgumentType.getString(context, "path"),
                                                        1.0f
                                                ))
                                )
                        )
        );
    }

    public static int struct_save(CommandSourceStack source, BlockPos pos1, BlockPos pos2, String path, boolean includeEntities) {
        BlockPos pos_min, pos_max;

        pos_min = BlockPos.min(pos1, pos2);
        pos_max = BlockPos.max(pos1, pos2);


        WeirdgeStructureBlockEntity sbe = new WeirdgeStructureBlockEntity(pos_min, StructureBlock.stateById(0));
        sbe.setMode(StructureMode.SAVE);
        sbe.setLevel(source.getUnsidedLevel());
        sbe.setStructureSize(new Vec3i(1 + pos_max.getX() - pos_min.getX(), 1 + pos_max.getY() - pos_min.getY(), 1 + pos_max.getZ() - pos_min.getZ()));
        sbe.setIgnoreEntities(!includeEntities);
        sbe.setStructureName(path);

        if (!sbe.saveStructure()) {
            source.sendFailure(Component.literal("Unable to save structure '" + path + "'"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Structure '" + path + "' saved successfully"), true);

        return Command.SINGLE_SUCCESS;
    }

    public static int struct_load(CommandSourceStack source, BlockPos pos, String path, float integrity) {
        WeirdgeStructureBlockEntity sbe = new WeirdgeStructureBlockEntity(pos, StructureBlock.stateById(0));
        sbe.setMode(StructureMode.LOAD);
        sbe.setStructureName(path);
        sbe.setIntegrity(integrity);
        sbe.setLevel(source.getUnsidedLevel());
        if (!sbe.loadStructureInfo(source.getLevel())) {
            source.sendFailure(Component.literal("Unable to load structure '" + path + "'"));
            return 0;
        }

        sbe.placeStructure(source.getLevel());

        source.sendSuccess(() -> Component.literal("Structure '" + path + "' loaded successfully"), true);

        return Command.SINGLE_SUCCESS;
    }

    public static abstract class WeirdgeBlockEntity extends AttachmentHolder implements IBlockEntityExtension {
        private static final Logger LOGGER = LogUtils.getLogger();
        private final BlockEntityType<?> type;
        @Nullable
        protected Level level;
        protected final BlockPos worldPosition;
        protected boolean remove;
        private BlockState blockState;
        private DataComponentMap components;
        @Nullable
        private CompoundTag customPersistentData;

        public WeirdgeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
            this.components = DataComponentMap.EMPTY;
            this.type = type;
            this.worldPosition = pos.immutable();
            this.blockState = blockState;
        }

        private void validateBlockState(BlockState p_353132_) {

        }

        @Nullable
        public Level getLevel() {
            return this.level;
        }

        public void setLevel(@Nullable Level level) {
            this.level = level;
        }

        public boolean hasLevel() {
            return this.level != null;
        }

        protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            if (tag.contains("NeoForgeData", 10)) {
                this.customPersistentData = tag.getCompound("NeoForgeData");
            }

            if (tag.contains("neoforge:attachments", 10)) {
                this.deserializeAttachments(registries, tag.getCompound("neoforge:attachments"));
            }

        }

        public final void loadWithComponents(CompoundTag tag, HolderLookup.Provider registries) {
            this.loadAdditional(tag, registries);
            ComponentHelper.COMPONENTS_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).resultOrPartial((p_337987_) -> LOGGER.warn("Failed to load components: {}", p_337987_)).ifPresent((p_337995_) -> this.components = p_337995_);
        }

        public final void loadCustomOnly(CompoundTag tag, HolderLookup.Provider registries) {
            this.loadAdditional(tag, registries);
        }

        protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            if (this.customPersistentData != null) {
                tag.put("NeoForgeData", this.customPersistentData.copy());
            }

            CompoundTag attachmentsTag = this.serializeAttachments(registries);
            if (attachmentsTag != null) {
                tag.put("neoforge:attachments", attachmentsTag);
            }

        }

        public final CompoundTag saveWithFullMetadata(HolderLookup.Provider registries) {
            CompoundTag compoundtag = this.saveWithoutMetadata(registries);
            this.saveMetadata(compoundtag);
            return compoundtag;
        }

        public final CompoundTag saveWithId(HolderLookup.Provider registries) {
            CompoundTag compoundtag = this.saveWithoutMetadata(registries);
            this.saveId(compoundtag);
            return compoundtag;
        }

        public final CompoundTag saveWithoutMetadata(HolderLookup.Provider registries) {
            CompoundTag compoundtag = new CompoundTag();
            this.saveAdditional(compoundtag, registries);
            ComponentHelper.COMPONENTS_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.components).resultOrPartial((p_337988_) -> LOGGER.warn("Failed to save components: {}", p_337988_)).ifPresent((p_337994_) -> compoundtag.merge((CompoundTag)p_337994_));
            return compoundtag;
        }

        public final CompoundTag saveCustomOnly(HolderLookup.Provider registries) {
            CompoundTag compoundtag = new CompoundTag();
            this.saveAdditional(compoundtag, registries);
            return compoundtag;
        }

        public final CompoundTag saveCustomAndMetadata(HolderLookup.Provider registries) {
            CompoundTag compoundtag = this.saveCustomOnly(registries);
            this.saveMetadata(compoundtag);
            return compoundtag;
        }

        private void saveId(CompoundTag tag) {
            ResourceLocation resourcelocation = BlockEntityType.getKey(this.getType());
            if (resourcelocation == null) {
                throw new RuntimeException(String.valueOf(this.getClass()) + " is missing a mapping! This is a bug!");
            } else {
                tag.putString("id", resourcelocation.toString());
            }
        }

        public static void addEntityType(CompoundTag tag, BlockEntityType<?> entityType) {
            tag.putString("id", Objects.requireNonNull(BlockEntityType.getKey(entityType)).toString());
        }

        public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
            CompoundTag compoundtag = this.saveCustomOnly(registries);
            this.removeComponentsFromTag(compoundtag);
            BlockItem.setBlockEntityData(stack, this.getType(), compoundtag);
            stack.applyComponents(this.collectComponents());
        }

        private void saveMetadata(CompoundTag tag) {
            this.saveId(tag);
            tag.putInt("x", this.worldPosition.getX());
            tag.putInt("y", this.worldPosition.getY());
            tag.putInt("z", this.worldPosition.getZ());
        }

        @Nullable
        public static net.minecraft.world.level.block.entity.BlockEntity loadStatic(BlockPos pos, BlockState state, CompoundTag tag, HolderLookup.Provider registries) {
            String s = tag.getString("id");
            ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
            if (resourcelocation == null) {
                LOGGER.error("Block entity has invalid type: {}", s);
                return null;
            } else {
                return (net.minecraft.world.level.block.entity.BlockEntity) BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(resourcelocation).map((p_155240_) -> {
                    try {
                        return p_155240_.create(pos, state);
                    } catch (Throwable throwable) {
                        LOGGER.error("Failed to create block entity {}", s, throwable);
                        return null;
                    }
                }).map((p_337992_) -> {
                    try {
                        p_337992_.loadWithComponents(tag, registries);
                        return p_337992_;
                    } catch (Throwable throwable) {
                        LOGGER.error("Failed to load data for block entity {}", s, throwable);
                        return null;
                    }
                }).orElseGet(() -> {
                    LOGGER.warn("Skipping BlockEntity with id {}", s);
                    return null;
                });
            }
        }

        public void setChanged() {
            if (this.level != null) {
                setChanged(this.level, this.worldPosition, this.blockState);
            }

        }

        protected static void setChanged(Level level, BlockPos pos, BlockState state) {
            level.blockEntityChanged(pos);
            if (!state.isAir()) {
                level.updateNeighbourForOutputSignal(pos, state.getBlock());
            }

        }

        public BlockPos getBlockPos() {
            return this.worldPosition;
        }

        public BlockState getBlockState() {
            return this.blockState;
        }

        @Nullable
        public Packet<ClientGamePacketListener> getUpdatePacket() {
            return null;
        }

        public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
            return new CompoundTag();
        }

        public boolean isRemoved() {
            return this.remove;
        }

        public void setRemoved() {
            this.remove = true;
            this.invalidateCapabilities();
            this.requestModelDataUpdate();
        }

        public void clearRemoved() {
            this.remove = false;
            this.invalidateCapabilities();
        }

        public boolean triggerEvent(int id, int type) {
            return false;
        }

        public void fillCrashReportCategory(CrashReportCategory reportCategory) {
            reportCategory.setDetail("Name", this::getNameForReporting);
            if (this.level != null) {
                CrashReportCategory.populateBlockDetails(reportCategory, this.level, this.worldPosition, this.getBlockState());
                CrashReportCategory.populateBlockDetails(reportCategory, this.level, this.worldPosition, this.level.getBlockState(this.worldPosition));
            }

        }

        private String getNameForReporting() {
            String var10000 = String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()));
            return var10000 + " // " + this.getClass().getCanonicalName();
        }

        public boolean onlyOpCanSetNbt() {
            return false;
        }

        public BlockEntityType<?> getType() {
            return this.type;
        }

        public @NotNull CompoundTag getPersistentData() {
            if (this.customPersistentData == null) {
                this.customPersistentData = new CompoundTag();
            }

            return this.customPersistentData;
        }

        @Nullable
        public final <T> T setData(@NotNull AttachmentType<T> type, @NotNull T data) {
            this.setChanged();
            return (T)super.setData(type, data);
        }

        @Nullable
        public final <T> T removeData(@NotNull AttachmentType<T> type) {
            this.setChanged();
            return (T)super.removeData(type);
        }

        /** @deprecated */
        @Deprecated
        public void setBlockState(BlockState blockState) {
            this.validateBlockState(blockState);
            this.blockState = blockState;
        }

        protected void applyImplicitComponents(DataComponentInput componentInput) {
        }

        public final void applyComponentsFromItemStack(ItemStack stack) {
            this.applyComponents(stack.getPrototype(), stack.getComponentsPatch());
        }

        public final void applyComponents(DataComponentMap components, DataComponentPatch patch) {
            final Set<DataComponentType<?>> set = new HashSet<>();
            set.add(DataComponents.BLOCK_ENTITY_DATA);
            final DataComponentMap datacomponentmap = PatchedDataComponentMap.fromPatch(components, patch);
            this.applyImplicitComponents(new DataComponentInput() {
                @Nullable
                public <T> T get(DataComponentType<T> p_338266_) {
                    set.add(p_338266_);
                    return (T)datacomponentmap.get(p_338266_);
                }

                public <T> T getOrDefault(DataComponentType<? extends T> p_338358_, T p_338352_) {
                    set.add(p_338358_);
                    return (T)datacomponentmap.getOrDefault(p_338358_, p_338352_);
                }
            });
            Objects.requireNonNull(set);
            DataComponentPatch datacomponentpatch = patch.forget(set::contains);
            this.components = datacomponentpatch.split().added();
        }

        protected void collectImplicitComponents(DataComponentMap.Builder components) {
        }

        public void removeComponentsFromTag(CompoundTag tag) {
        }

        public final DataComponentMap collectComponents() {
            DataComponentMap.Builder datacomponentmap$builder = DataComponentMap.builder();
            datacomponentmap$builder.addAll(this.components);
            this.collectImplicitComponents(datacomponentmap$builder);
            return datacomponentmap$builder.build();
        }

        public DataComponentMap components() {
            return this.components;
        }

        public void setComponents(DataComponentMap components) {
            this.components = components;
        }

        @Nullable
        public static Component parseCustomNameSafe(String customName, HolderLookup.Provider registries) {
            try {
                return Component.Serializer.fromJson(customName, registries);
            } catch (Exception exception) {
                LOGGER.warn("Failed to parse custom name from string '{}', discarding", customName, exception);
                return null;
            }
        }

        static class ComponentHelper {
            public static final Codec<DataComponentMap> COMPONENTS_CODEC;

            private ComponentHelper() {
            }

            static {
                COMPONENTS_CODEC = DataComponentMap.CODEC.optionalFieldOf("components", DataComponentMap.EMPTY).codec();
            }
        }

        protected interface DataComponentInput {
        }
    }

    private static class WeirdgeStructureBlockEntity extends WeirdgeBlockEntity {
        private static final int SCAN_CORNER_BLOCKS_RANGE = 5;
        public static final int MAX_OFFSET_PER_AXIS = 48;
        public static final int MAX_SIZE_PER_AXIS = 48;
        public static final String AUTHOR_TAG = "author";
        @Nullable
        private ResourceLocation structureName;
        private String author = "";
        private String metaData = "";
        private BlockPos structurePos = new BlockPos(0, 1, 0);
        private Vec3i structureSize;
        private Mirror mirror;
        private Rotation rotation;
        private StructureMode mode;
        private boolean ignoreEntities;
        private boolean powered;
        private boolean showAir;
        private boolean showBoundingBox;
        private float integrity;
        private long seed;

        public WeirdgeStructureBlockEntity(BlockPos pos, BlockState blockState) {
            super(BlockEntityType.STRUCTURE_BLOCK, pos, blockState);

            this.structureSize = Vec3i.ZERO;
            this.mirror = Mirror.NONE;
            this.rotation = Rotation.NONE;
            this.ignoreEntities = true;
            this.showBoundingBox = true;
            this.integrity = 1.0F;
        }

        protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.saveAdditional(tag, registries);
            tag.putString("name", this.getStructureName());
            tag.putString("author", this.author);
            tag.putString("metadata", this.metaData);
            tag.putInt("posX", this.structurePos.getX());
            tag.putInt("posY", this.structurePos.getY());
            tag.putInt("posZ", this.structurePos.getZ());
            tag.putInt("sizeX", this.structureSize.getX());
            tag.putInt("sizeY", this.structureSize.getY());
            tag.putInt("sizeZ", this.structureSize.getZ());
            tag.putString("rotation", this.rotation.toString());
            tag.putString("mirror", this.mirror.toString());
            tag.putString("mode", this.mode.toString());
            tag.putBoolean("ignoreEntities", this.ignoreEntities);
            tag.putBoolean("powered", this.powered);
            tag.putBoolean("showair", this.showAir);
            tag.putBoolean("showboundingbox", this.showBoundingBox);
            tag.putFloat("integrity", this.integrity);
            tag.putLong("seed", this.seed);
        }

        protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.loadAdditional(tag, registries);
            this.setStructureName(tag.getString("name"));
            this.author = tag.getString("author");
            this.metaData = tag.getString("metadata");
            int i = Mth.clamp(tag.getInt("posX"), -48, 48);
            int j = Mth.clamp(tag.getInt("posY"), -48, 48);
            int k = Mth.clamp(tag.getInt("posZ"), -48, 48);
            this.structurePos = new BlockPos(i, j, k);
            int l = Mth.clamp(tag.getInt("sizeX"), 0, 48);
            int i1 = Mth.clamp(tag.getInt("sizeY"), 0, 48);
            int j1 = Mth.clamp(tag.getInt("sizeZ"), 0, 48);
            this.structureSize = new Vec3i(l, i1, j1);

            try {
                this.rotation = Rotation.valueOf(tag.getString("rotation"));
            } catch (IllegalArgumentException var12) {
                this.rotation = Rotation.NONE;
            }

            try {
                this.mirror = Mirror.valueOf(tag.getString("mirror"));
            } catch (IllegalArgumentException var11) {
                this.mirror = Mirror.NONE;
            }

            try {
                this.mode = StructureMode.valueOf(tag.getString("mode"));
            } catch (IllegalArgumentException var10) {
                this.mode = StructureMode.DATA;
            }

            this.ignoreEntities = tag.getBoolean("ignoreEntities");
            this.powered = tag.getBoolean("powered");
            this.showAir = tag.getBoolean("showair");
            this.showBoundingBox = tag.getBoolean("showboundingbox");
            if (tag.contains("integrity")) {
                this.integrity = tag.getFloat("integrity");
            } else {
                this.integrity = 1.0F;
            }

            this.seed = tag.getLong("seed");
            this.updateBlockState();
        }

        private void updateBlockState() {
            if (this.level != null) {
                BlockPos blockpos = this.getBlockPos();
                BlockState blockstate = this.level.getBlockState(blockpos);
                if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
                    this.level.setBlock(blockpos, (BlockState)blockstate.setValue(StructureBlock.MODE, this.mode), 2);
                }
            }

        }

        public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
            return this.saveCustomOnly(registries);
        }


        public String getStructureName() {
            return this.structureName == null ? "" : this.structureName.toString();
        }

        public boolean hasStructureName() {
            return this.structureName != null;
        }

        public void setStructureName(@Nullable String structureName) {
            this.setStructureName(StringUtil.isNullOrEmpty(structureName) ? null : ResourceLocation.tryParse(structureName));
        }

        public void setStructureName(@Nullable ResourceLocation structureName) {
            this.structureName = structureName;
        }

        public void createdBy(LivingEntity author) {
            this.author = author.getName().getString();
        }

        public BlockPos getStructurePos() {
            return this.structurePos;
        }

        public void setStructurePos(BlockPos structurePos) {
            this.structurePos = structurePos;
        }

        public Vec3i getStructureSize() {
            return this.structureSize;
        }

        public void setStructureSize(Vec3i structureSize) {
            this.structureSize = structureSize;
        }

        public Mirror getMirror() {
            return this.mirror;
        }

        public void setMirror(Mirror mirror) {
            this.mirror = mirror;
        }

        public Rotation getRotation() {
            return this.rotation;
        }

        public void setRotation(Rotation rotation) {
            this.rotation = rotation;
        }

        public String getMetaData() {
            return this.metaData;
        }

        public void setMetaData(String metaData) {
            this.metaData = metaData;
        }

        public StructureMode getMode() {
            return this.mode;
        }

        public void setIntegrity(float integrity) { this.integrity = integrity; }

        public void setMode(StructureMode mode) {
            this.mode = mode;
        }

        public void setIgnoreEntities(boolean ignoreEntities) {
            this.ignoreEntities = ignoreEntities;
        }

        public boolean saveStructure() {
            return this.mode == StructureMode.SAVE && this.saveStructure(true);
        }

        public boolean saveStructure(boolean writeToDisk) {
            if (this.structureName == null) {
                return false;
            } else {
                BlockPos blockpos = this.getBlockPos().offset(this.structurePos);
                ServerLevel serverlevel = (ServerLevel)this.level;
                assert serverlevel != null;
                StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

                StructureTemplate structuretemplate;
                try {
                    structuretemplate = structuretemplatemanager.getOrCreate(this.structureName);
                } catch (ResourceLocationException var8) {
                    return false;
                }

                structuretemplate.fillFromWorld(this.level, blockpos, this.structureSize, !this.ignoreEntities, Blocks.STRUCTURE_VOID);
                structuretemplate.setAuthor(this.author);
                if (writeToDisk) {
                    try {
                        return structuretemplatemanager.save(this.structureName);
                    } catch (ResourceLocationException var7) {
                        return false;
                    }
                } else {
                    return true;
                }
            }
        }

        public static RandomSource createRandom(long seed) {
            return seed == 0L ? RandomSource.create(Util.getMillis()) : RandomSource.create(seed);
        }

        public boolean loadStructureInfo(ServerLevel level) {
            StructureTemplate structuretemplate = this.getStructureTemplate(level);
            if (structuretemplate == null) {
                return false;
            } else {
                this.loadStructureInfo(structuretemplate);
                return true;
            }
        }

        private void loadStructureInfo(StructureTemplate structureTemplate) {
            this.author = !StringUtil.isNullOrEmpty(structureTemplate.getAuthor()) ? structureTemplate.getAuthor() : "";
            this.structureSize = structureTemplate.getSize();
            this.setChanged();
        }

        public void placeStructure(ServerLevel level) {
            StructureTemplate structuretemplate = this.getStructureTemplate(level);
            if (structuretemplate != null) {
                this.placeStructure(level, structuretemplate);
            }

        }

        @Nullable
        private StructureTemplate getStructureTemplate(ServerLevel level) {
            return this.structureName == null ? null : (StructureTemplate)level.getStructureManager().get(this.structureName).orElse(null);
        }

        private void placeStructure(ServerLevel level, StructureTemplate structureTemplate) {
            this.loadStructureInfo(structureTemplate);
            StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings()).setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities);
            if (this.integrity < 1.0F) {
                structureplacesettings.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
            }

            BlockPos blockpos = this.getBlockPos().offset(this.structurePos);
            structureTemplate.placeInWorld(level, blockpos, blockpos, structureplacesettings, createRandom(this.seed), 2);
        }
    }

}
