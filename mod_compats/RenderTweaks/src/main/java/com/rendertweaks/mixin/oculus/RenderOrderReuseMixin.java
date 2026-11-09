package com.rendertweaks.mixin.oculus;

import com.rendertweaks.Tweak;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.irisshaders.batchedentityrendering.impl.TransparencyType;
import net.irisshaders.batchedentityrendering.impl.ordering.GraphTranslucencyRenderOrderManager;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Oculus 1.8.0 batched entity rendering decides the draw order of render types each frame: every
 * {@code begin(type)} and group call between {@code reset()}s builds a weighted graph, and
 * {@code getRenderOrder()} solves it (feedback arc set, then topological sort). About 1.5-2% of the
 * frame, for an answer that is usually the same as last frame's.
 *
 * <p>The graph, and so the answer, is fully determined by the sequence of calls since the last
 * reset. That sequence is recorded (Oculus's own methods still run as before); when it is identical
 * to last frame's, object for object, last frame's answer is returned and the solving is skipped.
 * Any difference (a mob coming into view, a new texture) means Oculus solves it again.
 */
@Mixin(value = GraphTranslucencyRenderOrderManager.class, remap = false)
public abstract class RenderOrderReuseMixin {

    @Unique
    private static final Object START_GROUP = new Object();

    @Unique
    private static final Object MAYBE_START_GROUP = new Object();

    @Unique
    private static final Object END_GROUP = new Object();

    @Unique
    private static final Object RESET_TYPE = new Object();

    @Unique
    private Object[] renderTweaks$calls = new Object[1024];

    @Unique
    private int renderTweaks$count;

    @Unique
    private Object[] renderTweaks$lastCalls;

    @Unique
    private List<RenderType> renderTweaks$lastOrder;

    @Unique
    private void renderTweaks$record(Object call) {
        if (renderTweaks$count == renderTweaks$calls.length) {
            renderTweaks$calls = Arrays.copyOf(renderTweaks$calls, renderTweaks$count * 2);
        }
        renderTweaks$calls[renderTweaks$count++] = call;
    }

    @Inject(method = "begin", at = @At("HEAD"))
    private void renderTweaks$recordBegin(RenderType type, CallbackInfo ci) {
        renderTweaks$record(type);
    }

    @Inject(method = "startGroup", at = @At("HEAD"))
    private void renderTweaks$recordStartGroup(CallbackInfo ci) {
        renderTweaks$record(START_GROUP);
    }

    @Inject(method = "maybeStartGroup", at = @At("HEAD"))
    private void renderTweaks$recordMaybeStartGroup(CallbackInfoReturnable<Boolean> cir) {
        renderTweaks$record(MAYBE_START_GROUP);
    }

    @Inject(method = "endGroup", at = @At("HEAD"))
    private void renderTweaks$recordEndGroup(CallbackInfo ci) {
        renderTweaks$record(END_GROUP);
    }

    @Inject(method = "resetType", at = @At("HEAD"))
    private void renderTweaks$recordResetType(TransparencyType type, CallbackInfo ci) {
        renderTweaks$record(RESET_TYPE);
        renderTweaks$record(type);
    }

    @Inject(method = "reset", at = @At("HEAD"))
    private void renderTweaks$startRecording(CallbackInfo ci) {
        // Drop the references so render types from the last frame are not kept beyond it.
        Arrays.fill(renderTweaks$calls, 0, renderTweaks$count, null);
        renderTweaks$count = 0;
    }

    @Inject(method = "getRenderOrder", at = @At("HEAD"), cancellable = true)
    private void renderTweaks$reuseOrder(CallbackInfoReturnable<List<RenderType>> cir) {
        if (Tweak.ORDER.isEnabled()
                && renderTweaks$lastOrder != null
                && renderTweaks$lastCalls.length == renderTweaks$count
                && renderTweaks$sameCalls()) {
            cir.setReturnValue(new ArrayList<>(renderTweaks$lastOrder));
        }
    }

    @Inject(method = "getRenderOrder", at = @At("RETURN"))
    private void renderTweaks$rememberOrder(CallbackInfoReturnable<List<RenderType>> cir) {
        renderTweaks$lastCalls = Arrays.copyOf(renderTweaks$calls, renderTweaks$count);
        renderTweaks$lastOrder = new ArrayList<>(cir.getReturnValue());
    }

    @Unique
    private boolean renderTweaks$sameCalls() {
        Object[] last = renderTweaks$lastCalls;
        Object[] current = renderTweaks$calls;
        for (int i = 0; i < last.length; i++) {
            if (last[i] != current[i]) {
                return false;
            }
        }
        return true;
    }
}
