package io.github.eclipsegrimoire;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

/** Per-player persistent magical preferences and mana; stamps settings onto books for client UI. */
public final class MagicData {
    private MagicData() {}
    public static final String KEY="EclipseGrimoire";
    public static final String DEFAULT_BOOK="The Eclipse Grimoire";
    public static final List<String> COLORS=List.of("purple","crimson","blue","emerald","gold","white","shadow","rose");

    public static CompoundTag data(ServerPlayer player) {
        CompoundTag root=player.getPersistentData();
        if(!root.contains(KEY,10)) {
            CompoundTag tag=new CompoundTag();
            tag.putInt("mana",100); tag.putString("name",DEFAULT_BOOK);
            tag.putString("magicColor","purple"); tag.putString("uiColor","shadow");
            root.put(KEY,tag);
        }
        return root.getCompound(KEY);
    }
    public static int mana(ServerPlayer p) {return data(p).getInt("mana");}
    public static void mana(ServerPlayer p,int m) {data(p).putInt("mana",Math.max(0,Math.min(100,m)));}
    public static boolean spend(ServerPlayer p,int cost) {
        if (p.isCreative()) return true;
        if(mana(p)<cost) return false;
        mana(p,mana(p)-cost);return true;
    }
    public static String name(ServerPlayer p) {return data(p).getString("name");}
    public static String magic(ServerPlayer p) {return data(p).getString("magicColor");}
    public static String ui(ServerPlayer p) {return data(p).getString("uiColor");}
    public static ItemStack newBook(ServerPlayer player) {
        ItemStack stack=new ItemStack(EclipseMod.GRIMOIRE.get()); stamp(stack,player); return stack;
    }
    private static void stamp(ItemStack stack,ServerPlayer player) {
        stack.getOrCreateTag().putString("eclipseName",name(player));
        stack.getOrCreateTag().putString("eclipseMagic",magic(player));
        stack.getOrCreateTag().putString("eclipseUI",ui(player));
        stack.setHoverName(Component.literal(name(player)));
    }
    public static void refreshBooks(ServerPlayer player) {
        for(int i=0;i<player.getInventory().getContainerSize();i++) {
            ItemStack item=player.getInventory().getItem(i);
            if(item.is(EclipseMod.GRIMOIRE.get())) stamp(item,player);
        }
        player.getInventory().setChanged();
    }
}
