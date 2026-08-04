package com.ironscompanions;

import com.ironscompanions.event.CompanionArmorEvents;
import com.ironscompanions.event.CompanionBookRemovalEvents;
import com.ironscompanions.event.CompanionDamageEvents;
import com.ironscompanions.event.CompanionsLootEvents;
import com.ironscompanions.soulmage.SoulMageAiEvents;
import com.ironscompanions.spell.IronsCompanionsSpells;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Iron's Companions bridges Iron's Spells 'n Spellbooks with the Companions! mod: it turns the
 * Companions magic books into real IS spells, removes the books, and retypes companion ability damage
 * into IS schools. See README.md for the full feature list.
 */
@Mod(IronsCompanions.MOD_ID)
public class IronsCompanions {

    public static final String MOD_ID = "irons_companions";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public IronsCompanions() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IronsCompanionsSpells.SPELLS.register(modBus);

        MinecraftForge.EVENT_BUS.register(new CompanionsLootEvents());        // Naginata boss drop
        MinecraftForge.EVENT_BUS.register(new CompanionDamageEvents());       // school retyping, spell damage
        MinecraftForge.EVENT_BUS.register(new CompanionArmorEvents());        // holy robe spell power
        MinecraftForge.EVENT_BUS.register(new CompanionBookRemovalEvents());  // recipes, Minion rewards
        MinecraftForge.EVENT_BUS.register(new SoulMageAiEvents());            // Soul Mage casts from scrolls

        // JEI hiding lives in compat/IronsCompanionsJeiPlugin, loaded by JEI on the client only.
        LOGGER.info("[IronsCompanions] Initialised.");
    }
}
