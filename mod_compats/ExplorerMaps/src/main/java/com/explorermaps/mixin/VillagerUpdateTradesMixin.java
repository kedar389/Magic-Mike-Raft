package com.explorermaps.mixin;

import com.explorermaps.MapTrades;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Appends the map offer after the vanilla pool roll, so it never competes with the level's own
 * trades for the two random picks.
 *
 * <p>{@code remap = false} with the SRG name, as in the sibling compat mods: {@code m_7604_} is
 * {@code Villager#updateTrades}.
 */
@Mixin(value = Villager.class, remap = false)
public abstract class VillagerUpdateTradesMixin {

    @Inject(method = "m_7604_()V", at = @At("TAIL"))
    private void explorerMaps$addMapTrade(CallbackInfo ci) {
        MapTrades.addTo((Villager) (Object) this);
    }
}
