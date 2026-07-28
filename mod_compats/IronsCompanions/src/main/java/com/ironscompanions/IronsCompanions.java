package com.ironscompanions;

import com.ironscompanions.event.CompanionAllyEvents;
import com.ironscompanions.event.CompanionArmorEvents;
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
 * Iron's Companions — bridges Iron's Spells 'n Spellbooks with the Companions! mod.
 *
 * <p>Phase 1-2 (this scaffold): registers the four converted spells (Ice Tornado, Stone Spikes,
 * Heal Ring, Naginata) and drops the Naginata scroll from the Sacred Pontiff. Later phases add
 * damage retyping, armour bridges, ally rules, and the Soul Mage AI (see README.md).
 */
@Mod(IronsCompanions.MOD_ID)
public class IronsCompanions {

    public static final String MOD_ID = "irons_companions";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public IronsCompanions() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Content: register our spells into Iron's Spells' spell registry.
        IronsCompanionsSpells.SPELLS.register(modBus);

        // Runtime event handlers.
        MinecraftForge.EVENT_BUS.register(new CompanionsLootEvents());  // Naginata scroll drop from Pontiff
        MinecraftForge.EVENT_BUS.register(new CompanionDamageEvents()); // Phase 3: retype ability damage to IS schools
        MinecraftForge.EVENT_BUS.register(new CompanionArmorEvents());  // Phase 4: mage-set / holy-robe bridges
        MinecraftForge.EVENT_BUS.register(new CompanionAllyEvents());   // Phase 5: no friendly fire
        MinecraftForge.EVENT_BUS.register(new SoulMageAiEvents());      // Phase 7: Soul Mage casts IS spells from scrolls

        LOGGER.info("[IronsCompanions] Initialised.");
    }
}
