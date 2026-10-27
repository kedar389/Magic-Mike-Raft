package com.dungeonsillagers;

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

/** Registers the Geomancer, the Excavator, the Supreme Evoker, their props, spawn eggs and sound events. */
@Mod(DungeonsIllagersMod.MOD_ID)
public class DungeonsIllagersMod {

    public static final String MOD_ID = "dungeons_illagers";

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);

    public static final RegistryObject<EntityType<Geomancer>> GEOMANCER = ENTITIES.register("geomancer",
            () -> EntityType.Builder.of(Geomancer::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":geomancer"));
    public static final RegistryObject<EntityType<Excavator>> EXCAVATOR = ENTITIES.register("excavator",
            () -> EntityType.Builder.of(Excavator::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build(MOD_ID + ":excavator"));
    public static final RegistryObject<EntityType<SupremeEvoker>> SUPREME_EVOKER = ENTITIES.register("supreme_evoker",
            () -> EntityType.Builder.of(SupremeEvoker::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.8F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build(MOD_ID + ":supreme_evoker"));

    // BP_Geomancer_BombPillar PillarBounds 110 x 320 UE; BP_Geomancer_WallPillar 90 x 280 UE
    public static final RegistryObject<EntityType<BombPillar>> BOMB_PILLAR = ENTITIES.register("bomb_pillar",
            () -> EntityType.Builder.<BombPillar>of(BombPillar::new, MobCategory.MISC)
                    .sized(1.1F, 3.2F)
                    .clientTrackingRange(8)
                    .fireImmune()
                    .build(MOD_ID + ":bomb_pillar"));
    public static final RegistryObject<EntityType<WallPillar>> WALL_PILLAR = ENTITIES.register("wall_pillar",
            () -> EntityType.Builder.<WallPillar>of(WallPillar::new, MobCategory.MISC)
                    .sized(0.9F, 2.8F)
                    .clientTrackingRange(8)
                    .fireImmune()
                    .build(MOD_ID + ":wall_pillar"));
    public static final RegistryObject<EntityType<FangTrap>> FANG_TRAP = ENTITIES.register("fang_trap",
            () -> EntityType.Builder.<FangTrap>of(FangTrap::new, MobCategory.MISC)
                    .sized(0.9F, 1.2F)
                    .clientTrackingRange(8)
                    .fireImmune()
                    .build(MOD_ID + ":fang_trap"));
    public static final RegistryObject<EntityType<EvokerOrb>> EVOKER_ORB = ENTITIES.register("evoker_orb",
            () -> EntityType.Builder.<EvokerOrb>of(EvokerOrb::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(6)
                    .updateInterval(2)
                    .build(MOD_ID + ":evoker_orb"));

    public static final RegistryObject<Item> GEOMANCER_EGG = ITEMS.register("geomancer_spawn_egg",
            () -> new ForgeSpawnEggItem(GEOMANCER, 0x8A8F8C, 0xB060D8, new Item.Properties()));
    public static final RegistryObject<Item> EXCAVATOR_EGG = ITEMS.register("excavator_spawn_egg",
            () -> new ForgeSpawnEggItem(EXCAVATOR, 0x6A5478, 0xD8322E, new Item.Properties()));
    public static final RegistryObject<Item> SUPREME_EVOKER_EGG = ITEMS.register("supreme_evoker_spawn_egg",
            () -> new ForgeSpawnEggItem(SUPREME_EVOKER, 0x3A2030, 0x30E8F0, new Item.Properties()));

    public DungeonsIllagersMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IllagerSound.values(); // registers every sound event before the register is bound
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(DungeonsIllagersMod::onAttributes);
        modBus.addListener(DungeonsIllagersMod::onCreativeTab);
    }

    static RegistryObject<SoundEvent> registerSound(String name) {
        String id = "entity." + MOD_ID + "." + name;
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, id)));
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(GEOMANCER.get(), Geomancer.createAttributes().build());
        event.put(EXCAVATOR.get(), Excavator.createAttributes().build());
        event.put(SUPREME_EVOKER.get(), SupremeEvoker.createAttributes().build());
    }

    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(GEOMANCER_EGG);
            event.accept(EXCAVATOR_EGG);
            event.accept(SUPREME_EVOKER_EGG);
        }
    }
}
