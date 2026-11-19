package io.github.eclipsegrimoire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Reuse the existing vanilla lectern, water cauldron and door blocks. */
@Mod.EventBusSubscriber(modid=EclipseMod.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class MagicInteractions {
    private MagicInteractions() {}

    @SubscribeEvent public static void blockUsed(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);
        ItemStack held = event.getItemStack();

        if (state.is(Blocks.LECTERN)) {
            if (held.is(EclipseMod.GRIMOIRE.get()) && !state.getValue(LecternBlock.HAS_BOOK)) {
                // Keep client interaction uncancelled so the normal use-on-block packet reaches the server.
                if (!event.getLevel().isClientSide()) {
                    handled(event);
                    ServerLevel level = (ServerLevel) event.getLevel();
                    ServerPlayer player = (ServerPlayer) event.getEntity();
                    ItemStack one = held.copy(); one.setCount(1);
                    if (LecternBlock.tryPlaceBook(player, level, pos, state, one)) {
                        if (!player.isCreative()) held.shrink(1);
                        msg(player, "Grimoire placed on the lectern. Right-click to read; sneak-right-click to reclaim.");
                    }
                }
                return;
            }
            if (state.getValue(LecternBlock.HAS_BOOK) && event.getLevel().getBlockEntity(pos) instanceof LecternBlockEntity lectern
                && lectern.getBook().is(EclipseMod.GRIMOIRE.get())) {
                // ClientEvents opens the custom UI. Never cancel on client when retrieving,
                // otherwise the packet needed for the server to return the book may be suppressed.
                if (!event.getLevel().isClientSide()) {
                    handled(event);
                    if (event.getEntity().isShiftKeyDown()) {
                    ServerPlayer player = (ServerPlayer) event.getEntity();
                    ItemStack book = lectern.getBook().copy();
                    lectern.setBook(ItemStack.EMPTY);
                    event.getLevel().setBlock(pos, state.setValue(LecternBlock.HAS_BOOK, false), 3);
                    if (!player.getInventory().add(book)) player.drop(book, false);
                    msg(player, "Grimoire removed from lectern.");
                    }
                }
                return;
            }
        }
        if (state.getBlock() instanceof DoorBlock && held.is(EclipseMod.SALT.get())) {
            if (!event.getLevel().isClientSide()) {
                handled(event);
                ServerLevel level = (ServerLevel) event.getLevel();
                ServerPlayer player = (ServerPlayer) event.getEntity();
                WardData wards = WardData.get(level);
                if (player.isShiftKeyDown()) {
                    msg(player, wards.clearDoor(level, pos, player.getUUID(), player.createCommandSourceStack().hasPermission(2))
                        ? "Salt ward removed from door." : "This door has no ward belonging to you.");
                } else if (wards.saltDoor(level, pos, player.getUUID())) {
                    if (!player.isCreative()) held.shrink(1);
                    level.sendParticles(ParticleTypes.END_ROD, pos.getX()+.5, pos.getY()+.7, pos.getZ()+.5, 20, .4, .8, .4, .01);
                    level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, .8f, 1.3f);
                    msg(player, "Salted door! Undead cannot pass through the doorway even when it is open.");
                } else msg(player, "This door is already protected by salt.");
            }
            return;
        }
        if (state.is(Blocks.WATER_CAULDRON) && (held.is(Items.AMETHYST_SHARD) || held.is(Items.GLOWSTONE_DUST) || held.is(Items.BLAZE_POWDER))) {
            if (!event.getLevel().isClientSide()) {
                handled(event);
                ServerLevel level = (ServerLevel) event.getLevel();
                ServerPlayer player = (ServerPlayer) event.getEntity();
                if (held.is(Items.AMETHYST_SHARD)) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0));
                    for (ServerPlayer ally : level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(8)))
                        if (SpellCasting.protectedFrom(player, ally)) {
                            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
                            ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0));
                        }
                    msg(player, "Cauldron: Blessing of the Waters. Nearby coven members are healed.");
                } else if (held.is(Items.GLOWSTONE_DUST)) {
                    MagicData.mana(player, MagicData.mana(player)+40);
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 2400, 0));
                    msg(player, "Cauldron: Arcane Infusion. Restored 40 mana and granted night vision.");
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600, 0));
                    for (ServerPlayer ally : level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(8)))
                        if (SpellCasting.protectedFrom(player, ally)) ally.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600, 0));
                    for (TamableAnimal pet : level.getEntitiesOfClass(TamableAnimal.class, player.getBoundingBox().inflate(8)))
                        if (SpellCasting.protectedFrom(player, pet)) pet.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600, 0));
                    msg(player, "Cauldron: Ember Ward. Nearby coven members and familiars resist fire.");
                }
                if (!player.isCreative()) held.shrink(1);
                int water = state.getValue(LayeredCauldronBlock.LEVEL);
                level.setBlock(pos, water<=1 ? Blocks.CAULDRON.defaultBlockState() : state.setValue(LayeredCauldronBlock.LEVEL, water-1), 3);
                level.sendParticles(ParticleTypes.ENCHANT, pos.getX()+.5, pos.getY()+1.0, pos.getZ()+.5, 30, .5, .25, .5, .2);
                level.playSound(null,pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1, 1.1f);
            }
        }
    }

    @SubscribeEvent public static void brokenBlock(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        WardData.get(level).broken(level, event.getPos());
    }
    private static void handled(PlayerInteractEvent.RightClickBlock event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
    }
    private static void msg(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("[Eclipse] " + text));
    }
}
