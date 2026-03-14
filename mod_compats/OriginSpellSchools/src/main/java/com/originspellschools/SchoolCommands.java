package com.originspellschools;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Provides two commands:
 *   /originschools reload         - reloads config from disk (OP level 2)
 *   /originschools check          - shows the current player's origin and allowed schools
 */
@Mod.EventBusSubscriber(modid = OriginSpellSchoolsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SchoolCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("originschools")
            .then(Commands.literal("reload")
                .requires(src -> src.hasPermission(2))
                .executes(SchoolCommands::reloadConfig))
            .then(Commands.literal("check")
                .executes(SchoolCommands::checkOrigin))
        );
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        try {
            SchoolRestrictionConfig newConfig = SchoolRestrictionConfig.load();
            // Rebuild the listener with fresh config
            OriginSpellSchoolsMod.onConfigReload(newConfig);
            ctx.getSource().sendSuccess(
                () -> Component.literal("[OriginSpellSchools] Config reloaded. " + newConfig.getMappingCount() + " origin mappings active."),
                true
            );
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("[OriginSpellSchools] Reload failed: " + e.getMessage()));
            return 0;
        }
    }

    private static int checkOrigin(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run as a player."));
            return 0;
        }

        String info = OriginSpellSchoolsMod.getOriginInfo(player);
        source.sendSuccess(() -> Component.literal(info), false);
        return 1;
    }
}
