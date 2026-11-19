package io.github.eclipsegrimoire;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.saveddata.SavedData;

/** Lightweight, per-dimension saved positions of enchanted salt and salted vanilla doors. */
public final class WardData extends SavedData {
    private final Set<Long> circleSalt = new HashSet<>();
    private final Map<Long, UUID> saltedDoors = new HashMap<>();

    public static WardData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(WardData::load, WardData::new, "eclipse_salt_wards");
    }
    private static WardData load(CompoundTag tag) {
        WardData data = new WardData();
        for (long key : tag.getLongArray("circle_salt")) data.circleSalt.add(key);
        for (Tag item : tag.getList("doors", Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) item;
            if (record.hasUUID("owner")) data.saltedDoors.put(record.getLong("pos"), record.getUUID("owner"));
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("circle_salt", circleSalt.stream().mapToLong(Long::longValue).toArray());
        ListTag doors = new ListTag();
        saltedDoors.forEach((pos, owner) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("pos", pos);
            entry.putUUID("owner", owner);
            doors.add(entry);
        });
        tag.put("doors", doors);
        return tag;
    }
    public void enchantCircle(Set<BlockPos> positions) {
        for (BlockPos pos : positions) circleSalt.add(pos.asLong());
        setDirty();
    }
    public boolean isCharged(ServerLevel level, BlockPos pos) {
        if (!circleSalt.contains(pos.asLong())) return false;
        if (level.getBlockState(pos).is(EclipseMod.SALT_LINE.get())) return true;
        circleSalt.remove(pos.asLong()); // flowing water, lava, or replacement broke the ward
        setDirty();
        return false;
    }
    public static BlockPos doorBase(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof DoorBlock)) return null;
        return state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }
    public boolean isSaltedDoor(ServerLevel level, BlockPos pos) {
        BlockPos base = doorBase(level, pos);
        if (base == null) return false;
        return saltedDoors.containsKey(base.asLong());
    }
    public boolean saltDoor(ServerLevel level, BlockPos pos, UUID owner) {
        BlockPos base = doorBase(level, pos);
        if (base == null || saltedDoors.containsKey(base.asLong())) return false;
        saltedDoors.put(base.asLong(), owner);
        setDirty();
        return true;
    }
    public boolean clearDoor(ServerLevel level, BlockPos pos, UUID player, boolean operator) {
        BlockPos base = doorBase(level, pos);
        if (base == null) return false;
        UUID owner = saltedDoors.get(base.asLong());
        if (owner == null || (!operator && !owner.equals(player))) return false;
        saltedDoors.remove(base.asLong());
        setDirty();
        return true;
    }
    public void broken(ServerLevel level, BlockPos pos) {
        if (circleSalt.remove(pos.asLong())) setDirty();
        BlockPos base = doorBase(level, pos);
        if (base != null && saltedDoors.remove(base.asLong()) != null) setDirty();
    }
}
