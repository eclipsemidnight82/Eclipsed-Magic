package io.github.eclipsegrimoire;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(EclipseMod.MOD_ID)
public final class EclipseMod {
    public static final String MOD_ID = "eclipse_grimoire";
    public static final TagKey<EntityType<?>> EXTRA_UNDEAD = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(MOD_ID, "undead"));
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final RegistryObject<Block> SALT_LINE = BLOCKS.register("salt_line", () -> new SaltLineBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE).noOcclusion()));
    public static final RegistryObject<Item> SALT = ITEMS.register("salt", () -> new BlockItem(SALT_LINE.get(), new Item.Properties()));
    public static final RegistryObject<Item> GRIMOIRE = ITEMS.register("grimoire", () -> new Item(new Item.Properties().stacksTo(1)));

    public EclipseMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(this::creativeTab);
    }
    private void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(SALT.get());
            event.accept(GRIMOIRE.get());
        }
    }
}
