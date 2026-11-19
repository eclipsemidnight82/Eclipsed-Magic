package io.github.eclipsegrimoire;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=EclipseMod.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class EclipseEvents {
    private EclipseEvents() {}
    // Weak keys avoid retaining mob entities after they despawn or chunks unload.
    private static final Map<Mob, Vec3> PREVIOUS = new WeakHashMap<>();

    @SubscribeEvent public static void playerTick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if(player.tickCount%20==0 && MagicData.mana(player)<100) MagicData.mana(player,MagicData.mana(player)+2);
    }
    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event) {
        CompoundTag oldData=event.getOriginal().getPersistentData();
        if(oldData.contains(MagicData.KEY,10)) {
            event.getEntity().getPersistentData().put(MagicData.KEY,oldData.getCompound(MagicData.KEY).copy());
        }
    }
    /** Stops a walking/jumping undead mob at the occupied salt tile, even if its AI wants to cross. */
    @SubscribeEvent public static void undeadTick(LivingEvent.LivingTickEvent event) {
        if(!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        if(mob.getMobType()!=MobType.UNDEAD && !mob.getType().is(EclipseMod.EXTRA_UNDEAD)) return;
        Vec3 current=mob.position();
        Vec3 previous=PREVIOUS.put(mob,current);
        if(previous==null) return;
        double length=current.distanceTo(previous);
        if(length>24) return; // Teleport/respawn safety; normal movement is checked in small steps.
        int steps=Math.max(1,(int)Math.ceil(length*5));
        for(int i=1;i<=steps;i++) {
            Vec3 point=previous.lerp(current,(double)i/steps);
            if(touchesSalt(level,point)) {
                mob.setPos(previous.x,previous.y,previous.z);
                mob.setDeltaMovement(0,Math.min(0,mob.getDeltaMovement().y),0);
                PREVIOUS.put(mob,previous);
                if(mob.tickCount%12==0) level.sendParticles(ParticleTypes.END_ROD,point.x,point.y+.15,point.z,2,.15,.1,.15,.01);
                break;
            }
        }
    }
    private static boolean touchesSalt(ServerLevel level,Vec3 point) {
        BlockPos feet=BlockPos.containing(point.x,point.y+.05,point.z);
        for(int dy=0;dy>=-2;dy--) {
            if(level.getBlockState(feet.offset(0,dy,0)).is(EclipseMod.SALT_LINE.get())) return true;
        }
        return false;
    }
}
