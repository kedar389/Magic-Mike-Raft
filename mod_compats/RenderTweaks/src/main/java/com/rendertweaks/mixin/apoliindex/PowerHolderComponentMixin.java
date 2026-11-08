package com.rendertweaks.mixin.apoliindex;

import com.rendertweaks.apoli.IndexedPowers;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Apoli 2.9.2 {@code PowerHolderComponent.hasPower(Entity, Class, Predicate)}, the static interface
 * method the two-argument {@code hasPower} delegates to. Answered from the index; null from
 * {@link IndexedPowers} leaves Apoli's own code to run.
 */
@Mixin(targets = "io.github.apace100.apoli.component.PowerHolderComponent", remap = false)
public interface PowerHolderComponentMixin {

    @Inject(
            method = "hasPower(Lnet/minecraft/world/entity/Entity;Ljava/lang/Class;Ljava/util/function/Predicate;)Z",
            at = @At("HEAD"),
            cancellable = true)
    @SuppressWarnings("unchecked")
    private static void renderTweaks$indexedHasPower(
            Entity entity, Class<?> type, Predicate<?> filter, CallbackInfoReturnable<Boolean> cir) {
        Boolean answer = IndexedPowers.hasPower(entity, type, (Predicate<Object>) filter);
        if (answer != null) {
            cir.setReturnValue(answer);
        }
    }
}
