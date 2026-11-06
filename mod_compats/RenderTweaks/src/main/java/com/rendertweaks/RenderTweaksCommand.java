package com.rendertweaks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client command:
 * <ul>
 *   <li>{@code /rendertweaks} lists every fix and whether it is on</li>
 *   <li>{@code /rendertweaks <fix> <true|false>} switches one fix</li>
 *   <li>{@code /rendertweaks all <true|false>} switches every fix</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = RenderTweaks.MOD_ID, value = Dist.CLIENT)
public final class RenderTweaksCommand {

    private static final String ENABLED = "enabled";

    private RenderTweaksCommand() {}

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("rendertweaks").executes(RenderTweaksCommand::status);
        for (Tweak tweak : Tweak.values()) {
            root.then(Commands.literal(tweak.id())
                    .then(Commands.argument(ENABLED, BoolArgumentType.bool())
                            .executes(context -> set(context, tweak))));
        }
        root.then(Commands.literal("all")
                .then(Commands.argument(ENABLED, BoolArgumentType.bool())
                        .executes(RenderTweaksCommand::setAll)));
        dispatcher.register(root);
    }

    private static int set(CommandContext<CommandSourceStack> context, Tweak tweak) {
        tweak.setEnabled(BoolArgumentType.getBool(context, ENABLED));
        context.getSource().sendSuccess(() -> line(tweak), false);
        return 1;
    }

    private static int setAll(CommandContext<CommandSourceStack> context) {
        boolean enabled = BoolArgumentType.getBool(context, ENABLED);
        for (Tweak tweak : Tweak.values()) {
            tweak.setEnabled(enabled);
        }
        return status(context);
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        for (Tweak tweak : Tweak.values()) {
            context.getSource().sendSuccess(() -> line(tweak), false);
        }
        return Tweak.values().length;
    }

    private static Component line(Tweak tweak) {
        return Component.literal(tweak.id() + ": ")
                .append(tweak.isEnabled()
                        ? Component.literal("on").withStyle(ChatFormatting.GREEN)
                        : Component.literal("off").withStyle(ChatFormatting.RED))
                .append(Component.literal("  " + tweak.description()).withStyle(ChatFormatting.GRAY));
    }
}
