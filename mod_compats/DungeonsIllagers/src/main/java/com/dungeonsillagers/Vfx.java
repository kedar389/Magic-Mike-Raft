package com.dungeonsillagers;

import com.dungeonsillagers.client.VfxLibrary;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Dungeons II visual effects (the bosses' Niagara systems). The server sends one small packet per effect to everyone
 * tracking the caster; the client builds it from {@link VfxLibrary}. An effect can follow a hand of its caster.
 */
public final class Vfx {

    /** One Niagara system each, named after it. */
    public enum Effect {
        // Prime Enchanter (FrostbiteDesert_Boss/VFX)
        ICE_FIST_ON, LIGHTNING_FIST_ON, FROST_PUNCH_CHARGE, FROST_PUNCH, STAB_ICE, STAB_LIGHTNING, SLASH_ICE,
        SLASH_LIGHTNING, POWERED_UP, GROUND_SLAM, LAUNCH, LAUNCH_TELEGRAPH, LAUNCH_IMPACT, LIGHTNING_SLAM_CHARGE,
        LIGHTNING_SLAM, OVERCHARGE, OVERCHARGE_TELEGRAPH, OVERCHARGE_FLOOR, ICE_MINE_TELEGRAPH, ICE_MINE_EXPLOSION,
        FROST_SUMMON, FROST_SUMMON_MINION, INVULNERABLE,
        // Supreme Evoker (Illager_SupremeEvoker/VFX, Evoker/VFX)
        ORB_INITIATE, ORB_HIT, EVOKER_SUMMON_INITIATE, EVOKER_SUMMON, EVOKER_SUMMON_MINION, BUBBLE_INITIATE,
        BUBBLE_HIT, BUBBLE_SHATTER, TELEPORT_INITIATE, TELEPORT_OUT, TELEPORT_IN, FANG_INITIATE, FANG_CAST, FANG_RISE,
        EVOKER_DEATH
    }

    /** Where an effect sits: a fixed point, or a hand of its caster, followed while the effect lasts. */
    public enum Anchor { POINT, LEFT_HAND, RIGHT_HAND, BODY }

    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DungeonsIllagersMod.MOD_ID, "vfx"), () -> VERSION, VERSION::equals, VERSION::equals);

    private Vfx() {
    }

    static void register() {
        CHANNEL.registerMessage(0, Packet.class, Packet::encode, Packet::decode, Packet::handle);
    }

    /** Plays {@code effect} at {@code pos} facing {@code yaw}; {@code param} is effect-specific (radius, duration). */
    public static void play(Entity caster, Effect effect, Vec3 pos, float yaw, float param) {
        send(caster, new Packet(effect, Anchor.POINT, caster.getId(), pos, yaw, param));
    }

    /** Plays {@code effect} on {@code anchor} of {@code caster}. */
    public static void play(Entity caster, Effect effect, Anchor anchor, float param) {
        send(caster, new Packet(effect, anchor, caster.getId(), caster.position(), caster.getYRot(), param));
    }

    private static void send(Entity caster, Packet packet) {
        if (!caster.level().isClientSide()) {
            CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> caster), packet);
        }
    }

    /** Effect, anchor, caster, position, facing and one parameter. */
    public record Packet(Effect effect, Anchor anchor, int caster, Vec3 pos, float yaw, float param) {

        void encode(FriendlyByteBuf buf) {
            buf.writeEnum(this.effect);
            buf.writeEnum(this.anchor);
            buf.writeVarInt(this.caster);
            buf.writeDouble(this.pos.x);
            buf.writeDouble(this.pos.y);
            buf.writeDouble(this.pos.z);
            buf.writeFloat(this.yaw);
            buf.writeFloat(this.param);
        }

        static Packet decode(FriendlyByteBuf buf) {
            return new Packet(buf.readEnum(Effect.class), buf.readEnum(Anchor.class), buf.readVarInt(),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readFloat(), buf.readFloat());
        }

        void handle(Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VfxLibrary.play(this)));
            context.get().setPacketHandled(true);
        }
    }
}
