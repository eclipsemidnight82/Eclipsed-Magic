package io.github.eclipsegrimoire.client;

import io.github.eclipsegrimoire.MagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Four compact pages, using vanilla GUI widgets and standard /eclipse commands. */
public final class GrimoireScreen extends Screen {
    private enum Page { SPELLS, COVEN, FAMILIARS, SETTINGS }
    private Page page=Page.SPELLS;
    private final String name;
    private String magicColor,uiColor;
    private EditBox nameInput;
    private static final String[] COLORS={"purple","crimson","blue","emerald","gold","white","shadow","rose"};

    public GrimoireScreen(ItemStack book) {
        super(Component.literal("The Eclipse Grimoire"));
        var nbt=book.getTag();
        name=nbt!=null&&nbt.contains("eclipseName")?nbt.getString("eclipseName"):MagicData.DEFAULT_BOOK;
        magicColor=nbt!=null&&nbt.contains("eclipseMagic")?nbt.getString("eclipseMagic"):"purple";
        uiColor=nbt!=null&&nbt.contains("eclipseUI")?nbt.getString("eclipseUI"):"shadow";
    }
    private int accent() {return 0xff000000|switch(uiColor){
        case "purple"->0xa46af5;case "crimson"->0xe34e69;case "blue"->0x54a6ef;
        case "emerald"->0x43bd94;case "gold"->0xe6b74e;case "white"->0xe7dfe9;
        case "rose"->0xed91cb;default->0x5b526b;
    };}
    @Override protected void init() {
        int x=width/2-155,y=height/2-104;
        String[] labels={"Spells","Coven","Familiars","Settings"};
        Page[] pages=Page.values();
        for(int i=0;i<4;i++) {
            final Page selected=pages[i];
            addRenderableWidget(Button.builder(Component.literal(labels[i]),b->switchPage(selected)).bounds(x+9+i*76,y+30,72,20).build());
        }
        switch(page) {
            case SPELLS -> {
                spellButton("Ward  [20 mana]","ward",x+15,y+69);
                spellButton("Power  [30 mana]","power",x+160,y+69);
                spellButton("Thunder [70 mana]","thunder",x+15,y+107);
                spellButton("Final Aegis [100]","aegis",x+160,y+107);
            }
            case SETTINGS -> {
                nameInput=new EditBox(font,x+16,y+73,278,20,Component.literal("Grimoire name"));
                nameInput.setMaxLength(32);nameInput.setValue(name);addRenderableWidget(nameInput);
                addRenderableWidget(Button.builder(Component.literal("Save Name"),b->{command("eclipse rename "+nameInput.getValue());close();}).bounds(x+16,y+97,278,20).build());
                addRenderableWidget(Button.builder(Component.literal("Magic: "+magicColor),b->{magicColor=nextColor(magicColor);command("eclipse color magic "+magicColor);switchPage(Page.SETTINGS);}).bounds(x+16,y+129,135,20).build());
                addRenderableWidget(Button.builder(Component.literal("Book: "+uiColor),b->{uiColor=nextColor(uiColor);command("eclipse color book "+uiColor);switchPage(Page.SETTINGS);}).bounds(x+158,y+129,135,20).build());
            }
            case COVEN, FAMILIARS -> { }
        }
        addRenderableWidget(Button.builder(Component.literal("Close"),b->close()).bounds(x+117,y+180,75,20).build());
    }
    private void spellButton(String label,String spell,int x,int y) {
        addRenderableWidget(Button.builder(Component.literal(label),b->{command("eclipse cast "+spell);close();}).bounds(x,y,135,23).build());
    }
    private String nextColor(String now) {
        for(int i=0;i<COLORS.length;i++) if(COLORS[i].equals(now)) return COLORS[(i+1)%COLORS.length];
        return COLORS[0];
    }
    private void command(String command) {
        if(Minecraft.getInstance().player!=null && Minecraft.getInstance().player.connection!=null)
            Minecraft.getInstance().player.connection.sendCommand(command);
    }
    private void close() {Minecraft.getInstance().setScreen(null);}
    private void switchPage(Page p) {page=p;clearWidgets();init();}
    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
        renderBackground(g,mx,my,partial);
        int x=width/2-155,y=height/2-104;
        g.fill(x,y,x+310,y+211,0xff141019);
        g.fill(x+5,y+5,x+305,y+206,0xff231b2c);
        g.fill(x+5,y+5,x+305,y+7,accent());
        g.drawCenteredString(font,name,width/2,y+13,0xffeee2fa);
        super.render(g,mx,my,partial);
        switch(page) {
            case SPELLS -> {
                g.drawCenteredString(font,"Born with magic. Choose a spell:",width/2,y+57,0xffcdb9da);
                g.drawCenteredString(font,"Magic color: "+magicColor,width/2,y+152,0xffe7d7f6);
            }
            case COVEN -> {
                text(g,"Create: /eclipse coven create <name>",x+19,y+73);
                text(g,"Invite: /eclipse coven invite <player>",x+19,y+90);
                text(g,"Accept: /eclipse coven join",x+19,y+107);
                text(g,"Leave: /eclipse coven leave",x+19,y+124);
                text(g,"Info: /eclipse coven info",x+19,y+141);
            }
            case FAMILIARS -> {
                text(g,"Tame a pet, look at it, then type:",x+19,y+75);
                text(g,"/eclipse familiar bond",x+19,y+96);
                text(g,"Your spells spare your pets and coven.",x+19,y+119);
            }
            case SETTINGS -> {
                text(g,"Rename and customize your magic:",x+19,y+59);
                text(g,"Colors update your book and particles.",x+19,y+160);
            }
        }
    }
    private void text(GuiGraphics g,String text,int x,int y){g.drawString(font,text,x,y,0xffe1d1ee);}
    @Override public boolean isPauseScreen() {return false;}
}
