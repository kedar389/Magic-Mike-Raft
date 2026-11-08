package com.rendertweaks.mixin.gtbc;

import com.rendertweaks.gtbc.TickEntityQueries;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * GTBC Spell Lib 2.2.0 {@code ForgeClientEvents}: {@code m_45976_} is
 * {@code Level#getEntitiesOfClass(Class, AABB)}, called once in each overlay method and twice in
 * {@code onCameraSetup}.
 */
@Mixin(targets = "com.gametechbc.spelllib.events.ForgeClientEvents", remap = false)
public abstract class GtbcEntityQueryMixin {

    // require = 0: if a GTBC update changes these methods, skip this tweak instead of failing startup.
    @Redirect(
            method = {"onCameraSetup", "processScreenFlashEffects", "processPowerInversionEffects"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;m_45976_(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"),
            require = 0)
    private static <T extends Entity> List<T> renderTweaks$oncePerTick(Level level, Class<T> type, AABB box) {
        return TickEntityQueries.entitiesOfClass(level, type, box);
    }
}
