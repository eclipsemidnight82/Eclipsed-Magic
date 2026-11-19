package io.github.eclipsegrimoire;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Minimal server-owned persistent coven memberships and invitations. */
public final class CovenData extends SavedData {
    private final Map<UUID,String> members=new HashMap<>();
    private final Map<UUID,String> invitations=new HashMap<>();

    public static CovenData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(CovenData::load, CovenData::new, "eclipse_covens");
    }
    private static CovenData load(CompoundTag tag) {
        CovenData data=new CovenData();
        for(Tag entry: tag.getList("members",10)) {
            CompoundTag record=(CompoundTag)entry;
            if(record.hasUUID("id")) data.members.put(record.getUUID("id"),record.getString("coven"));
        }
        for(Tag entry: tag.getList("invitations",10)) {
            CompoundTag record=(CompoundTag)entry;
            if(record.hasUUID("id")) data.invitations.put(record.getUUID("id"),record.getString("coven"));
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        tag.put("members",serialize(members));
        tag.put("invitations",serialize(invitations));
        return tag;
    }
    private static ListTag serialize(Map<UUID,String> map) {
        ListTag all=new ListTag();
        map.forEach((uuid,name)->{CompoundTag entry=new CompoundTag();entry.putUUID("id",uuid);entry.putString("coven",name);all.add(entry);});
        return all;
    }
    public String of(UUID id) {return members.getOrDefault(id,"");}
    public boolean same(UUID a,UUID b) {return !of(a).isEmpty() && of(a).equals(of(b));}
    public boolean create(UUID player,String name) {
        name=name.trim().toLowerCase(Locale.ROOT);
        if(!of(player).isEmpty() || name.length()<2 || name.length()>24 || !name.matches("[a-z0-9_ -]+") || members.containsValue(name)) return false;
        members.put(player,name);setDirty();return true;
    }
    public boolean invite(UUID from,UUID to) {
        String coven=of(from);
        if(coven.isEmpty() || !of(to).isEmpty()) return false;
        invitations.put(to,coven);setDirty();return true;
    }
    public boolean join(UUID player) {
        String coven=invitations.remove(player);
        if(coven==null || !of(player).isEmpty() || !members.containsValue(coven)) return false;
        members.put(player,coven);setDirty();return true;
    }
    public boolean leave(UUID player) {
        boolean existed=members.remove(player)!=null;
        if(existed) setDirty();
        return existed;
    }
    public int count(String coven) {return (int)members.values().stream().filter(coven::equals).count();}
}
