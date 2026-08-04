package com.ironscompanions.event;

import com.ironscompanions.spell.IronsCompanionsSpells;
import com.ironscompanions.spell.SpellScrolls;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The Naginata boss drop.
 *
 * <p>The other three spells need no loot code at all. Iron's Spells distributes scrolls with its own
 * {@code irons_spellbooks:randomize_spell} loot function, whose default filter is
 * {@code SpellRegistry.getEnabledSpells()} kept by {@code isEnabled() && allowLooting()}, weighted by
 * {@code getMinRarity()}. Our spells are registered in that registry, so they already appear wherever
 * any other scroll appears (IS's eight chest-loot modifiers, its entity drops, its structures), at a
 * weight matching their rarity. Adding our own pool would double-dip and ignore rarity.
 *
 * <p>Naginata stays out of that pool by overriding {@code allowLooting()} to false in
 * {@link com.ironscompanions.spell.NaginataSpell}; it is a Pontiff drop only.
 */
public class CompanionsLootEvents {

    /**
     * "His Holiness" is the Pontiff's second phase, rendered from the same entity, so this one id
     * covers both. There is no registered {@code companions:his_holiness} entity type.
     */
    private static final ResourceLocation SACRED_PONTIFF = new ResourceLocation("companions", "sacred_pontiff");
    private static final double DROP_CHANCE = 0.10; // 10% on player kill; tune later
    private static final int DROP_SPELL_LEVEL = 1;

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        LivingEntity dead = event.getEntity();
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(dead.getType());
        if (!SACRED_PONTIFF.equals(id)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player)) {
            return;
        }
        if (dead.getRandom().nextDouble() > DROP_CHANCE) {
            return;
        }
        ItemStack scroll = SpellScrolls.of(IronsCompanionsSpells.NAGINATA.get(), DROP_SPELL_LEVEL);
        if (!scroll.isEmpty()) {
            event.getDrops().add(new ItemEntity(dead.level(), dead.getX(), dead.getY(), dead.getZ(), scroll));
        }
    }
}
