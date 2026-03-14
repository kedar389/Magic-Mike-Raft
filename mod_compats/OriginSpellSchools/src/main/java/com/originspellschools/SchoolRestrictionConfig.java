package com.originspellschools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Reads originspellschools.json from the config directory.
 *
 * Format:
 * {
 *   "default_behavior": "allow_all",   // or "block_all" for origins with no entry
 *   "notify_player": true,              // send chat message when a cast is blocked
 *   "origins": {
 *     "origins:blazeborn": ["fire"],
 *     "medievalorigins:high_elf": ["holy", "ice", "ender"],
 *     "origins:human": ["*"]            // wildcard = allow all schools
 *   }
 * }
 *
 * Valid school IDs: fire, ice, lightning, holy, ender, blood, evocation, nature, eldritch
 */
public class SchoolRestrictionConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("originspellschools.json");

    /** origin ResourceLocation → set of allowed school names (lowercase). "*" means allow-all. */
    private final Map<ResourceLocation, Set<String>> originToAllowedSchools;
    private final boolean notifyPlayer;
    private final String defaultBehavior; // "allow_all" or "block_all"

    private SchoolRestrictionConfig(
            Map<ResourceLocation, Set<String>> originToAllowedSchools,
            boolean notifyPlayer,
            String defaultBehavior) {
        this.originToAllowedSchools = originToAllowedSchools;
        this.notifyPlayer = notifyPlayer;
        this.defaultBehavior = defaultBehavior;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Returns true if the given origin is allowed to cast a spell of the given school.
     * @param originId  ResourceLocation of the player's current origin (e.g. "origins:blazeborn")
     * @param schoolId  lowercase school name as registered in ISB (e.g. "fire")
     */
    public boolean isSchoolAllowed(ResourceLocation originId, String schoolId) {
        Set<String> allowed = originToAllowedSchools.get(originId);

        if (allowed == null) {
            // Origin not listed in config
            return defaultBehavior.equals("allow_all");
        }
        if (allowed.contains("*")) {
            return true;
        }
        return allowed.contains(schoolId.toLowerCase(Locale.ROOT));
    }

    public boolean shouldNotifyPlayer() {
        return notifyPlayer;
    }

    public int getMappingCount() {
        return originToAllowedSchools.size();
    }

    // -----------------------------------------------------------------------
    // Load / save
    // -----------------------------------------------------------------------

    public static SchoolRestrictionConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            SchoolRestrictionConfig defaults = buildDefaults();
            defaults.save();
            return defaults;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(CONFIG_PATH.toFile()), StandardCharsets.UTF_8)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            return fromJson(json);
        } catch (Exception e) {
            OriginSpellSchoolsMod.LOGGER.error("[OriginSpellSchools] Failed to read config, using defaults: {}", e.getMessage());
            return buildDefaults();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(CONFIG_PATH.toFile()), StandardCharsets.UTF_8)) {
                GSON.toJson(toJson(), writer);
            }
        } catch (IOException e) {
            OriginSpellSchoolsMod.LOGGER.error("[OriginSpellSchools] Failed to save config: {}", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Serialization
    // -----------------------------------------------------------------------

    private static SchoolRestrictionConfig fromJson(JsonObject json) {
        boolean notify = json.has("notify_player") && json.get("notify_player").getAsBoolean();
        String defaultBehavior = json.has("default_behavior")
                ? json.get("default_behavior").getAsString()
                : "allow_all";

        Map<ResourceLocation, Set<String>> map = new LinkedHashMap<>();
        if (json.has("origins")) {
            JsonObject origins = json.getAsJsonObject("origins");
            for (Map.Entry<String, JsonElement> entry : origins.entrySet()) {
                ResourceLocation originId = new ResourceLocation(entry.getKey());
                Set<String> schools = new LinkedHashSet<>();
                for (JsonElement schoolEl : entry.getValue().getAsJsonArray()) {
                    schools.add(schoolEl.getAsString().toLowerCase(Locale.ROOT));
                }
                map.put(originId, schools);
            }
        }
        return new SchoolRestrictionConfig(map, notify, defaultBehavior);
    }

    private JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("default_behavior", defaultBehavior);
        root.addProperty("notify_player", notifyPlayer);

        // Add helpful comment via a _comment key (Gson preserves insertion order)
        root.addProperty("_comment",
            "Valid schools: fire, ice, lightning, holy, ender, blood, evocation, nature, eldritch. Use \"*\" to allow all schools.");

        JsonObject originsObj = new JsonObject();
        for (Map.Entry<ResourceLocation, Set<String>> entry : originToAllowedSchools.entrySet()) {
            var arr = new com.google.gson.JsonArray();
            entry.getValue().forEach(arr::add);
            originsObj.add(entry.getKey().toString(), arr);
        }
        root.add("origins", originsObj);
        return root;
    }

    // -----------------------------------------------------------------------
    // Default config matching the datapack we made
    // -----------------------------------------------------------------------

    private static SchoolRestrictionConfig buildDefaults() {
        Map<ResourceLocation, Set<String>> map = new LinkedHashMap<>();

        // Vanilla Origins
        put(map, "origins:arachnid",    "nature", "eldritch");
        put(map, "origins:avian",       "holy");
        put(map, "origins:blazeborn",   "fire");
        put(map, "origins:elytrian",    "lightning", "ender");
        put(map, "origins:enderian",    "ender");
        put(map, "origins:feline",      "nature", "ender");
        put(map, "origins:merling",     "ice", "nature");
        put(map, "origins:phantom",     "eldritch", "ender");
        put(map, "origins:shulk",       "ender", "evocation");
        put(map, "origins:human",       "*");  // Human can use all schools

        // Medieval Origins
        put(map, "medievalorigins:alfiq",         "ender", "evocation");
        put(map, "medievalorigins:arachnae",      "nature", "eldritch");
        put(map, "medievalorigins:banshee",       "eldritch", "blood");
        put(map, "medievalorigins:dwarf",         "fire", "evocation");
        put(map, "medievalorigins:fae",           "holy", "nature");
        put(map, "medievalorigins:goblin",        "evocation", "nature");
        put(map, "medievalorigins:gorgon",        "eldritch", "ender");
        put(map, "medievalorigins:high_elf",      "holy", "ice", "ender");
        put(map, "medievalorigins:incubus",       "blood", "evocation");
        put(map, "medievalorigins:moon_elf",      "ice", "ender");
        put(map, "medievalorigins:ogre",          "fire", "evocation");
        put(map, "medievalorigins:pixie",         "holy", "nature");
        put(map, "medievalorigins:plague_victim", "blood", "nature");
        put(map, "medievalorigins:revenant",      "blood", "eldritch");
        put(map, "medievalorigins:siren",         "nature", "ender");
        put(map, "medievalorigins:troll",         "nature", "eldritch");
        put(map, "medievalorigins:valkyrie",      "holy", "lightning");
        put(map, "medievalorigins:wood_elf",      "nature", "ender");
        put(map, "medievalorigins:yeti",          "ice");

        return new SchoolRestrictionConfig(map, true, "allow_all");
    }

    private static void put(Map<ResourceLocation, Set<String>> map, String origin, String... schools) {
        Set<String> set = new LinkedHashSet<>(Arrays.asList(schools));
        map.put(new ResourceLocation(origin), set);
    }
}
