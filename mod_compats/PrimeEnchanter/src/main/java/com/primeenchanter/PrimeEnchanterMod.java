package com.primeenchanter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registers the Prime Enchanter, its spawn egg and its sound events. */
@Mod(PrimeEnchanterMod.MOD_ID)
public class PrimeEnchanterMod {

    public static final String MOD_ID = "prime_enchanter";

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);

    public static final RegistryObject<EntityType<PrimeEnchanter>> PRIME_ENCHANTER = ENTITIES.register("prime_enchanter",
            () -> EntityType.Builder.of(PrimeEnchanter::new, MobCategory.MONSTER)
                    .sized(1.4F, 3.2F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build(MOD_ID + ":prime_enchanter"));

    public static final RegistryObject<Item> SPAWN_EGG = ITEMS.register("prime_enchanter_spawn_egg",
            () -> new ForgeSpawnEggItem(PRIME_ENCHANTER, 0x6B2530, 0x3FF2F2, new Item.Properties()));

    public PrimeEnchanterMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BossSound.values(); // registers every sound event before the register is bound
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(PrimeEnchanterMod::onAttributes);
        modBus.addListener(PrimeEnchanterMod::onCreativeTab);
    }

    static RegistryObject<SoundEvent> registerSound(String name) {
        String id = "entity.prime_enchanter." + name;
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, id)));
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(PRIME_ENCHANTER.get(), PrimeEnchanter.createAttributes().build());
    }

    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(SPAWN_EGG);
        }
    }
}
