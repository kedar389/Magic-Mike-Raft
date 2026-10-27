package com.dungeonsillagers.client;

import com.dungeonsillagers.DungeonsIllagersMod;
import com.dungeonsillagers.PrimeEnchanter;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib renderer: geo, animations and texture under {@code dungeons_illagers/.../entity/prime_enchanter}, plus the glowmask. */
@Mod.EventBusSubscriber(modid = DungeonsIllagersMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PrimeEnchanterRenderer extends GeoEntityRenderer<PrimeEnchanter> {

    public PrimeEnchanterRenderer(EntityRendererProvider.Context context) {
        super(context, new Model());
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
        withScale(1.4F);
        this.shadowRadius = 1.0F;
    }

    /** The death animation lays the body down itself. */
    @Override
    protected float getDeathMaxRotation(PrimeEnchanter entity) {
        return 0.0F;
    }

    /** Lit gauntlets stay visible over whatever scale the playing animation gives them. */
    private static final class Model extends DefaultedEntityGeoModel<PrimeEnchanter> {

        Model() {
            super(new ResourceLocation(DungeonsIllagersMod.MOD_ID, "prime_enchanter"));
        }

        @Override
        public void setCustomAnimations(PrimeEnchanter animatable, long instanceId, AnimationState<PrimeEnchanter> state) {
            super.setCustomAnimations(animatable, instanceId, state);
            if (animatable.gauntletsLit()) {
                for (String bone : new String[] {"gauntlet_ice", "gauntlet_lightning"}) {
                    getBone(bone).ifPresent(b -> b.updateScale(1.0F, 1.0F, 1.0F));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DungeonsIllagersMod.PRIME_ENCHANTER.get(), PrimeEnchanterRenderer::new);
    }
}
