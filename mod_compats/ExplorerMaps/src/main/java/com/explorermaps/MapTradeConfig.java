package com.explorermaps;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraftforge.fml.loading.FMLPaths;

/** {@code config/explorer_maps.json}. Written from the jar's default on first start. */
public record MapTradeConfig(int maxDistanceBlocks, int attempts, List<Tier> tiers) {

    public record Tier(int level, String name, int emeralds, int maxUses, int xp, float priceMultiplier,
                       String decoration, List<Target> targets) {
    }

    public record Target(String name, List<String> structures) {
    }

    private static final String FILE_NAME = "explorer_maps.json";
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    static MapTradeConfig read() throws IOException {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        if (Files.notExists(path)) {
            try (InputStream in = MapTradeConfig.class.getResourceAsStream("/defaults/" + FILE_NAME)) {
                if (in == null) {
                    throw new IOException("default " + FILE_NAME + " missing from the jar");
                }
                Files.copy(in, path);
            }
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            return GSON.fromJson(reader, MapTradeConfig.class);
        }
    }
}
