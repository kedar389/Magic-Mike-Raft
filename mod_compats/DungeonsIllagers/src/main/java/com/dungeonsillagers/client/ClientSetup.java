package com.dungeonsillagers.client;

import com.dungeonsillagers.BombPillar;
import com.dungeonsillagers.DungeonsIllagersMod;
import com.dungeonsillagers.Excavator;
import com.dungeonsillagers.Geomancer;
import com.dungeonsillagers.Prop;
import com.dungeonsillagers.RoyalGuard;
import com.dungeonsillagers.SupremeEvoker;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** GeckoLib renderers: geo, animations and texture under {@code dungeons_illagers/.../entity/<name>}, plus glowmasks. */
@Mod.EventBusSubscriber(modid = DungeonsIllagersMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DungeonsIllagersMod.GEOMANCER.get(), ctx -> {
            // beads matte purple, shining yellow (bomb pillars) or pink (wall pillars) only while he casts
            Renderer<Geomancer> renderer = new Renderer<>(ctx, "geomancer", 1.0F, false, 0.5F);
            renderer.addRenderLayer(new TintedGlowLayer<>(renderer, texture("geomancer_beads"), (geomancer, partialTick) ->
                    switch (geomancer.beads()) {
                        case BOMB -> TintedGlowLayer.color(VfxLibrary.BEAD_YELLOW, 1.0F);
                        case WALL -> TintedGlowLayer.color(VfxLibrary.BEAD_PINK, 1.0F);
                        default -> null;
                    }));
            return renderer;
        });
        event.registerEntityRenderer(DungeonsIllagersMod.EXCAVATOR.get(), ExcavatorRenderer::new);
        event.registerEntityRenderer(DungeonsIllagersMod.ROYAL_GUARD.get(), RoyalGuardRenderer::new);
        event.registerEntityRenderer(DungeonsIllagersMod.SUPREME_EVOKER.get(), ctx -> {
            Renderer<SupremeEvoker> renderer = new Renderer<SupremeEvoker>(ctx, "supreme_evoker", 1.0F, true, 0.8F) {
                /** The death animation lays the body down itself. */
                @Override
                protected float getDeathMaxRotation(SupremeEvoker entity) {
                    return 0.0F;
                }
            }.trackingHands();
            renderer.addRenderLayer(new ShieldBubbleLayer(renderer));
            return renderer;
        });
        // BP scales: wall pillar mesh 0.9, fang trap 1.5
        event.registerEntityRenderer(DungeonsIllagersMod.BOMB_PILLAR.get(), ctx -> {
            // block outlines and centres glow yellow as the fuse burns down: a flicker at ignition, then heating up
            Renderer<BombPillar> renderer = new Renderer<>(ctx, "bomb_pillar", 1.0F, false, 0.6F);
            renderer.addRenderLayer(new TintedGlowLayer<>(renderer, texture("bomb_pillar_heat"), (bomb, partialTick) -> {
                float t = bomb.tickCount + partialTick;
                float heat;
                if (t >= 2 && t < 10) {
                    heat = 0.35F * (1 - Math.abs(t - 6) / 4);
                } else if (t >= BombPillar.HEAT_FROM) {
                    heat = Math.min(1.0F, (t - BombPillar.HEAT_FROM) / (BombPillar.FUSE - BombPillar.HEAT_FROM));
                    heat *= 0.85F + 0.15F * (float) Math.sin(t * 1.3F);
                } else {
                    return null;
                }
                return heat <= 0.01F ? null : TintedGlowLayer.color(0xFFE040, heat);
            }));
            return renderer;
        });
        event.registerEntityRenderer(DungeonsIllagersMod.WALL_PILLAR.get(), ctx -> new Renderer<Prop>(ctx, "wall_pillar", 0.9F, false, 0.5F));
        event.registerEntityRenderer(DungeonsIllagersMod.FANG_TRAP.get(), ctx -> new Renderer<Prop>(ctx, "fang_trap", 1.5F, false, 0.3F));
        event.registerEntityRenderer(DungeonsIllagersMod.EVOKER_ORB.get(), NoopRenderer::new);
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(DungeonsIllagersMod.MOD_ID, "textures/entity/" + name + ".png");
    }

    /** Model and texture named after the entity; invisible entities (a teleport in progress) are not drawn. */
    static class Renderer<T extends Entity & GeoEntity> extends GeoEntityRenderer<T> {

        private boolean tracksHands;

        Renderer(EntityRendererProvider.Context context, String name, float scale, boolean glows, float shadow) {
            this(context, new DefaultedEntityGeoModel<>(new ResourceLocation(DungeonsIllagersMod.MOD_ID, name)), scale, glows, shadow);
        }

        Renderer(EntityRendererProvider.Context context, DefaultedEntityGeoModel<T> model, float scale, boolean glows, float shadow) {
            super(context, model);
            if (glows) {
                addRenderLayer(new AutoGlowingGeoLayer<>(this));
            }
            withScale(scale);
            this.shadowRadius = shadow;
        }

        @Override
        public void render(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
            if (entity instanceof LivingEntity living && living.isInvisible()) {
                return;
            }
            super.render(entity, yaw, partialTick, poseStack, buffers, light);
            if (this.tracksHands) {
                HandTracking.report(this.model, entity, "J_L_Hand", "J_R_Hand");
            }
        }

        /** Reports the hand bones for effects cast from the hands. */
        Renderer<T> trackingHands() {
            this.tracksHands = true;
            return this;
        }
    }

    /** The shield bone shows only while he still holds it. */
    static final class RoyalGuardRenderer extends Renderer<RoyalGuard> {

        RoyalGuardRenderer(EntityRendererProvider.Context context) {
            super(context, new DefaultedEntityGeoModel<>(new ResourceLocation(DungeonsIllagersMod.MOD_ID, "royal_guard")) {
                @Override
                public void setCustomAnimations(RoyalGuard animatable, long instanceId, AnimationState<RoyalGuard> state) {
                    super.setCustomAnimations(animatable, instanceId, state);
                    getBone("shield").ifPresent(bone -> bone.setHidden(!animatable.hasShield()));
                }
            }, 1.0F, true, 0.5F);
        }
    }

    /** Half of them wear the backpack texture (T_DeepDark_Illager_Backpack). */
    static final class ExcavatorRenderer extends Renderer<Excavator> {

        /** The game's pickaxe is thin diagonal strips GeckoLib cannot box; a vanilla iron pickaxe takes its place. */
        private static final ItemStack PICKAXE = new ItemStack(Items.IRON_PICKAXE);
        private static final String WEAPON_BONE = "J_R_Weapon";
        private static final float PICKAXE_SCALE = 1.25F;

        private static final ResourceLocation BACKPACK =
                new ResourceLocation(DungeonsIllagersMod.MOD_ID, "textures/entity/excavator_backpack.png");

        ExcavatorRenderer(EntityRendererProvider.Context context) {
            super(context, new DefaultedEntityGeoModel<>(new ResourceLocation(DungeonsIllagersMod.MOD_ID, "excavator")) {
                @Override
                public ResourceLocation getTextureResource(Excavator animatable) {
                    return animatable.hasBackpack() ? BACKPACK : super.getTextureResource(animatable);
                }
            }, 1.0F, false, 0.5F);
            addRenderLayer(new BlockAndItemGeoLayer<>(this) {
                @Override
                protected ItemStack getStackForBone(GeoBone bone, Excavator animatable) {
                    return WEAPON_BONE.equals(bone.getName()) ? PICKAXE : null;
                }

                @Override
                protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, Excavator animatable) {
                    return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                }

                /** Held like a player's main hand from the bone at the fist, sized to the game's 115 UE pickaxe. */
                @Override
                protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, Excavator animatable,
                        MultiBufferSource buffers, float partialTick, int light, int overlay) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                    poseStack.scale(PICKAXE_SCALE, PICKAXE_SCALE, PICKAXE_SCALE);
                    super.renderStackForBone(poseStack, bone, stack, animatable, buffers, partialTick, light, overlay);
                }
            });
        }

        /** NS_DeepDarkIllager_CandleFire sits on the candle: 78 UE above the head pivot, 29 UE forward. */
        @Override
        public void render(Excavator entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
            super.render(entity, yaw, partialTick, poseStack, buffers, light);
            VfxLibrary.trackAnchor(entity, HandTracking.point(this.model.getBone("J_Head"), 0, 0.78F, -0.29F));
        }
    }
}
