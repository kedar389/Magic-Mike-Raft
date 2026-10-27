package com.dungeonsillagers.client;

import com.dungeonsillagers.DungeonsIllagersMod;
import com.dungeonsillagers.Vfx;
import com.dungeonsillagers.Vfx.Anchor;
import com.dungeonsillagers.client.VfxParticle.Shape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * The bosses' Niagara systems rebuilt from {@link VfxParticle}s: same sprites (atlas from the game's textures), the
 * colours of their decoded colour curves, and the shapes of their mesh emitters. Effects that last are emitters ticked
 * here; an emitter on a hand follows it (positions reported by the renderers, see {@link #trackHands}).
 */
@Mod.EventBusSubscriber(modid = DungeonsIllagersMod.MOD_ID, value = Dist.CLIENT)
public final class VfxLibrary {

    // colour curves of the Niagara systems, HDR values brought into range
    static final int ICE = 0x5AD8FF, ICE_DEEP = 0x0073FF, ICE_WHITE = 0xD8F6FF;
    static final int YELLOW = 0xFFE94A, WHITE_HOT = 0xFFFFE6, SPARK_BLUE = 0x3FA8FF, ORANGE = 0xFF5A00;
    static final int EV_PURPLE = 0x9A1AFF, EV_CYAN = 0x33FFFF, EV_DEEP = 0x30005F, EV_MAGENTA = 0xFF4DFF;
    static final int ROYAL_ORANGE = 0xFF4200;
    // Geomancer runes (SummonPillar: purple, SummonPillar_Bomb: red-orange), bomb fire, Excavator slash
    static final int GEO_PURPLE = 0x8000FF, GEO_RED = 0xFF2010, FIRE_ORANGE = 0xFF8A00, FIRE_RED = 0xFF3000;
    static final int EXC_RED = 0xFF0019, DUST = 0xB8AC9C;
    /** Fallback hand offset when the model has not been drawn yet: sideways, up, ahead. */
    private static final double HAND_SIDE = 0.8, HAND_UP = 1.7;

    private static final List<Emitter> EMITTERS = new ArrayList<>();
    private static final Map<Integer, Vec3[]> HANDS = new HashMap<>();
    private static final Map<Integer, Vec3> ANCHORS = new HashMap<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private VfxLibrary() {
    }

    /** Called by renderers every frame with the world positions of the left and right hand bones. */
    public static void trackHands(Entity entity, @Nullable Vec3 left, @Nullable Vec3 right) {
        HANDS.put(entity.getId(), new Vec3[] {left, right});
    }

    /** A third tracked point (the Excavator's candle). */
    public static void trackAnchor(Entity entity, @Nullable Vec3 point) {
        if (point != null) {
            ANCHORS.put(entity.getId(), point);
        }
    }

    @Nullable
    static Vec3 hand(Entity entity, boolean left) {
        Vec3[] hands = HANDS.get(entity.getId());
        Vec3 tracked = hands == null ? null : hands[left ? 0 : 1];
        if (tracked != null && tracked.distanceToSqr(entity.position()) < 64) {
            return tracked;
        }
        Vec3 side = Vec3.directionFromRotation(0, entity.getYRot() + (left ? -90 : 90)).scale(HAND_SIDE);
        return entity.position().add(side).add(0, HAND_UP * entity.getBbHeight() / 3.2, 0);
    }

    /** A lasting effect: {@code body} runs every tick with its age and the current origin. */
    private record Emitter(int duration, @Nullable Entity caster, Anchor anchor, Vec3 point, BiConsumer<Integer, Vec3> body,
            int[] age) {
    }

    private static void emit(Vfx.Packet packet, @Nullable Entity caster, int duration, BiConsumer<Integer, Vec3> body) {
        EMITTERS.add(new Emitter(duration, caster, packet.anchor(), packet.pos(), body, new int[] {0}));
    }

    @Nullable
    private static Vec3 origin(@Nullable Entity caster, Anchor anchor, Vec3 point) {
        if (anchor == Anchor.POINT) {
            return point;
        }
        if (caster == null || caster.isRemoved()) {
            return null;
        }
        return switch (anchor) {
            case LEFT_HAND -> hand(caster, true);
            case RIGHT_HAND -> hand(caster, false);
            default -> caster.position();
        };
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().isPaused()) {
            if (Minecraft.getInstance().level == null) {
                EMITTERS.clear();
                HANDS.clear();
                ANCHORS.clear();
            }
            return;
        }
        for (Iterator<Emitter> it = EMITTERS.iterator(); it.hasNext(); ) {
            Emitter e = it.next();
            Vec3 at = origin(e.caster, e.anchor, e.point);
            if (at == null || e.age[0] >= e.duration) {
                it.remove();
                continue;
            }
            e.body.accept(e.age[0]++, at);
        }
        HANDS.keySet().removeIf(id -> Minecraft.getInstance().level.getEntity(id) == null);
        ANCHORS.keySet().removeIf(id -> Minecraft.getInstance().level.getEntity(id) == null);
    }

    // ---- building blocks -------------------------------------------------------------------------

    private static float rand() {
        return RANDOM.nextFloat() * 2 - 1;
    }

    private static Vec3 dir() {
        Vec3 v;
        do {
            v = new Vec3(rand(), rand(), rand());
        } while (v.lengthSqr() > 1 || v.lengthSqr() < 1.0E-4);
        return v.normalize();
    }

    private static Vec3 flat(double angle) {
        return new Vec3(Math.cos(angle), 0, Math.sin(angle));
    }

    private static Vec3 facing(float yaw) {
        return Vec3.directionFromRotation(0, yaw);
    }

    /** Glowing pixel streak (VelocityAligned sparks, lightning shocks). */
    private static VfxParticle spark(Vec3 at, Vec3 velocity, float width, float length, int life, int from, int to) {
        return VfxParticle.at(at).shape(Shape.STREAK).velocity(velocity).size(width, width * 0.5F).height(length, length * 0.3F)
                .color(from, to).alpha(1, 0).life(life).drag(0.88F).spawn();
    }

    /** Camera-facing sprite. */
    private static VfxParticle glint(VfxSprite sprite, Vec3 at, Vec3 velocity, float size, int life, int from, int to) {
        return VfxParticle.at(at).sprite(sprite, -1).velocity(velocity).size(size, size * 0.4F).color(from, to).alpha(1, 0)
                .life(life).drag(0.9F).spawn();
    }

    /** Flat ring on the ground growing from {@code r0} to {@code r1}. */
    private static VfxParticle ring(VfxSprite sprite, Vec3 at, float r0, float r1, int life, int from, int to) {
        return VfxParticle.at(at.add(0, 0.05, 0)).shape(Shape.FLAT).sprite(sprite, 0).size(r0, r1).color(from, to).alpha(1, 0)
                .life(life).spawn();
    }

    /** Lightning strip between two points (T_ElectricStraight). */
    private static VfxParticle bolt(Vec3 from, Vec3 to, float width, int life, int color) {
        return VfxParticle.at(from).shape(Shape.BEAM).sprite(VfxSprite.BOLT, -1).beam(to.subtract(from)).size(width, width)
                .color(color).alpha(1, 0).life(life).linear().spawn();
    }

    /** Electric arc flipbook (M_ElectricArcs). */
    private static VfxParticle arc(Vec3 at, float size, int life, int color) {
        return VfxParticle.at(at).sprite(VfxSprite.ARC, 0).animated().size(size, size).color(color).alpha(1, 0.6F).life(life)
                .spin(30 * rand()).spawn();
    }

    private static VfxParticle cube(Vec3 at, Vec3 velocity, float s0, float s1, int life, int from, int to) {
        return VfxParticle.at(at).shape(Shape.CUBE).velocity(velocity).size(s0, s1).color(from, to).alpha(1, 0).life(life)
                .spin(12 * rand()).drag(0.9F).spawn();
    }

    /** Light pillar / tube (SM_Pipe_Bottomless with MI_LinearGradient). */
    private static VfxParticle tube(VfxSprite sprite, Vec3 at, float r0, float r1, float h0, float h1, int life, int from, int to) {
        return VfxParticle.at(at).shape(Shape.TUBE).sprite(sprite, 0).size(r0, r1).height(h0, h1).color(from, to).alpha(0.9F, 0)
                .life(life).spawn();
    }

    /** Ice spike (SM_Iccle): three stacked cubes shrinking upwards, rising from the ground. */
    private static void icicle(Vec3 at, float scale, int life, int color) {
        for (int i = 0; i < 3; i++) {
            float s = scale * (0.5F - i * 0.13F);
            VfxParticle.at(at.add(0, scale * (0.15 + i * 0.32), 0)).shape(Shape.CUBE).size(s * 0.3F, s).color(ICE_WHITE, color)
                    .alpha(1, 0).fadeIn(0.1F).life(life).spawn();
        }
    }

    /** Snow / ice glints drifting down (SnowParticles: M_IceSymbol). */
    private static void snow(Vec3 at, double spread, int count, int life) {
        for (int i = 0; i < count; i++) {
            Vec3 p = at.add(rand() * spread, rand() * spread * 0.5, rand() * spread);
            VfxParticle.at(p).sprite(VfxSprite.ICE, -1).velocity(new Vec3(rand() * 0.02, -0.02 - RANDOM.nextFloat() * 0.03, rand() * 0.02))
                    .size(0.09F, 0.05F).color(ICE_WHITE, ICE).alpha(1, 0).fadeIn(0.2F).life(life + RANDOM.nextInt(10)).spawn();
        }
    }

    /** MI_SmokePixel puff swelling and fading, drifting. */
    private static VfxParticle puff(Vec3 at, Vec3 velocity, float size, int life, int color, float alpha) {
        return VfxParticle.at(at).sprite(VfxSprite.SMOKE, 0).animated().velocity(velocity).size(size, size * 1.3F).color(color)
                .alpha(alpha, alpha * 0.6F).life(life).drag(0.9F).matte().spawn();
    }

    /** MI_DebrisGeneric chip thrown out, falling. */
    private static VfxParticle chip(Vec3 at, Vec3 velocity, float size, int life, int color) {
        return VfxParticle.at(at).sprite(VfxSprite.DEBRIS, -1).velocity(velocity).size(size, size).color(color).alpha(1, 0.8F)
                .life(life).gravity(0.9F).drag(0.98F).spin(20 * rand()).matte().physics().spawn();
    }

    /** NS_Dust-Impact / SummonDust: a ring of dust puffs and a few chips. */
    private static void dust(Vec3 at, double radius, int puffs, int chips) {
        for (int i = 0; i < puffs; i++) {
            Vec3 d = flat(RANDOM.nextFloat() * Mth.TWO_PI);
            puff(at.add(d.scale(radius * RANDOM.nextFloat())).add(0, 0.1, 0), d.scale(0.06).add(0, 0.02, 0), 0.35F + RANDOM.nextFloat() * 0.25F,
                    18 + RANDOM.nextInt(10), DUST, 0.7F);
        }
        for (int i = 0; i < chips; i++) {
            Vec3 d = flat(RANDOM.nextFloat() * Mth.TWO_PI);
            chip(at.add(0, 0.2, 0), d.scale(0.1 + RANDOM.nextFloat() * 0.15).add(0, 0.25 + RANDOM.nextFloat() * 0.2, 0), 0.12F, 20, 0x9A9488);
        }
    }

    /** Runes rising (NS_IllagerGeomancer_SummonPillar / _Bomb, NS_GeomancerWall / BombBlock_Appear). */
    private static void runes(Vec3 at, double radius, int count, int color, float rise) {
        for (int i = 0; i < count; i++) {
            Vec3 p = at.add(flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(RANDOM.nextFloat() * radius)).add(0, RANDOM.nextFloat() * 0.5, 0);
            VfxParticle.at(p).sprite(VfxSprite.RUNE, -1).velocity(new Vec3(0, rise * (0.5F + RANDOM.nextFloat()), 0)).size(0.16F, 0.08F)
                    .color(0xFFFFFF, color).alpha(1, 0).life(18 + RANDOM.nextInt(10)).drag(0.95F).spawn();
        }
    }

    /** Soft cold mist (Mist / Smoke emitters): big faint pixels drifting up. */
    private static void mist(Vec3 at, double spread, int count, int color) {
        for (int i = 0; i < count; i++) {
            VfxParticle.at(at.add(rand() * spread, RANDOM.nextFloat() * 0.4, rand() * spread)).velocity(new Vec3(rand() * 0.02, 0.02, rand() * 0.02))
                    .size(0.35F, 0.7F).color(color).alpha(0.25F, 0).fadeIn(0.25F).life(30 + RANDOM.nextInt(15)).spin(3 * rand()).spawn();
        }
    }

    // ---- effects ---------------------------------------------------------------------------------

    public static void play(Vfx.Packet packet) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Entity caster = Minecraft.getInstance().level.getEntity(packet.caster());
        Vec3 at = origin(caster, packet.anchor(), packet.pos());
        if (at == null) {
            return;
        }
        float yaw = packet.yaw();
        float param = packet.param();
        switch (packet.effect()) {
            case ICE_FIST_ON -> {
                cube(at, Vec3.ZERO, 0.3F, 0.9F, 7, ICE_WHITE, ICE_DEEP);
                for (int i = 0; i < 14; i++) {
                    glint(VfxSprite.ICE, at, dir().scale(0.12), 0.12F, 14, ICE_WHITE, ICE);
                }
                for (int i = 0; i < 6; i++) {
                    glint(VfxSprite.SPARKLE, at, dir().scale(0.06), 0.14F, 18, 0xFFFFFF, ICE);
                }
            }
            case LIGHTNING_FIST_ON -> {
                cube(at, Vec3.ZERO, 0.3F, 0.9F, 6, WHITE_HOT, YELLOW);
                for (int i = 0; i < 4; i++) {
                    arc(at.add(dir().scale(0.3)), 0.45F, 6, WHITE_HOT);
                }
                for (int i = 0; i < 12; i++) {
                    spark(at, dir().scale(0.25), 0.05F, 0.3F, 8, WHITE_HOT, YELLOW);
                }
            }
            case FROST_PUNCH_CHARGE -> emit(packet, caster, 18, (age, o) -> {
                // Spherical particles drawn into the fist, a cube swelling at its core
                if (age == 0) {
                    cube(o, Vec3.ZERO, 0.1F, 0.6F, 18, ICE_WHITE, ICE_DEEP);
                }
                for (int i = 0; i < 4; i++) {
                    Vec3 d = dir();
                    spark(o.add(d.scale(1.4)), d.scale(-0.14), 0.05F, 0.4F, 10, ICE_WHITE, ICE);
                }
                glint(VfxSprite.ICE, o.add(dir().scale(0.8)), dir().scale(0.02), 0.1F, 10, ICE_WHITE, ICE);
            });
            case FROST_PUNCH -> {
                // IcePunch / Shockwave: open ice boxes bursting forward, mist and snow thrown along the punch
                Vec3 fwd = facing(yaw);
                VfxParticle.at(at).shape(Shape.WALLS).sprite(VfxSprite.GRADIENT, 0).yaw(-yaw).size(0.4F, 1.9F).height(1.6F, 0.6F)
                        .color(ICE_WHITE, ICE_DEEP).alpha(0.9F, 0).life(10).spawn();
                VfxParticle.at(at.add(fwd.scale(1.2))).shape(Shape.WALLS).sprite(VfxSprite.GRADIENT, 0).yaw(-yaw).size(0.3F, 1.3F)
                        .height(1.2F, 0.4F).color(ICE_WHITE, ICE).alpha(0.8F, 0).life(8).spawn();
                ring(VfxSprite.RING_THICK, at, 0.4F, 3.2F, 12, ICE_WHITE, ICE_DEEP);
                for (int i = 0; i < 26; i++) {
                    Vec3 v = fwd.scale(0.25 + RANDOM.nextFloat() * 0.35).add(rand() * 0.15, RANDOM.nextFloat() * 0.18, rand() * 0.15);
                    glint(VfxSprite.ICE, at.add(0, 0.6 + RANDOM.nextFloat(), 0), v, 0.13F, 16, ICE_WHITE, ICE);
                }
                mist(at.add(fwd.scale(1.5)), 1.2, 8, ICE);
            }
            case STAB_ICE, STAB_LIGHTNING -> {
                // AnimNS_Stab: a spear of light from the fist and a little ring where it lands
                boolean ice = packet.effect() == Vfx.Effect.STAB_ICE;
                int color = ice ? ICE : YELLOW;
                Vec3 fwd = facing(yaw);
                VfxParticle.at(at).shape(Shape.BEAM).sprite(VfxSprite.SOLID, 0).beam(fwd.scale(2.2)).size(0.35F, 0.05F)
                        .color(ice ? ICE_WHITE : WHITE_HOT, color).alpha(1, 0).life(4).spawn();
                VfxParticle.at(at.add(fwd.scale(2.2))).sprite(VfxSprite.RING, 0).size(0.2F, 0.9F).color(color).alpha(1, 0).life(5).spawn();
            }
            case SLASH_ICE, SLASH_LIGHTNING -> {
                // AnimNS_Slash: a crescent swept in front of the boss
                boolean ice = packet.effect() == Vfx.Effect.SLASH_ICE;
                int hot = ice ? ICE_WHITE : WHITE_HOT, color = ice ? ICE : YELLOW;
                float side = param >= 0 ? 1 : -1;
                for (int i = 0; i <= 16; i++) {
                    float a = (yaw + side * (-70 + i * 140 / 16.0F)) * Mth.DEG_TO_RAD;
                    Vec3 p = at.add(-Math.sin(a) * 2.0, 0.3 * Math.sin(i / 16.0 * Math.PI), Math.cos(a) * 2.0);
                    Vec3 tangent = new Vec3(-Math.cos(a), 0, -Math.sin(a)).scale(side * 0.25);
                    spark(p, tangent, 0.14F, 0.7F, 5 + i / 4, hot, color);
                }
            }
            case POWERED_UP -> {
                // SparkBuff blue + yellow, lightning shocks, ice spikes round the feet
                for (int i = 0; i < 24; i++) {
                    Vec3 p = at.add(rand() * 1.2, RANDOM.nextFloat() * 3.0, rand() * 1.2);
                    spark(p, new Vec3(0, 0.15 + RANDOM.nextFloat() * 0.15, 0), 0.06F, 0.5F, 14, WHITE_HOT, i % 2 == 0 ? SPARK_BLUE : YELLOW);
                }
                for (int i = 0; i < 6; i++) {
                    arc(at.add(rand() * 1.0, 0.8 + RANDOM.nextFloat() * 2.2, rand() * 1.0), 0.8F, 8, WHITE_HOT);
                }
                for (int i = 0; i < 5; i++) {
                    icicle(at.add(flat(Mth.TWO_PI * i / 5).scale(1.6)), 1.2F, 24, ICE);
                }
                ring(VfxSprite.RING, at, 0.5F, 3.5F, 12, WHITE_HOT, SPARK_BLUE);
            }
            case GROUND_SLAM, LAUNCH_IMPACT -> impact(at, packet.effect() == Vfx.Effect.LAUNCH_IMPACT ? 3.5F : 2.8F);
            case LAUNCH -> {
                // Wind pipe whirling upward, electric arcs at take-off
                VfxParticle.at(at).shape(Shape.TUBE).sprite(VfxSprite.WIND, 0).size(1.1F, 1.6F).height(1.0F, 6.0F).scroll(0.08F)
                        .color(0xFFFFFF, ICE).alpha(0.8F, 0).life(16).spawn();
                for (int i = 0; i < 8; i++) {
                    arc(at.add(rand() * 1.0, RANDOM.nextFloat() * 2.5, rand() * 1.0), 0.9F, 8, WHITE_HOT);
                }
                mist(at, 1.5, 10, ICE);
            }
            case LAUNCH_TELEGRAPH -> emit(packet, caster, Math.max(1, (int) param), (age, o) -> {
                // LaunchImpact_Telegraph: god rays and electric threads falling on the landing spot
                if (age % 6 == 0) {
                    ring(VfxSprite.RING, o, 3.5F, 3.3F, 8, WHITE_HOT, SPARK_BLUE);
                }
                if (age % 2 == 0) {
                    float a = RANDOM.nextFloat() * Mth.TWO_PI;
                    Vec3 base = o.add(flat(a).scale(RANDOM.nextFloat() * 1.8));
                    VfxParticle.at(base).shape(Shape.BEAM).sprite(VfxSprite.GOD_RAY, -1).beam(new Vec3(0, 7, 0)).size(0.5F, 0.5F)
                            .color(0xFFFFFF, SPARK_BLUE).alpha(0.6F, 0).fadeIn(0.4F).life(10).linear().spawn();
                }
                if (age % 3 == 0) {
                    Vec3 top = o.add(rand() * 2, 6 + RANDOM.nextFloat() * 2, rand() * 2);
                    bolt(top, o.add(rand() * 0.5, 0, rand() * 0.5), 0.35F, 3, WHITE_HOT);
                }
            });
            case LIGHTNING_SLAM_CHARGE -> emit(packet, caster, Math.max(1, (int) param), (age, o) -> {
                // LightningFromSky into the raised fist, radial lightning and sparks round it
                if (age % 4 == 0) {
                    bolt(o.add(rand() * 1.5, 9, rand() * 1.5), o, 0.5F, 3, WHITE_HOT);
                }
                if (age % 2 == 0) {
                    Vec3 d = dir();
                    bolt(o, o.add(d.scale(0.8 + RANDOM.nextFloat() * 0.6)), 0.22F, 2, YELLOW);
                }
                spark(o, dir().scale(0.12), 0.05F, 0.3F, 8, WHITE_HOT, YELLOW);
                if (age % 5 == 0) {
                    arc(o, 0.7F, 5, WHITE_HOT);
                }
            });
            case LIGHTNING_SLAM -> {
                // Explosion_Ring (128), lightning shocks out of the sky across the blast, lingering sparks
                ring(VfxSprite.RING_THICK, at, 0.5F, param, 14, 0xFFFFFF, SPARK_BLUE);
                ring(VfxSprite.RING, at, 0.5F, param * 0.75F, 10, WHITE_HOT, YELLOW);
                VfxParticle.at(at).shape(Shape.SPHERE).sprite(VfxSprite.SOLID, 0).size(0.5F, param * 0.45F).color(WHITE_HOT, SPARK_BLUE)
                        .alpha(0.35F, 0).life(10).spawn();
                for (int i = 0; i < 7; i++) {
                    Vec3 hit = at.add(flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(RANDOM.nextFloat() * param * 0.8));
                    bolt(hit.add(rand(), 12, rand()), hit, 0.7F, 6, WHITE_HOT);
                    arc(hit.add(0, 0.4, 0), 0.9F, 7, WHITE_HOT);
                }
                for (int i = 0; i < 40; i++) {
                    Vec3 d = flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(0.35 + RANDOM.nextFloat() * 0.4).add(0, 0.2 + RANDOM.nextFloat() * 0.35, 0);
                    spark(at.add(0, 0.2, 0), d, 0.07F, 0.6F, 16, WHITE_HOT, i % 3 == 0 ? YELLOW : SPARK_BLUE);
                }
                mist(at, param * 0.4, 10, SPARK_BLUE);
            }
            case OVERCHARGE -> emit(packet, caster, Math.max(1, (int) param), (age, o) -> {
                // Beams from the fist to the ground around, a pulsing cube, arcs and sparks
                if (age % 2 == 0) {
                    for (int i = 0; i < 2; i++) {
                        Vec3 ground = new Vec3(o.x, o.y - 1.6, o.z).add(flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(1.5 + RANDOM.nextFloat() * 3));
                        bolt(o, ground, 0.4F, 3, WHITE_HOT);
                    }
                }
                if (age % 6 == 0) {
                    cube(o, Vec3.ZERO, 0.5F, 0.9F, 6, 0xFFFFFF, YELLOW);
                }
                spark(o, dir().scale(0.2), 0.05F, 0.35F, 8, WHITE_HOT, YELLOW);
                if (age % 4 == 0) {
                    arc(o.add(dir().scale(0.4)), 0.6F, 6, WHITE_HOT);
                }
            });
            case OVERCHARGE_TELEGRAPH -> emit(packet, caster, Math.max(1, (int) param), (age, o) -> {
                if (age % 5 == 0) {
                    ring(VfxSprite.RING, new Vec3(o.x, packet.pos().y, o.z), 10.0F, 9.8F, 6, WHITE_HOT, YELLOW);
                }
                if (age % 2 == 0) {
                    Vec3 p = new Vec3(o.x, packet.pos().y + 0.1, o.z).add(flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(10));
                    arc(p, 0.5F, 5, YELLOW);
                }
            });
            case OVERCHARGE_FLOOR -> emit(packet, caster, Math.max(1, (int) param), (age, o) -> {
                // Lightning floor: arcs scattered over the disc, beams racing across it, sparks
                for (int i = 0; i < 6; i++) {
                    Vec3 p = o.add(flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(Math.sqrt(RANDOM.nextFloat()) * 10)).add(0, 0.15, 0);
                    arc(p, 0.5F + RANDOM.nextFloat() * 0.4F, 5, i % 2 == 0 ? WHITE_HOT : YELLOW);
                }
                if (age % 3 == 0) {
                    float a = RANDOM.nextFloat() * Mth.TWO_PI, b = a + Mth.PI * (0.6F + RANDOM.nextFloat() * 0.8F);
                    bolt(o.add(flat(a).scale(10)).add(0, 0.15, 0), o.add(flat(b).scale(10)).add(0, 0.15, 0), 0.4F, 3, WHITE_HOT);
                }
                for (int i = 0; i < 3; i++) {
                    Vec3 p = o.add(flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(Math.sqrt(RANDOM.nextFloat()) * 10));
                    spark(p, new Vec3(rand() * 0.05, 0.12, rand() * 0.05), 0.05F, 0.3F, 6, WHITE_HOT, YELLOW);
                }
                if (age % 10 == 0) {
                    ring(VfxSprite.RING, o, 10.0F, 10.0F, 10, YELLOW, WHITE_HOT);
                }
            });
            case ICE_MINE_TELEGRAPH -> emit(packet, caster, Math.max(1, (int) param), (age, o) -> {
                // Iccle rising, mist and a snow loop; the ring marks the blast (250 UE)
                if (age == 0) {
                    icicle(o, 0.8F, (int) param, ICE);
                }
                if (age % 4 == 0) {
                    ring(VfxSprite.RING, o, 3.5F, 3.4F, 6, ICE_WHITE, ICE);
                    snow(o.add(0, 1, 0), 1.0, 2, 14);
                }
                if (age % 8 == 0) {
                    mist(o, 0.8, 1, ICE);
                }
            });
            case ICE_MINE_EXPLOSION -> {
                ring(VfxSprite.RING_THICK, at, 0.4F, 3.5F, 10, ICE_WHITE, ICE_DEEP);
                VfxParticle.at(at.add(0, 0.5, 0)).shape(Shape.SPHERE).sprite(VfxSprite.SOLID, 0).size(0.3F, 2.2F).color(ICE_WHITE, ICE)
                        .alpha(0.55F, 0).life(9).spawn();
                for (int i = 0; i < 16; i++) {
                    Vec3 d = dir();
                    cube(at.add(0, 0.6, 0), new Vec3(d.x * 0.4, Math.abs(d.y) * 0.4 + 0.1, d.z * 0.4), 0.18F, 0.05F, 14, ICE_WHITE, ICE)
                            .gravity(0.06F);
                }
                snow(at.add(0, 1, 0), 1.5, 10, 18);
                mist(at, 1.2, 6, ICE);
            }
            case FROST_SUMMON, FROST_SUMMON_MINION, EVOKER_SUMMON, EVOKER_SUMMON_MINION -> {
                // Cylinder light pillar (MI_LinearGradient) with runes and particles rising inside it
                boolean evoker = packet.effect() == Vfx.Effect.EVOKER_SUMMON || packet.effect() == Vfx.Effect.EVOKER_SUMMON_MINION;
                boolean big = packet.effect() == Vfx.Effect.FROST_SUMMON || packet.effect() == Vfx.Effect.EVOKER_SUMMON;
                int a = evoker ? EV_CYAN : SPARK_BLUE, b = evoker ? EV_PURPLE : YELLOW;
                float radius = big ? 2.4F : 0.8F, height = big ? 6 : 3;
                tube(VfxSprite.GRADIENT, at, radius, radius * 1.1F, 0.5F, height, 20, 0xFFFFFF, evoker ? EV_PURPLE : SPARK_BLUE);
                for (int i = 0; i < (big ? 26 : 10); i++) {
                    Vec3 p = at.add(flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(RANDOM.nextFloat() * radius));
                    VfxParticle.at(p).sprite(i % 3 == 0 ? VfxSprite.RUNE : VfxSprite.SOLID, -1).velocity(new Vec3(0, 0.06 + RANDOM.nextFloat() * 0.08, 0))
                            .size(i % 3 == 0 ? 0.16F : 0.06F, 0.04F).color(0xFFFFFF, i % 2 == 0 ? a : b).alpha(1, 0).life(22 + RANDOM.nextInt(12)).spawn();
                }
                ring(VfxSprite.RING, at, radius * 0.3F, radius, 14, 0xFFFFFF, evoker ? EV_PURPLE : SPARK_BLUE);
            }
            case INVULNERABLE -> emit(packet, caster, Math.max(1, (int) param), (age, o) -> {
                // Barrier: orange pixels flickering over the body while untouchable
                if (caster == null) {
                    return;
                }
                for (int i = 0; i < 3; i++) {
                    Vec3 p = o.add(flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(caster.getBbWidth() * 0.7)).add(0, RANDOM.nextFloat() * caster.getBbHeight(), 0);
                    VfxParticle.at(p).velocity(new Vec3(0, 0.02, 0)).size(0.1F, 0.02F).color(0xFFB060, ORANGE).alpha(1, 0).fadeIn(0.3F).life(10).spawn();
                }
            });
            case ORB_INITIATE -> emit(packet, caster, 10, (age, o) -> {
                // Rune_ArmWrap spiralling round the hand, a flash ring and a cube going purple to cyan
                if (age == 0) {
                    VfxParticle.at(o).sprite(VfxSprite.RING, 0).size(0.2F, 1.2F).color(0xFFFFFF, EV_CYAN).alpha(1, 0).life(7).spawn();
                    cube(o, Vec3.ZERO, 0.2F, 0.45F, 10, EV_PURPLE, EV_CYAN);
                }
                float a = age * 0.9F;
                for (int i = 0; i < 2; i++) {
                    Vec3 p = o.add(Math.cos(a + i * Math.PI) * 0.45, age * 0.04 - 0.2, Math.sin(a + i * Math.PI) * 0.45);
                    VfxParticle.at(p).sprite(VfxSprite.RUNE, -1).size(0.14F, 0.05F).color(EV_MAGENTA, EV_CYAN).alpha(1, 0).life(10).spawn();
                }
            });
            case ORB_HIT -> {
                cube(at, Vec3.ZERO, 0.3F, 0.8F, 6, 0xFFFFFF, EV_CYAN);
                for (int i = 0; i < 14; i++) {
                    spark(at, dir().scale(0.22), 0.05F, 0.35F, 10, 0xFFFFFF, EV_CYAN);
                }
                for (int i = 0; i < 8; i++) {
                    glint(VfxSprite.SPIRAL, at, dir().scale(0.05), 0.18F, 16, EV_CYAN, EV_PURPLE);
                }
            }
            case EVOKER_SUMMON_INITIATE -> {
                for (int i = 0; i < 14; i++) {
                    VfxParticle.at(at).sprite(VfxSprite.RUNE, -1).velocity(dir().scale(0.15)).size(0.2F, 0.08F).color(0xFFFFFF, EV_PURPLE)
                            .alpha(1, 0).drag(0.88F).life(20).spawn();
                }
            }
            case BUBBLE_INITIATE -> {
                // Octagon_Hollow rings bursting from the hand, a cube, sparks
                cube(at, Vec3.ZERO, 0.3F, 0.7F, 8, 0xFFFFFF, EV_MAGENTA);
                for (int i = 0; i < 8; i++) {
                    VfxParticle.at(at).sprite(VfxSprite.OCTAGON, 0).velocity(dir().scale(0.12)).size(0.15F, 0.5F).color(0xFFFFFF, EV_PURPLE)
                            .alpha(1, 0).drag(0.9F).spin(8 * rand()).life(14).spawn();
                }
                for (int i = 0; i < 16; i++) {
                    spark(at, dir().scale(0.2), 0.04F, 0.3F, 10, 0xFFFFFF, EV_MAGENTA);
                }
            }
            case BUBBLE_HIT -> {
                for (int i = 0; i < 5; i++) {
                    VfxParticle.at(at.add(dir().scale(1.6))).sprite(VfxSprite.OCTAGON, 0).size(0.2F, 0.45F).color(0xFFFFFF, EV_MAGENTA)
                            .alpha(0.9F, 0).life(8).spawn();
                }
            }
            case BUBBLE_SHATTER -> {
                // Glass shards of the bubble flying out, a hexagon flash
                VfxParticle.at(at).shape(Shape.SPHERE).sprite(VfxSprite.SOLID, 0).size(2.4F, 3.0F).color(0xFF80FF, EV_PURPLE).alpha(0.5F, 0)
                        .life(6).spawn();
                for (int i = 0; i < 30; i++) {
                    Vec3 d = dir();
                    cube(at.add(d.scale(2.2)), d.scale(0.15 + RANDOM.nextFloat() * 0.15), 0.22F, 0.08F, 18, 0xFF9CFF, EV_PURPLE).gravity(0.04F);
                }
            }
            case TELEPORT_INITIATE -> {
                // Rune_Radial: runes laid in a circle on the ground, one in the middle
                for (int i = 0; i < 10; i++) {
                    Vec3 p = at.add(flat(Mth.TWO_PI * i / 10).scale(1.4)).add(0, 0.05, 0);
                    VfxParticle.at(p).shape(Shape.FLAT).sprite(VfxSprite.RUNE, -1).yaw(-i * 36.0F).size(0.25F, 0.25F).color(0xFFFFFF, EV_PURPLE)
                            .alpha(1, 0).fadeIn(0.2F).life(20).spawn();
                }
                VfxParticle.at(at.add(0, 0.05, 0)).shape(Shape.FLAT).sprite(VfxSprite.RUNE, -1).size(0.6F, 0.6F).color(0xFFFFFF, EV_CYAN)
                        .alpha(1, 0).fadeIn(0.2F).spin(4).life(20).spawn();
            }
            case TELEPORT_OUT, TELEPORT_IN -> {
                // Lines shooting up (out) or falling in, and a burst of pixels
                boolean out = packet.effect() == Vfx.Effect.TELEPORT_OUT;
                for (int i = 0; i < 26; i++) {
                    Vec3 p = at.add(flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(RANDOM.nextFloat() * 0.8)).add(0, out ? RANDOM.nextFloat() * 2.4 : 4 + RANDOM.nextFloat() * 2, 0);
                    spark(p, new Vec3(0, out ? 0.5 : -0.5, 0), 0.06F, 1.4F, 9, 0xFFFFFF, i % 2 == 0 ? EV_CYAN : EV_PURPLE).drag(1.0F);
                }
                for (int i = 0; i < 18; i++) {
                    glint(VfxSprite.SOLID, at.add(0, 1.2, 0), dir().scale(0.18), 0.07F, 14, 0xFFFFFF, EV_PURPLE);
                }
            }
            case FANG_INITIATE -> emit(packet, caster, Math.max(1, (int) param), (age, o) -> {
                // Spirals glowing on the hand, spiral particles drawn in
                if (age % 3 == 0) {
                    VfxParticle.at(o).sprite(VfxSprite.SPIRAL, 0).size(0.35F, 0.2F).spin(10).color(0xFFFFFF, EV_CYAN).alpha(0.9F, 0).life(6).spawn();
                }
                Vec3 d = dir();
                VfxParticle.at(o.add(d.scale(0.9))).sprite(VfxSprite.SPIRAL, 0).velocity(d.scale(-0.08)).size(0.12F, 0.05F)
                        .color(EV_CYAN, EV_PURPLE).alpha(1, 0).life(10).spawn();
            });
            case FANG_CAST -> {
                for (int i = 0; i < 16; i++) {
                    glint(VfxSprite.SOLID, at, dir().scale(0.2), 0.06F, 10, 0xFFFFFF, EV_CYAN);
                }
            }
            case FANG_RISE -> {
                ring(VfxSprite.RING, at, 0.2F, 0.9F, 10, EV_CYAN, EV_PURPLE);
                for (int i = 0; i < 6; i++) {
                    glint(VfxSprite.SPIRAL, at.add(rand() * 0.4, 0.1, rand() * 0.4), new Vec3(0, 0.06, 0), 0.12F, 14, EV_CYAN, EV_PURPLE);
                }
            }
            case ROYAL_SLASH -> {
                // AnimNS_Slash in FF4200: a crescent of hot streaks
                float side = param >= 0 ? 1 : -1;
                for (int i = 0; i <= 12; i++) {
                    float a = (yaw + side * (-60 + i * 120 / 12.0F)) * Mth.DEG_TO_RAD;
                    Vec3 p = at.add(-Math.sin(a) * 1.4, 0.25 * Math.sin(i / 12.0 * Math.PI), Math.cos(a) * 1.4);
                    spark(p, new Vec3(-Math.cos(a), 0, -Math.sin(a)).scale(side * 0.2), 0.12F, 0.55F, 4 + i / 4, 0xFFC080, ROYAL_ORANGE);
                }
            }
            case ROYAL_SLAM -> {
                ring(VfxSprite.RING_THICK, at, 0.3F, 1.8F, 8, 0xFFC080, ROYAL_ORANGE);
                for (int i = 0; i < 14; i++) {
                    Vec3 d = flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(0.2 + RANDOM.nextFloat() * 0.2).add(0, 0.2 + RANDOM.nextFloat() * 0.2, 0);
                    spark(at.add(0, 0.1, 0), d, 0.06F, 0.4F, 10, 0xFFC080, ROYAL_ORANGE).gravity(0.03F);
                }
            }
            case SHIELD_BREAK -> {
                // Shield debris (magenta-hot, cooling), orange sparks turning blue, blue sparks upward
                for (int i = 0; i < 16; i++) {
                    Vec3 d = dir();
                    cube(at, d.scale(0.15 + RANDOM.nextFloat() * 0.2).add(0, 0.15, 0), 0.22F, 0.1F, 22, 0xFF60C0, 0x303030).gravity(0.06F);
                }
                for (int i = 0; i < 20; i++) {
                    spark(at, dir().scale(0.3), 0.06F, 0.45F, 12, 0xFFA040, SPARK_BLUE);
                }
                for (int i = 0; i < 10; i++) {
                    spark(at.add(rand() * 0.4, 0, rand() * 0.4), new Vec3(rand() * 0.05, 0.25 + RANDOM.nextFloat() * 0.15, rand() * 0.05), 0.05F, 0.5F, 14, 0x40B0FF, 0x0060FF);
                }
            }
            case MOB_SPAWN -> {
                // NS_GenericMobSpawn: a white cube flash and pixels bursting out of the body
                cube(at.add(0, 1.0, 0), Vec3.ZERO, 0.4F, 1.4F, 7, 0xFFFFFF, 0x808080);
                for (int i = 0; i < 18; i++) {
                    VfxParticle.at(at.add(0, 0.4 + RANDOM.nextFloat() * 1.4, 0)).velocity(dir().scale(0.12)).size(0.08F, 0.02F)
                            .color(0xFFFFFF, 0xA0A0A0).alpha(1, 0).fadeIn(0.1F).drag(0.9F).life(16).spawn();
                }
            }
            case GEO_BEADS -> emit(packet, caster, 10, (age, o) -> {
                // AnimNS_Geomancer_GlowingBeads: the bead necklace flares (orange for bombs, purple for walls)
                int color = param > 0 ? GEO_PURPLE : FIRE_ORANGE;
                for (int i = 0; i < 2; i++) {
                    float a = RANDOM.nextFloat() * Mth.TWO_PI;
                    VfxParticle.at(o.add(Math.cos(a) * 0.3, 1.45, Math.sin(a) * 0.3)).velocity(new Vec3(0, 0.015, 0)).size(0.07F, 0.02F)
                            .color(0xFFFFFF, color).alpha(1, 0).life(8).spawn();
                }
            });
            case GEO_CAST_BOMB, GEO_CAST_WALL -> {
                boolean bomb = packet.effect() == Vfx.Effect.GEO_CAST_BOMB;
                runes(at, 0.8, 12, bomb ? GEO_RED : GEO_PURPLE, 0.08F);
                for (int i = 0; i < 10; i++) {
                    VfxParticle.at(at.add(rand() * 0.6, RANDOM.nextFloat() * 0.4, rand() * 0.6)).velocity(new Vec3(0, 0.08 + RANDOM.nextFloat() * 0.06, 0))
                            .size(0.06F, 0.02F).color(0xFFFFFF, bomb ? FIRE_ORANGE : GEO_PURPLE).alpha(1, 0).life(16).spawn();
                }
            }
            case GEO_SUMMON_DUST -> dust(at, 0.6, 8, 6);
            case BOMB_APPEAR, WALL_APPEAR -> {
                // NS_GeomancerBombBlock_Appear / Wall_Appear: runes (red-hot or purple) and dust where the block rises
                boolean bomb = packet.effect() == Vfx.Effect.BOMB_APPEAR;
                runes(at, 0.6, 8, bomb ? GEO_RED : GEO_PURPLE, 0.1F);
                dust(at, 0.7, 7, bomb ? 0 : 4);
                ring(VfxSprite.RING, at, 0.2F, 0.9F, 10, 0xFFFFFF, bomb ? GEO_RED : GEO_PURPLE);
            }
            case BOMB_FUSE -> emit(packet, caster, Math.max(1, (int) param), (age, o) -> {
                // AnimNS_GeomancerBombBlock_PreExplosion: the pillar heats up, embers and flames licking up its sides
                float heat = age / Math.max(1.0F, param);
                if (age % 2 == 0) {
                    Vec3 p = o.add(flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(0.55)).add(0, RANDOM.nextFloat() * 2.8, 0);
                    VfxParticle.at(p).sprite(VfxSprite.FIRE, 0).animated().velocity(new Vec3(0, 0.03, 0)).size(0.12F + heat * 0.15F, 0.08F)
                            .color(0xFFE080, FIRE_RED).alpha(1, 0.4F).life(8).spawn();
                }
                if (RANDOM.nextFloat() < 0.3F + heat) {
                    spark(o.add(rand() * 0.4, 2.9, rand() * 0.4), new Vec3(rand() * 0.05, 0.12, rand() * 0.05), 0.05F, 0.25F, 8, 0xFFE080, FIRE_ORANGE);
                }
                if (age % 10 == 0) {
                    ring(VfxSprite.RING, o, 4.5F, 4.4F, 8, FIRE_ORANGE, FIRE_RED);
                }
            });
            case BOMB_EXPLODE -> {
                // NS_ExplosionGeomancerMedium: flash, pixel blast ring, fire sphere, sparks, fire floaters, radial smoke
                VfxParticle.at(at.add(0, 1.0, 0)).shape(Shape.SPHERE).sprite(VfxSprite.SOLID, 0).size(0.4F, param * 0.8F)
                        .color(0xFFF0A0, FIRE_RED).alpha(0.8F, 0).life(8).spawn();
                ring(VfxSprite.RING_THICK, at, 0.5F, param * 1.2F, 12, 0xFFE080, FIRE_RED);
                cube(at.add(0, 1.0, 0), Vec3.ZERO, 0.8F, 2.4F, 4, 0xFFFFFF, 0xFFE080);
                for (int i = 0; i < 36; i++) {
                    Vec3 d = dir();
                    spark(at.add(0, 1.0, 0), new Vec3(d.x, Math.abs(d.y), d.z).scale(0.3 + RANDOM.nextFloat() * 0.3), 0.08F, 0.6F, 14,
                            0xFFF0A0, i % 2 == 0 ? FIRE_ORANGE : FIRE_RED).gravity(0.04F);
                }
                for (int i = 0; i < 10; i++) {
                    VfxParticle.at(at.add(flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(RANDOM.nextFloat() * param * 0.7)).add(0, 0.1, 0))
                            .sprite(VfxSprite.FIRE, 0).animated().velocity(new Vec3(0, 0.02, 0)).size(0.3F, 0.2F).color(0xFFE080, FIRE_RED)
                            .alpha(1, 0.3F).life(14 + RANDOM.nextInt(8)).spawn();
                }
                for (int i = 0; i < 14; i++) {
                    Vec3 d = flat(RANDOM.nextFloat() * Mth.TWO_PI);
                    puff(at.add(0, 0.4, 0), d.scale(0.2).add(0, 0.05, 0), 0.6F, 26, 0x403830, 0.8F);
                }
                for (int i = 0; i < 12; i++) {
                    Vec3 d = dir();
                    chip(at.add(0, 1.0, 0), new Vec3(d.x, Math.abs(d.y) + 0.3, d.z).scale(0.35), 0.15F, 24, 0x9A9488);
                }
            }
            case WALL_CRUMBLE -> {
                // NS_GeomancerWall_Debris / SmallDebris: dust sliding down the pillar, chips falling off
                for (int i = 0; i < 8; i++) {
                    puff(at.add(rand() * 0.5, RANDOM.nextFloat() * 2.4, rand() * 0.5), new Vec3(rand() * 0.04, -0.02, rand() * 0.04), 0.35F, 22, DUST, 0.6F);
                }
                for (int i = 0; i < 10; i++) {
                    chip(at.add(rand() * 0.45, 0.5 + RANDOM.nextFloat() * 2.0, rand() * 0.45), new Vec3(rand() * 0.08, 0.05, rand() * 0.08), 0.12F, 20, 0x8E8A84);
                }
            }
            case EXC_SLASH -> {
                // AnimNS_Slash in FF0019
                float side = param >= 0 ? 1 : -1;
                for (int i = 0; i <= 10; i++) {
                    float a = (yaw + side * (-55 + i * 110 / 10.0F)) * Mth.DEG_TO_RAD;
                    Vec3 p = at.add(-Math.sin(a) * 1.1, 0.4 * (i / 10.0 - 0.5) * side, Math.cos(a) * 1.1);
                    spark(p, new Vec3(-Math.cos(a), 0, -Math.sin(a)).scale(side * 0.18), 0.1F, 0.45F, 4 + i / 4, 0xFF8090, EXC_RED);
                }
            }
            case EXC_SMASH -> {
                // NS_GroundSlam_Hammer (warm circle, long sparks), NS_Dust-Impact-Small, NS_Debris_Generic_Medium
                ring(VfxSprite.RING_THICK, at, 0.3F, 2.2F, 9, 0xFFD0B0, 0xFF9966);
                for (int i = 0; i < 16; i++) {
                    Vec3 d = flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(0.25 + RANDOM.nextFloat() * 0.3).add(0, 0.25 + RANDOM.nextFloat() * 0.3, 0);
                    spark(at.add(0, 0.1, 0), d, 0.06F, i % 3 == 0 ? 0.8F : 0.35F, 12, 0xFFF0A0, FIRE_ORANGE).gravity(0.05F);
                }
                dust(at, 1.2, 10, 10);
            }
            case EVOKER_DEATH -> {
                // ArenaAnnounce: flash, whirlwind pipe and an expanding sphere in deep cyan
                VfxParticle.at(at).shape(Shape.SPHERE).sprite(VfxSprite.SOLID, 0).size(0.5F, 5.0F).color(0x60E8FF, 0x0040A0).alpha(0.6F, 0)
                        .life(16).spawn();
                VfxParticle.at(at).shape(Shape.TUBE).sprite(VfxSprite.WIND, 0).size(1.5F, 3.0F).height(2, 8).scroll(0.06F)
                        .color(0x60E8FF, 0x0040A0).alpha(0.9F, 0).life(24).spawn();
                for (int i = 0; i < 40; i++) {
                    spark(at.add(0, 1.4, 0), dir().scale(0.35), 0.07F, 0.6F, 20, 0xFFFFFF, i % 2 == 0 ? EV_CYAN : 0x0060C0);
                }
            }
        }
    }

    /** NS_Frostbite_GroundSlam / LaunchImpact: blast rings, ice spikes, lightning shocks and sparks thrown out. */
    private static void impact(Vec3 at, float radius) {
        ring(VfxSprite.RING_THICK, at, 0.4F, radius * 1.3F, 12, 0xFFFFFF, ICE);
        ring(VfxSprite.RING, at, 0.4F, radius, 9, WHITE_HOT, SPARK_BLUE);
        for (int i = 0; i < 7; i++) {
            icicle(at.add(flat(Mth.TWO_PI * i / 7 + rand() * 0.3).scale(radius * 0.55)), 1.1F + RANDOM.nextFloat() * 0.5F, 22, ICE);
        }
        for (int i = 0; i < 10; i++) {
            Vec3 d = flat(RANDOM.nextFloat() * Mth.TWO_PI);
            Vec3 p = at.add(d.scale(RANDOM.nextFloat() * radius));
            bolt(p, p.add(d.scale(0.8)).add(0, 1.2 + RANDOM.nextFloat(), 0), 0.3F, 4, WHITE_HOT);
        }
        for (int i = 0; i < 46; i++) {
            Vec3 d = flat(RANDOM.nextFloat() * Mth.TWO_PI).scale(0.3 + RANDOM.nextFloat() * 0.45).add(0, 0.15 + RANDOM.nextFloat() * 0.4, 0);
            spark(at.add(0, 0.2, 0), d, 0.07F, 0.55F, 14 + RANDOM.nextInt(8), WHITE_HOT, i % 3 == 0 ? ICE : SPARK_BLUE).gravity(0.02F);
        }
        snow(at.add(0, 1, 0), radius * 0.6, 12, 20);
        mist(at, radius * 0.5, 10, ICE);
    }

    /** Gauntlet auras while lit (NS_Frostbite_IceFist / ElectricityFist): called each client tick by the boss. */
    public static void gauntletAura(Entity boss, int tick) {
        Vec3 ice = hand(boss, false), lightning = hand(boss, true);
        if (ice != null && tick % 2 == 0) {
            VfxParticle.at(ice.add(rand() * 0.25, rand() * 0.25, rand() * 0.25)).sprite(VfxSprite.ICE, -1)
                    .velocity(new Vec3(0, -0.03, 0)).size(0.09F, 0.03F).color(ICE_WHITE, ICE).alpha(1, 0).life(12).spawn();
        }
        if (lightning != null && tick % 3 == 0) {
            arc(lightning.add(rand() * 0.2, rand() * 0.2, rand() * 0.2), 0.45F, 4, tick % 6 == 0 ? WHITE_HOT : YELLOW);
        }
        if (lightning != null && tick % 4 == 1) {
            spark(lightning, dir().scale(0.1), 0.04F, 0.25F, 6, WHITE_HOT, YELLOW);
        }
    }

    /**
     * NS_DeepDarkIllager_CandleFire on the Excavator's cap: a centre flame and regular flames licking upward
     * (MI_FireSide, VelocityAligned), side flames, and embers floating off. Called each client tick.
     */
    public static void candle(Entity excavator, int tick) {
        Vec3 at = ANCHORS.get(excavator.getId());
        if (at == null || at.distanceToSqr(excavator.position()) > 16) {
            at = excavator.position().add(0, 2.3, 0);
        }
        // Fire_Center
        VfxParticle.at(at.add(0, 0.06, 0)).sprite(VfxSprite.FIRE, 0).animated().velocity(new Vec3(0, 0.015, 0)).size(0.13F, 0.07F)
                .color(0xFFF2B0, FIRE_ORANGE).alpha(1, 0.3F).life(5).spawn();
        // Fire_Regular
        if (tick % 2 == 0) {
            VfxParticle.at(at.add(rand() * 0.03, 0.1, rand() * 0.03)).sprite(VfxSprite.FIRE, 0).animated().velocity(new Vec3(0, 0.03, 0))
                    .size(0.1F, 0.04F).color(0xFFD060, FIRE_RED).alpha(1, 0).life(7).spawn();
        }
        // Fire_Side
        if (tick % 3 == 0) {
            VfxParticle.at(at.add(rand() * 0.06, 0.05, rand() * 0.06)).sprite(VfxSprite.FIRE, 0).animated()
                    .velocity(new Vec3(rand() * 0.01, 0.02, rand() * 0.01)).size(0.07F, 0.03F).color(0xFFB040, FIRE_RED).alpha(1, 0).life(6).spawn();
        }
        // Fire_Floater: embers (colour curve 25,3.5,0 fading out)
        if (tick % 6 == 0) {
            VfxParticle.at(at.add(0, 0.18, 0)).velocity(new Vec3(rand() * 0.01, 0.025, rand() * 0.01)).size(0.03F, 0.01F)
                    .color(0xFFA020, FIRE_RED).alpha(1, 0).life(18).spawn();
        }
    }

    /** NS_Step_Dust: a puff at a foot plant. */
    public static void stepDust(Entity mob) {
        for (int i = 0; i < 2; i++) {
            puff(mob.position().add(rand() * 0.25, 0.05, rand() * 0.25), new Vec3(rand() * 0.02, 0.01, rand() * 0.02), 0.18F, 14, DUST, 0.45F);
        }
    }

    /** NS_SupremeEvoker_Projectile: white cube core pulsing, spiral trail fading cyan to purple. Called per orb tick. */
    public static void orbTrail(Vec3 at, Vec3 motion, int tick) {
        cube(at, Vec3.ZERO, 0.32F, 0.2F, 2, 0xFFFFFF, EV_CYAN);
        if (tick % 4 == 0) {
            VfxParticle.at(at).shape(Shape.CUBE).size(0.3F, 0.6F).color(EV_CYAN, EV_PURPLE).alpha(0.5F, 0).life(5).spawn();
        }
        VfxParticle.at(at.subtract(motion.scale(0.5)).add(rand() * 0.1, rand() * 0.1, rand() * 0.1)).sprite(VfxSprite.SPIRAL, 0)
                .size(0.14F, 0.04F).spin(15).color(EV_CYAN, EV_PURPLE).alpha(1, 0).life(14).spawn();
        if (tick % 2 == 0) {
            VfxParticle.at(at).velocity(dir().scale(0.02)).size(0.05F, 0.01F).color(EV_CYAN, EV_DEEP).alpha(1, 0).life(16).spawn();
        }
    }
}
