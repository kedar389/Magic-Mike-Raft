package com.explorermaps;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Explorer Maps: one structure map per cartographer level, see {@link MapTrades}.
 * Server-only, clients join without it.
 */
@Mod(ExplorerMaps.MOD_ID)
public final class ExplorerMaps {

    public static final String MOD_ID = "explorer_maps";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ExplorerMaps() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (version, fromServer) -> true));
        MinecraftForge.EVENT_BUS.addListener((ServerStartedEvent event) -> MapTrades.load(event.getServer()));
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> MapTrades.clear());
    }
}
