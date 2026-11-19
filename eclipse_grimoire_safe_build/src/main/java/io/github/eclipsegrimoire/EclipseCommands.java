package io.github.eclipsegrimoire;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=EclipseMod.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class EclipseCommands {
    private EclipseCommands() { }
    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("eclipse")
            .then(Commands.literal("help").executes(c->help(c.getSource().getPlayerOrException())))
            .then(Commands.literal("book").executes(c->giveBook(c.getSource().getPlayerOrException())))
            .then(Commands.literal("grimoire").executes(c->giveBook(c.getSource().getPlayerOrException())))
            .then(Commands.literal("salt").requires(s->s.hasPermission(2))
                .executes(c->giveSalt(c.getSource().getPlayerOrException(),32))
                .then(Commands.argument("count",IntegerArgumentType.integer(1,64))
                    .executes(c->giveSalt(c.getSource().getPlayerOrException(),IntegerArgumentType.getInteger(c,"count")))))
            .then(Commands.literal("mana").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();say(p,"Mana: "+MagicData.mana(p)+"/100");return 1;})
                .then(Commands.literal("refill").requires(source->source.hasPermission(2)).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();MagicData.mana(p,100);say(p,"Mana restored.");return 1;})))
            .then(Commands.literal("cast").then(Commands.argument("spell",StringArgumentType.word())
                .executes(c->SpellCasting.cast(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"spell")))))
            .then(Commands.literal("rename").then(Commands.argument("name",StringArgumentType.greedyString())
                .executes(c->rename(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name")))))
            .then(Commands.literal("color")
                .then(Commands.literal("magic").then(Commands.argument("color",StringArgumentType.word())
                    .executes(c->setColor(c.getSource().getPlayerOrException(),"magicColor",StringArgumentType.getString(c,"color")))))
                .then(Commands.literal("book").then(Commands.argument("color",StringArgumentType.word())
                    .executes(c->setColor(c.getSource().getPlayerOrException(),"uiColor",StringArgumentType.getString(c,"color"))))))
            .then(Commands.literal("coven")
                .then(Commands.literal("create").then(Commands.argument("name",StringArgumentType.greedyString())
                    .executes(c->createCoven(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name")))))
                .then(Commands.literal("invite").then(Commands.argument("player",EntityArgument.player())
                    .executes(c->invite(c.getSource().getPlayerOrException(),EntityArgument.getPlayer(c,"player")))))
                .then(Commands.literal("join").executes(c->join(c.getSource().getPlayerOrException())))
                .then(Commands.literal("leave").executes(c->leave(c.getSource().getPlayerOrException())))
                .then(Commands.literal("info").executes(c->info(c.getSource().getPlayerOrException()))))
            .then(Commands.literal("familiar")
                .then(Commands.literal("bond").executes(c->bond(c.getSource().getPlayerOrException())))));
    }
    private static void say(ServerPlayer p,String s) {p.sendSystemMessage(Component.literal("[Eclipse] "+s));}
    private static int help(ServerPlayer p) {
        say(p,"Commands: /eclipse book, mana, cast ward|power|thunder|aegis");
        say(p,"/eclipse rename <name>, color magic|book <purple|crimson|blue|emerald|gold|white|shadow|rose>");
        say(p,"/eclipse coven create|invite|join|leave|info; familiar bond; OP: /eclipse salt [count], mana refill");return 1;
    }
    private static int giveBook(ServerPlayer p) {
        ItemStack book=MagicData.newBook(p);
        if(!p.getInventory().add(book)) p.drop(book,false);
        say(p,"Your personal grimoire has been granted.");return 1;
    }
    private static int giveSalt(ServerPlayer p,int count) {
        ItemStack salt=new ItemStack(EclipseMod.SALT.get(),count);
        if(!p.getInventory().add(salt)) p.drop(salt,false);
        say(p,"Granted "+count+" salt.");return 1;
    }
    private static int rename(ServerPlayer p,String name) {
        name=name.trim();
        if(name.length()<2 || name.length()>32) {say(p,"Names must be 2-32 characters.");return 0;}
        MagicData.data(p).putString("name",name);MagicData.refreshBooks(p);
        say(p,"Grimoire renamed to "+name);return 1;
    }
    private static int setColor(ServerPlayer p,String key,String color) {
        color=color.toLowerCase(java.util.Locale.ROOT);
        if(!MagicData.COLORS.contains(color)) {say(p,"Colors: "+String.join(", ",MagicData.COLORS));return 0;}
        MagicData.data(p).putString(key,color);MagicData.refreshBooks(p);
        say(p,"Updated "+key+" to "+color+".");return 1;
    }
    private static CovenData covens(ServerPlayer p){return CovenData.get(p.serverLevel());}
    private static int createCoven(ServerPlayer p,String name) {
        if(!covens(p).create(p.getUUID(),name)) {say(p,"Could not create: already in coven, invalid name, or name taken.");return 0;}
        say(p,"Coven created: "+covens(p).of(p.getUUID()));return 1;
    }
    private static int invite(ServerPlayer p,ServerPlayer target) {
        if(!covens(p).invite(p.getUUID(),target.getUUID())) {say(p,"Invite failed (you need a coven; target must be unassigned).");return 0;}
        say(p,"Invited "+target.getGameProfile().getName());
        say(target,"You were invited to "+covens(p).of(p.getUUID())+". Type /eclipse coven join");return 1;
    }
    private static int join(ServerPlayer p) {
        if(!covens(p).join(p.getUUID())) {say(p,"You need a valid pending invitation.");return 0;}
        say(p,"Joined "+covens(p).of(p.getUUID()));return 1;
    }
    private static int leave(ServerPlayer p) {
        if(!covens(p).leave(p.getUUID())) {say(p,"You aren't in a coven.");return 0;}
        say(p,"Left your coven.");return 1;
    }
    private static int info(ServerPlayer p) {
        String name=covens(p).of(p.getUUID());
        say(p,name.isEmpty()?"You have no coven.":"Coven: "+name+" ("+covens(p).count(name)+" members)");return 1;
    }
    private static int bond(ServerPlayer p) {
        Vec3 eye=p.getEyePosition();
        TamableAnimal best=null;double bestDistance=Double.MAX_VALUE;
        for(TamableAnimal pet:p.serverLevel().getEntitiesOfClass(TamableAnimal.class,p.getBoundingBox().inflate(6))) {
            if(!pet.isTame() || !p.getUUID().equals(pet.getOwnerUUID())) continue;
            Vec3 toward=pet.getBoundingBox().getCenter().subtract(eye);
            double d=toward.lengthSqr();
            if(d>36 || d<.001 || toward.normalize().dot(p.getLookAngle())<.55) continue;
            if(d<bestDistance){best=pet;bestDistance=d;}
        }
        if(best==null){say(p,"Look toward your OWN tamed pet within 6 blocks, then retry.");return 0;}
        best.getPersistentData().putUUID("EclipseFamiliarOwner",p.getUUID());
        say(p,"Bonded familiar: "+best.getName().getString()+". Your magic will spare it.");return 1;
    }
}
