package io.github.eclipsegrimoire.client;

import io.github.eclipsegrimoire.EclipseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client event is only loaded on physical clients; dedicated server never loads UI classes. */
@Mod.EventBusSubscriber(modid=EclipseMod.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() { }
    @SubscribeEvent public static void grimoireRightClick(PlayerInteractEvent.RightClickItem event) {
        if(!event.getLevel().isClientSide() || !event.getItemStack().is(EclipseMod.GRIMOIRE.get())) return;
        Minecraft.getInstance().setScreen(new GrimoireScreen(event.getItemStack().copy()));
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
