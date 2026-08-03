package com.ironscompanions.event;

import com.ironscompanions.IronsCompanions;
import com.ironscompanions.spell.IronsCompanionsSpells;
import com.ironscompanions.spell.SpellScrolls;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Where the converted spells come from, now that the magic books are gone.
 *
 * <p>Two sources, both mirroring how Companions itself handed the books out:
 * <ul>
 *   <li><b>Chest loot.</b> Companions' {@code CompanionsLootTables#onLootTableLoad} appends one pool
 *       to every {@code minecraft:chests/*} table holding a coin plus {@code book_black_hole} and
 *       {@code book_magic_ray} at 4.5% each. We append a pool the same way, carrying the scrolls
 *       instead.</li>
 *   <li><b>Boss drop.</b> The Naginata was the one craftable book (4x relic gold + a book); it is now
 *       the legendary scroll dropped by the Sacred Pontiff / His Holiness instead.</li>
 * </ul>
 */
public class CompanionsLootEvents {

    private static final ResourceLocation SACRED_PONTIFF = new ResourceLocation("companions", "sacred_pontiff");
    private static final ResourceLocation HIS_HOLINESS = new ResourceLocation("companions", "his_holiness");
    private static final double DROP_CHANCE = 0.10; // 10% on player kill; tune later

    /** Same 4.5% per entry Companions used for its two chest-loot books. */
    private static final float SCROLL_CHANCE = 0.045f;

    /** Level the looted scrolls are inscribed at. Ice Tornado tiering (README 5A-i) is still TODO. */
    private static final int LOOT_SPELL_LEVEL = 1;

    private static final String POOL_NAME = "irons_companions_scroll_pool";

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        LivingEntity dead = event.getEntity();
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(dead.getType());
        if (id == null || !(SACRED_PONTIFF.equals(id) || HIS_HOLINESS.equals(id))) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player)) {
            return;
        }
        if (dead.getRandom().nextDouble() > DROP_CHANCE) {
            return;
        }
        ItemStack scroll = SpellScrolls.of(IronsCompanionsSpells.NAGINATA.get(), LOOT_SPELL_LEVEL);
        if (!scroll.isEmpty()) {
            event.getDrops().add(new ItemEntity(dead.level(), dead.getX(), dead.getY(), dead.getZ(), scroll));
        }
    }

    /**
     * Adds the scroll pool to every vanilla chest table, exactly where the books used to appear.
     * Naginata is deliberately excluded: it is the Pontiff drop, not chest loot.
     */
    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        if (!"minecraft".equals(name.getNamespace()) || !name.getPath().startsWith("chests/")) {
            return;
        }

        LootPool.Builder pool = LootPool.lootPool()
                .name(POOL_NAME)
                .setRolls(ConstantValue.exactly(1))
                .add(scrollEntry(IronsCompanionsSpells.ICE_TORNADO))
                .add(scrollEntry(IronsCompanionsSpells.STONE_SPIKES))
                .add(scrollEntry(IronsCompanionsSpells.HEAL_RING));

        event.getTable().addPool(pool.build());
    }

    /**
     * A scroll cannot be a plain {@code LootItem}, because the spell lives in the stack's NBT. So we
     * build the real scroll once, then hand the loot system the generic Scroll item plus that tag.
     */
    private static LootPoolEntryContainer.Builder<?> scrollEntry(RegistryObject<AbstractSpell> spell) {
        ItemStack scroll = SpellScrolls.of(spell.get(), LOOT_SPELL_LEVEL);
        CompoundTag tag = scroll.getTag();
        if (tag == null) {
            IronsCompanions.LOGGER.warn("[IronsCompanions] Scroll for {} has no NBT, loot entry will be a blank scroll.",
                    spell.getId());
            tag = new CompoundTag();
        }
        return LootItem.lootTableItem(ItemRegistry.SCROLL.get())
                .apply(SetNbtFunction.setTag(tag))
                .when(LootItemRandomChanceCondition.randomChance(SCROLL_CHANCE));
    }
}
