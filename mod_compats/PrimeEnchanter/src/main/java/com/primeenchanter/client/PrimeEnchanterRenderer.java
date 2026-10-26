package com.primeenchanter.client;

import com.primeenchanter.PrimeEnchanter;
import com.primeenchanter.PrimeEnchanterMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib renderer: geo, animations and texture under {@code prime_enchanter/.../entity/}, plus the glowmask. */
@Mod.EventBusSubscriber(modid = PrimeEnchanterMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PrimeEnchanterRenderer extends GeoEntityRenderer<PrimeEnchanter> {

    public PrimeEnchanterRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(new ResourceLocation(PrimeEnchanterMod.MOD_ID, "prime_enchanter")));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
        withScale(1.4F);
        this.shadowRadius = 1.0F;
    }

    /** The death animation lays the body down itself. */
    @Override
    protected float getDeathMaxRotation(PrimeEnchanter entity) {
        return 0.0F;
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PrimeEnchanterMod.PRIME_ENCHANTER.get(), PrimeEnchanterRenderer::new);
    }
}
