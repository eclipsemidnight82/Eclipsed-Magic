package io.github.eclipsegrimoire;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/** Uses vanilla potions, particle dust, and visual-only lightning; deals spell damage directly. */
public final class SpellCasting {
    private SpellCasting() { }
    public static int cast(ServerPlayer player,String spell) {
        int cost,delay;
        switch(spell) {
            case "ward" -> {cost=20;delay=20;}
            case "power" -> {cost=30;delay=30;}
            case "thunder" -> {cost=70;delay=100;}
            case "aegis" -> {cost=100;delay=180;}
            default -> {msg(player,"Unknown spell. Try ward, power, thunder, aegis.");return 0;}
        }
        ServerLevel level=player.serverLevel();
        long now=level.getGameTime();
        long ready=MagicData.data(player).getLong("ready_"+spell);
        if(now<ready) {msg(player,"Spell is cooling down: "+((ready-now+19)/20)+"s");return 0;}
        if(!MagicData.spend(player,cost)) {msg(player,"Not enough mana. Current: "+MagicData.mana(player)+" / 100");return 0;}
        MagicData.data(player).putLong("ready_"+spell,now+20L*delay);
        switch(spell) {
            case "ward" -> {
                protect(player,600);
                allies(player,16).forEach(ally->protect(ally,600));
                protectPets(player,16,600);
                msg(player,"A protective ward surrounds you and nearby allies.");
            }
            case "power" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,600,1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,600,0));
                msg(player,"Your magical power surges.");
            }
            case "thunder" -> {
                attack(player,12,12f,8,true);
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,200,0));
                msg(player,"ALZUR'S THUNDER! You feel drained.");
            }
            case "aegis" -> {
                protect(player,800); allies(player,20).forEach(ally->protect(ally,800));
                protectPets(player,20,800);
                attack(player,16,16f,12,true);
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,500,1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,200,0));
                msg(player,"The Final Aegis! Its power exacts a price.");
            }
        }
        dust(player,65);
        level.playSound(null,player.blockPosition(),SoundEvents.EVOKER_CAST_SPELL,SoundSource.PLAYERS,1f,.65f);
        return 1;
    }

    private static void protect(LivingEntity entity,int ticks) {
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,ticks,1));
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,ticks,1));
    }
    private static void protectPets(ServerPlayer player,double radius,int ticks) {
        for(TamableAnimal pet:player.serverLevel().getEntitiesOfClass(TamableAnimal.class, player.getBoundingBox().inflate(radius))) {
            if(protectedFrom(player,pet)) protect(pet,ticks);
        }
    }
    private static List<ServerPlayer> allies(ServerPlayer p,double r) {
        CovenData covens=CovenData.get(p.serverLevel());
        return p.serverLevel().getEntitiesOfClass(ServerPlayer.class,p.getBoundingBox().inflate(r),other->other!=p && covens.same(p.getUUID(),other.getUUID()));
    }
    public static boolean protectedFrom(ServerPlayer p,LivingEntity target) {
        if(target==p) return true;
        CovenData covens=CovenData.get(p.serverLevel());
        if(target instanceof ServerPlayer sp) return covens.same(p.getUUID(),sp.getUUID());
        if(target instanceof TamableAnimal pet && pet.isTame() && pet.getOwnerUUID()!=null) {
            UUID owner=pet.getOwnerUUID();
            return owner.equals(p.getUUID()) || covens.same(p.getUUID(),owner);
        }
        if(target.getPersistentData().hasUUID("EclipseFamiliarOwner")) {
            UUID owner=target.getPersistentData().getUUID("EclipseFamiliarOwner");
            return owner.equals(p.getUUID()) || covens.same(p.getUUID(),owner);
        }
        return false;
    }
    private static void attack(ServerPlayer p,double range,float damage,int max,boolean lightning) {
        ServerLevel level=p.serverLevel();int count=0;
        List<LivingEntity> targets=level.getEntitiesOfClass(LivingEntity.class,new AABB(p.blockPosition()).inflate(range),
            target->target.isAlive() && target!=p && !protectedFrom(p,target)
            && (target instanceof Enemy || (target instanceof ServerPlayer && p.server.isPvpAllowed())));
        targets.sort((a,b)->Double.compare(a.distanceToSqr(p),b.distanceToSqr(p)));
        for(LivingEntity target:targets) {
            if(count++ >= max) break;
            target.hurt(p.damageSources().magic(),damage);
            if(lightning) {
                LightningBolt bolt=EntityType.LIGHTNING_BOLT.create(level);
                if(bolt!=null) {
                    bolt.moveTo(target.position());
                    bolt.setVisualOnly(true); // protects allies from vanilla lightning collateral and fire
                    level.addFreshEntity(bolt);
                }
            }
        }
    }
    public static void dust(ServerPlayer player,int count) {
        int color=switch(MagicData.magic(player)) {
            case "crimson" -> 0xe34e69; case "blue" -> 0x54a6ef; case "emerald" -> 0x43bd94;
            case "gold" -> 0xe6b74e; case "white" -> 0xe7dfe9; case "shadow" -> 0x5b526b;
            case "rose" -> 0xed91cb; default -> 0xa46af5;
        };
        Vector3f rgb=new Vector3f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f);
        player.serverLevel().sendParticles(new DustParticleOptions(rgb,1.4f),player.getX(),player.getY()+1,player.getZ(),count,1.0,1.0,1.0,.06);
    }
    private static void msg(ServerPlayer p,String s){p.sendSystemMessage(Component.literal(s));}
}
