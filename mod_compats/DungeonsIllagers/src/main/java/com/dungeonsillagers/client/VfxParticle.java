package com.dungeonsillagers.client;

import com.dungeonsillagers.DungeonsIllagersMod;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * One piece of a Dungeons II effect: a sprite from the effects atlas or a mesh (cube, open box, tube, sphere), glowing
 * (full bright, additive) and fading from one colour, size and alpha to another over its life, the way the Niagara
 * emitters' colour and size curves do. Built with chained setters, then {@link #spawn()}.
 */
public class VfxParticle extends Particle {

    static final ResourceLocation ATLAS = new ResourceLocation(DungeonsIllagersMod.MOD_ID, "textures/particle/vfx.png");
    private static final int FULL_BRIGHT = 0xF000F0;

    /** Additive, unculled, depth-tested but not depth-writing: the game's emissive particle materials. */
    static final ParticleRenderType GLOW = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textures) {
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, ATLAS);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return DungeonsIllagersMod.MOD_ID + ":glow";
        }
    };

    /** How the piece is drawn. */
    public enum Shape {
        /** Sprite facing the camera. */
        CAMERA,
        /** Sprite lying flat on the ground (rings, rune circles). */
        FLAT,
        /** Streak stretched back along its velocity by {@code height} (VelocityAligned sparks and shocks). */
        STREAK,
        /** Strip from the position to {@code position + beam}, turned to the camera (lightning, electric beams). */
        BEAM,
        /** Solid cube of edge {@code size} (ParticleCube). */
        CUBE,
        /** Four walls of half-width {@code size}, {@code height} tall, turned by {@code yaw} (SM_Cube_Bottomless). */
        WALLS,
        /** Open tube of radius {@code size}, {@code height} tall (SM_Pipe_Bottomless). */
        TUBE,
        /** Sphere of radius {@code size} (SphereFullUV). */
        SPHERE
    }

    private Shape shape = Shape.CAMERA;
    private VfxSprite sprite = VfxSprite.SOLID;
    private int frame;
    private boolean animated;
    private float size0 = 0.1F, size1 = 0.1F, height0, height1;
    private float r0 = 1, g0 = 1, b0 = 1, r1 = 1, g1 = 1, b1 = 1;
    private float alpha0 = 1, alpha1;
    /** Fraction of life spent fading in (Niagara alpha curves that start at 0). */
    private float fadeIn;
    private float spin, angle;
    private float yaw;
    private Vec3 beam = Vec3.ZERO;
    private float scroll;
    private boolean easeSize = true;

    public VfxParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.hasPhysics = false;
        this.gravity = 0;
        this.friction = 1.0F;
        this.xd = this.yd = this.zd = 0;
        this.lifetime = 20;
    }

    public static VfxParticle at(Vec3 pos) {
        return new VfxParticle(Minecraft.getInstance().level, pos.x, pos.y, pos.z);
    }

    public VfxParticle shape(Shape value) {
        this.shape = value;
        return this;
    }

    /** Sprite and frame; {@code frame} < 0 picks a random one. */
    public VfxParticle sprite(VfxSprite value, int index) {
        this.sprite = value;
        this.frame = index < 0 ? this.random.nextInt(value.frames) : index % value.frames;
        return this;
    }

    /** Plays the sprite's frames over the particle's life (flipbooks). */
    public VfxParticle animated() {
        this.animated = true;
        return this;
    }

    public VfxParticle size(float from, float to) {
        this.size0 = from;
        this.size1 = to;
        return this;
    }

    public VfxParticle height(float from, float to) {
        this.height0 = from;
        this.height1 = to;
        return this;
    }

    /** Size changes linearly instead of easing out. */
    public VfxParticle linear() {
        this.easeSize = false;
        return this;
    }

    public VfxParticle color(int from, int to) {
        this.r0 = (from >> 16 & 255) / 255.0F;
        this.g0 = (from >> 8 & 255) / 255.0F;
        this.b0 = (from & 255) / 255.0F;
        this.r1 = (to >> 16 & 255) / 255.0F;
        this.g1 = (to >> 8 & 255) / 255.0F;
        this.b1 = (to & 255) / 255.0F;
        return this;
    }

    public VfxParticle color(int rgb) {
        return color(rgb, rgb);
    }

    public VfxParticle alpha(float from, float to) {
        this.alpha0 = from;
        this.alpha1 = to;
        return this;
    }

    public VfxParticle fadeIn(float fraction) {
        this.fadeIn = fraction;
        return this;
    }

    public VfxParticle life(int ticks) {
        this.lifetime = Math.max(1, ticks);
        return this;
    }

    public VfxParticle velocity(Vec3 v) {
        this.xd = v.x;
        this.yd = v.y;
        this.zd = v.z;
        return this;
    }

    public VfxParticle gravity(float value) {
        this.gravity = value;
        return this;
    }

    /** Velocity kept per tick (1 = none lost). */
    public VfxParticle drag(float value) {
        this.friction = value;
        return this;
    }

    public VfxParticle spin(float degreesPerTick) {
        this.spin = degreesPerTick * Mth.DEG_TO_RAD;
        this.angle = this.random.nextFloat() * Mth.TWO_PI;
        return this;
    }

    public VfxParticle yaw(float degrees) {
        this.yaw = degrees * Mth.DEG_TO_RAD;
        return this;
    }

    public VfxParticle beam(Vec3 offset) {
        this.beam = offset;
        return this;
    }

    /** Texture scroll speed around a tube, in turns per tick (M_WhirlWind). */
    public VfxParticle scroll(float turnsPerTick) {
        this.scroll = turnsPerTick;
        return this;
    }

    public VfxParticle spawn() {
        this.oRoll = this.roll = this.angle;
        Minecraft.getInstance().particleEngine.add(this);
        return this;
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        this.roll += this.spin;
        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return GLOW;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    /** Big meshes and beams reach far past their position; never frustum-cull them by it. */
    @Override
    public boolean shouldCull() {
        return this.shape == Shape.CAMERA || this.shape == Shape.STREAK || this.shape == Shape.CUBE;
    }

    // ---- drawing ---------------------------------------------------------------------------------

    @Override
    public void render(VertexConsumer out, Camera camera, float partialTick) {
        float t = Math.min(1.0F, (this.age + partialTick) / this.lifetime);
        float grow = this.easeSize ? 1 - (1 - t) * (1 - t) * (1 - t) : t;
        float size = Mth.lerp(grow, this.size0, this.size1);
        float height = Mth.lerp(grow, this.height0, this.height1);
        float alpha = Mth.lerp(t, this.alpha0, this.alpha1);
        if (this.fadeIn > 0 && t < this.fadeIn) {
            alpha *= t / this.fadeIn;
        }
        if (alpha <= 0.003F || size <= 0) {
            return;
        }
        Color c = new Color(Mth.lerp(t, this.r0, this.r1), Mth.lerp(t, this.g0, this.g1), Mth.lerp(t, this.b0, this.b1), alpha);
        Vec3 cam = camera.getPosition();
        Vector3f p = new Vector3f((float) (Mth.lerp(partialTick, this.xo, this.x) - cam.x),
                (float) (Mth.lerp(partialTick, this.yo, this.y) - cam.y), (float) (Mth.lerp(partialTick, this.zo, this.z) - cam.z));
        int index = this.animated ? Math.min(this.sprite.frames - 1, (int) (t * this.sprite.frames)) : this.frame;
        float[] uv = this.sprite.uv(index);
        float roll = Mth.lerp(partialTick, this.oRoll, this.roll);
        switch (this.shape) {
            case CAMERA -> {
                Quaternionf rot = new Quaternionf(camera.rotation()).rotateZ(roll);
                quad(out, c, uv, corner(p, rot, -size, -size), corner(p, rot, -size, size), corner(p, rot, size, size), corner(p, rot, size, -size));
            }
            case FLAT -> {
                Quaternionf rot = new Quaternionf().rotateY(this.yaw + roll).rotateX(-Mth.HALF_PI);
                quad(out, c, uv, corner(p, rot, -size, -size), corner(p, rot, -size, size), corner(p, rot, size, size), corner(p, rot, size, -size));
            }
            case STREAK -> {
                Vector3f dir = new Vector3f((float) this.xd, (float) this.yd, (float) this.zd);
                if (dir.lengthSquared() < 1.0E-8F) {
                    dir.set(0, 1, 0);
                }
                dir.normalize();
                strip(out, c, uv, new Vector3f(p).sub(new Vector3f(dir).mul(Math.max(height, size))), p, size);
            }
            case BEAM -> strip(out, c, uv, p, new Vector3f(p).add((float) this.beam.x, (float) this.beam.y, (float) this.beam.z), size);
            case CUBE -> cube(out, c, uv, p, size / 2, roll);
            case WALLS -> walls(out, c, uv, p, size, height, 4, this.yaw + Mth.HALF_PI / 2, 0);
            case TUBE -> walls(out, c, uv, p, size, height, 16, roll, this.scroll * (this.age + partialTick));
            case SPHERE -> sphere(out, c, uv, p, size);
        }
    }

    private record Color(float r, float g, float b, float a) {
    }

    private static Vector3f corner(Vector3f center, Quaternionf rot, float dx, float dy) {
        return new Vector3f(dx, dy, 0).rotate(rot).add(center);
    }

    private static void vertex(VertexConsumer out, Vector3f p, float u, float v, Color c) {
        out.vertex(p.x, p.y, p.z).uv(u, v).color(c.r, c.g, c.b, c.a).uv2(FULL_BRIGHT).endVertex();
    }

    /** Quad a-b-c-d with the sprite's rect mapped u0v1, u0v0, u1v0, u1v1. */
    private static void quad(VertexConsumer out, Color c, float[] uv, Vector3f a, Vector3f b, Vector3f cc, Vector3f d) {
        vertex(out, a, uv[0], uv[3], c);
        vertex(out, b, uv[0], uv[1], c);
        vertex(out, cc, uv[2], uv[1], c);
        vertex(out, d, uv[2], uv[3], c);
    }

    /** A camera-facing strip from {@code from} to {@code to}, {@code width} wide; the sprite's u runs along it. */
    private static void strip(VertexConsumer out, Color c, float[] uv, Vector3f from, Vector3f to, float width) {
        Vector3f axis = new Vector3f(to).sub(from);
        Vector3f mid = new Vector3f(from).add(to).mul(0.5F);
        Vector3f side = new Vector3f(axis).cross(mid).normalize(width / 2);
        if (!Float.isFinite(side.x)) {
            return;
        }
        vertex(out, new Vector3f(from).add(side), uv[0], uv[1], c);
        vertex(out, new Vector3f(from).sub(side), uv[0], uv[3], c);
        vertex(out, new Vector3f(to).sub(side), uv[2], uv[3], c);
        vertex(out, new Vector3f(to).add(side), uv[2], uv[1], c);
    }

    private static void cube(VertexConsumer out, Color c, float[] uv, Vector3f p, float h, float roll) {
        Quaternionf rot = new Quaternionf().rotateY(roll).rotateX(roll * 0.7F);
        Vector3f[] v = new Vector3f[8];
        for (int i = 0; i < 8; i++) {
            v[i] = new Vector3f((i & 1) == 0 ? -h : h, (i & 2) == 0 ? -h : h, (i & 4) == 0 ? -h : h).rotate(rot).add(p);
        }
        int[][] faces = {{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
        for (int[] f : faces) {
            quad(out, c, uv, v[f[0]], v[f[1]], v[f[2]], v[f[3]]);
        }
    }

    /**
     * Open walls around the vertical axis: {@code sides} = 4 is a box, more is a tube. The sprite's v runs bottom (v0,
     * opaque in the gradient) to top; its u wraps once around, offset by {@code scroll} turns.
     */
    private static void walls(VertexConsumer out, Color c, float[] uv, Vector3f p, float radius, float height, int sides,
            float turn, float scroll) {
        float corner = sides == 4 ? radius * Mth.SQRT_OF_TWO : radius;
        for (int i = 0; i < sides; i++) {
            float a0 = turn + Mth.TWO_PI * i / sides, a1 = turn + Mth.TWO_PI * (i + 1) / sides;
            float u0 = Mth.lerp(frac(i / (float) sides + scroll), uv[0], uv[2]);
            float u1 = u0 + (uv[2] - uv[0]) / sides;
            Vector3f b0 = new Vector3f(Mth.cos(a0) * corner, 0, Mth.sin(a0) * corner).add(p);
            Vector3f b1 = new Vector3f(Mth.cos(a1) * corner, 0, Mth.sin(a1) * corner).add(p);
            vertex(out, b0, u0, uv[1], c);
            vertex(out, new Vector3f(b0).add(0, height, 0), u0, uv[3], c);
            vertex(out, new Vector3f(b1).add(0, height, 0), u1, uv[3], c);
            vertex(out, b1, u1, uv[1], c);
        }
    }

    private static float frac(float v) {
        return v - Mth.floor(v);
    }

    private static void sphere(VertexConsumer out, Color c, float[] uv, Vector3f p, float radius) {
        int rings = 6, segments = 10;
        for (int i = 0; i < rings; i++) {
            float t0 = Mth.PI * i / rings - Mth.HALF_PI, t1 = Mth.PI * (i + 1) / rings - Mth.HALF_PI;
            for (int j = 0; j < segments; j++) {
                float a0 = Mth.TWO_PI * j / segments, a1 = Mth.TWO_PI * (j + 1) / segments;
                quad(out, c, uv, onSphere(p, radius, t0, a0), onSphere(p, radius, t1, a0), onSphere(p, radius, t1, a1), onSphere(p, radius, t0, a1));
            }
        }
    }

    private static Vector3f onSphere(Vector3f p, float r, float lat, float lon) {
        return new Vector3f(Mth.cos(lat) * Mth.cos(lon) * r, Mth.sin(lat) * r, Mth.cos(lat) * Mth.sin(lon) * r).add(p);
    }
}
