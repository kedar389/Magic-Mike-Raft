package com.ironscompanions.spell;

import com.ironscompanions.IronsCompanions;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Deferred registry for the four Companions-derived spells. Registering here is also what makes them
 * eligible for Iron's Spells' own scroll loot.
 */
public final class IronsCompanionsSpells {

    private IronsCompanionsSpells() {}

    public static final DeferredRegister<AbstractSpell> SPELLS =
            DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, IronsCompanions.MOD_ID);

    public static final RegistryObject<AbstractSpell> ICE_TORNADO =
            SPELLS.register("ice_tornado", IceTornadoSpell::new);

    public static final RegistryObject<AbstractSpell> STONE_SPIKES =
            SPELLS.register("stone_spikes", StoneSpikesSpell::new);

    public static final RegistryObject<AbstractSpell> HEAL_RING =
            SPELLS.register("heal_ring", HealRingSpell::new);

    public static final RegistryObject<AbstractSpell> NAGINATA =
            SPELLS.register("naginata", NaginataSpell::new);
}
