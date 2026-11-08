package com.rendertweaks.mixin.apoliindex;

import com.rendertweaks.apoli.IndexedPowers;
import com.rendertweaks.apoli.PowerTypeIndex;
import com.rendertweaks.apoli.PowerTypeIndexHolder;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Apoli 2.9.2 {@code PowerHolderComponentImpl}. Its power map is only written by {@code addPower}
 * (put), {@code removePower} (remove) and {@code fromTag} (clear, put; used by NBT load and the
 * client sync packet). The version is bumped right after each of those writes, before any code that
 * runs next can ask, so the index is never older than the map.
 *
 * <p>Every injector here is required: if one cannot apply, Mixin skips this whole class (its config
 * is optional), and {@link IndexedPowers} then falls back to Apoli because the component does not
 * implement {@link PowerTypeIndexHolder}.
 */
@Mixin(targets = "io.github.apace100.apoli.component.PowerHolderComponentImpl", remap = false)
public abstract class PowerHolderComponentImplMixin implements PowerTypeIndexHolder {

    private static final String PUT = "Ljava/util/concurrent/ConcurrentHashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String REMOVE = "Ljava/util/concurrent/ConcurrentHashMap;remove(Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String CLEAR = "Ljava/util/concurrent/ConcurrentHashMap;clear()V";

    @Shadow
    @Final
    private ConcurrentHashMap<?, ?> powers;

    @Unique
    private volatile int renderTweaks$version;

    @Unique
    private volatile PowerTypeIndex renderTweaks$index;

    @Override
    public Object[] renderTweaks$powersOf(Class<?> type) {
        int version = renderTweaks$version;
        PowerTypeIndex index = renderTweaks$index;
        if (index == null || index.version() != version) {
            index = new PowerTypeIndex(version, powers.values().toArray());
            renderTweaks$index = index;
        }
        return index.ofType(type);
    }

    @Unique
    private void renderTweaks$changed() {
        renderTweaks$version++;
    }

    @Inject(method = "addPower", at = @At(value = "INVOKE", target = PUT, shift = At.Shift.AFTER))
    private void renderTweaks$afterAdd(CallbackInfoReturnable<Boolean> cir) {
        renderTweaks$changed();
    }

    @Inject(method = "removePower", at = @At(value = "INVOKE", target = REMOVE, shift = At.Shift.AFTER))
    private void renderTweaks$afterRemove(CallbackInfo ci) {
        renderTweaks$changed();
    }

    @Inject(method = "fromTag", at = @At(value = "INVOKE", target = CLEAR, shift = At.Shift.AFTER))
    private void renderTweaks$afterClear(CallbackInfo ci) {
        renderTweaks$changed();
    }

    @Inject(method = "fromTag", at = @At(value = "INVOKE", target = PUT, shift = At.Shift.AFTER))
    private void renderTweaks$afterLoad(CallbackInfo ci) {
        renderTweaks$changed();
    }

    @Inject(method = "getPowers(Ljava/lang/Class;Z)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void renderTweaks$indexedGetPowers(Class<?> type, boolean includeInactive, CallbackInfoReturnable<List<Object>> cir) {
        List<Object> powersOfType = IndexedPowers.getPowers(this, type, includeInactive);
        if (powersOfType != null) {
            cir.setReturnValue(powersOfType);
        }
    }
}
