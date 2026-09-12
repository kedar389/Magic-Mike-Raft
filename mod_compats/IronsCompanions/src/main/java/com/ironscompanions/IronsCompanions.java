package com.ironscompanions;

import com.ironscompanions.attribute.CompanionArmorBonuses;
import com.ironscompanions.attribute.MageStaffBonuses;
import com.ironscompanions.attribute.NaginataBookBonuses;
import com.ironscompanions.books.RemovedBookDrops;
import com.ironscompanions.books.RemovedBookLoot;
import com.ironscompanions.damage.CompanionsDamageBridge;
import com.ironscompanions.recipe.RecipeRewrites;
import com.ironscompanions.soulmage.SoulMageSpellcasting;
import com.ironscompanions.spell.IronsCompanionsSpells;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Iron's Companions bridges Iron's Spells 'n Spellbooks with the Companions! mod: it turns the
 * Companions magic books into real Iron's Spells spells, removes the books, and retypes companion
 * ability damage into Iron's Spells schools. See README.md for the full feature list.
 *
 * <p>Every handler is stateless, so the classes themselves are registered rather than instances of
 * them, and each package owns one bridge:
 *
 * <ul>
 *   <li>{@code spell} the four converted spells and the machinery that spawns Companions projectiles
 *       on their behalf;</li>
 *   <li>{@code damage} Companions hits re-dealt through Iron's Spells;</li>
 *   <li>{@code attribute} the bonuses Companions gear grants while worn;</li>
 *   <li>{@code books} the magic books taken out of circulation;</li>
 *   <li>{@code recipe} every recipe change, applied in one sweep;</li>
 *   <li>{@code soulmage} the Soul Mage casting from scrolls;</li>
 *   <li>{@code mixin}, {@code client} and {@code compat} for what no event can reach, the client-only
 *       tooltip, and the JEI plugin.</li>
 * </ul>
 */
@Mod(IronsCompanions.MOD_ID)
public class IronsCompanions {

    public static final String MOD_ID = "irons_companions";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public IronsCompanions() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IronsCompanionsSpells.SPELLS.register(modBus);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.register(CompanionsDamageBridge.class);   // school retyping, spell damage
        forgeBus.register(CompanionArmorBonuses.class);    // armour set bridges
        forgeBus.register(RemovedBookLoot.class);          // magic books out of chest loot
        forgeBus.register(RemovedBookDrops.class);         // Minion handouts become scrolls
        forgeBus.register(RecipeRewrites.class);           // book recipes, blood vial <-> crystallized blood
        forgeBus.register(SoulMageSpellcasting.class);     // Soul Mage casts from scrolls
        forgeBus.register(NaginataBookBonuses.class);      // worn mana + holy power
        forgeBus.register(MageStaffBonuses.class);         // mage staff held bonuses

        // The client tooltip subscribes itself through @Mod.EventBusSubscriber(Dist.CLIENT), and JEI
        // hiding lives in compat/IronsCompanionsJeiPlugin, which JEI loads on the client only.
        LOGGER.info("[IronsCompanions] Initialised.");
    }
}
