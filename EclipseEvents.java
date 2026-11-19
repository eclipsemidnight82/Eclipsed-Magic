package io.github.eclipsegrimoire;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.DoorBlock;
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
    private static final Map<LivingEntity, Vec3> PREVIOUS = new WeakHashMap<>();

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
    /** Ordinary salt blocks undead; spell-conjured salt circles block ALL living creatures.
     * Movement checks are server-side and do not alter ordinary Minecraft collision blocks. */
    @SubscribeEvent public static void saltBoundaryTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity=event.getEntity();
        if(!(entity.level() instanceof ServerLevel level)) return;
        if(entity.isSpectator()) return;
        Vec3 current=entity.position();
        Vec3 previous=PREVIOUS.put(entity,current);
        if(previous==null) return;
        double length=current.distanceTo(previous);
        if(length>16) return; // do not interfere with commands, world teleportation or portal travel
        boolean undead=entity.getMobType()==MobType.UNDEAD || entity.getType().is(EclipseMod.EXTRA_UNDEAD);
        WardData wards=WardData.get(level);
        int steps=Math.max(1,(int)Math.ceil(length*6));
        for(int i=1;i<=steps;i++) {
            Vec3 point=previous.lerp(current,(double)i/steps);
            if(touchesChargedSalt(level,wards,point) || (undead && (touchesOrdinarySalt(level,point) || touchesSaltedDoor(level,wards,point)))) {
                if(entity instanceof ServerPlayer player) player.connection.teleport(previous.x,previous.y,previous.z,player.getYRot(),player.getXRot());
                else entity.setPos(previous.x,previous.y,previous.z);
                entity.setDeltaMovement(0,Math.min(0,entity.getDeltaMovement().y),0);
                PREVIOUS.put(entity,previous);
                if(entity.tickCount%12==0) level.sendParticles(ParticleTypes.END_ROD,point.x,point.y+.15,point.z,3,.15,.25,.15,.01);
                break;
            }
        }
    }
    private static boolean touchesChargedSalt(ServerLevel level,WardData wards,Vec3 point) {
        BlockPos at=BlockPos.containing(point.x,point.y,point.z);
        // Check a vertical ward wall reaching 5 blocks above its salt line.
        for(int dy=0;dy>=-5;dy--) if(wards.isCharged(level,at.offset(0,dy,0))) return true;
        return false;
    }
    private static boolean touchesOrdinarySalt(ServerLevel level,Vec3 point) {
        BlockPos at=BlockPos.containing(point.x,point.y+.05,point.z);
        for(int dy=0;dy>=-2;dy--)
            if(level.getBlockState(at.offset(0,dy,0)).is(EclipseMod.SALT_LINE.get())) return true;
        return false;
    }
    private static boolean touchesSaltedDoor(ServerLevel level,WardData wards,Vec3 point) {
        BlockPos at=BlockPos.containing(point.x,point.y+.05,point.z);
        for(int dy=-2;dy<=1;dy++) if(wards.isSaltedDoor(level,at.offset(0,dy,0))) return true;
        return false;
    }
}
