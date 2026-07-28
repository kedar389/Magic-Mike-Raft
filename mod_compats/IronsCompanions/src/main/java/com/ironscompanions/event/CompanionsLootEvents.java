package com.ironscompanions.event;

import com.ironscompanions.IronsCompanions;
import com.ironscompanions.spell.IronsCompanionsSpells;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Drops the legendary Naginata scroll from the Sacred Pontiff (and His Holiness). Done in Java so
 * the scroll's spell NBT is built with Iron's Spells' own API rather than hand-written datapack NBT.
 */
public class CompanionsLootEvents {

    private static final ResourceLocation SACRED_PONTIFF = new ResourceLocation("companions", "sacred_pontiff");
    private static final ResourceLocation HIS_HOLINESS = new ResourceLocation("companions", "his_holiness");
    private static final double DROP_CHANCE = 0.10; // 10% on player kill; tune later

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
        ItemStack scroll = createNaginataScroll();
        if (!scroll.isEmpty()) {
            event.getDrops().add(new ItemEntity(dead.level(), dead.getX(), dead.getY(), dead.getZ(), scroll));
        }
    }

    /**
     * VERIFY(build): build a scroll of the Naginata spell using the IS API. Expected shape in 3.16.2:
     * <pre>
     *   ItemStack stack = new ItemStack(io.redspace.ironsspellbooks.registries.ItemRegistry.SCROLL.get());
     *   ISpellContainer.get(stack).addSpell(IronsCompanionsSpells.NAGINATA.getId().toString(), 1, true, stack);
     *   return stack;
     * </pre>
     * Confirm the scroll item + {@code ISpellContainer} method names against the shipped API, then
     * replace this stub.
     */
    private static ItemStack createNaginataScroll() {
        IronsCompanions.LOGGER.debug(
                "[IronsCompanions] createNaginataScroll() stub -> wire IS scroll API for {}",
                IronsCompanionsSpells.NAGINATA.getId());
        return ItemStack.EMPTY; // TODO(build): replace with real IS scroll (see javadoc above)
    }
}
