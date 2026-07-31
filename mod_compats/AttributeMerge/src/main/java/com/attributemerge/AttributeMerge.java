package com.attributemerge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Attribute Merge, folds duplicate RPG attributes into the Apothic Attributes set.
 *
 * <p>The pack ships five parallel attribute systems. Apothic Attributes is canonical (it owns the
 * Attributes GUI, the tooltips and the combat formulas). Of the duplicates:
 *
 * <ul>
 *   <li>the seven <b>Iron's Lib</b> ones are merged by {@code datapacks/Attribute_Merge/}, using
 *       Iron's Lib's own datapack remap, no code needed,</li>
 *   <li><b>Cataclysm</b> and <b>Obscure API</b> need their amounts rescaled / their operation
 *       changed, which that remap cannot do, that is what this mod is for (see {@link MergeRules}
 *       for the measured reasons),</li>
 * </ul>
 *
 * <p>Implementation is a single {@code ItemAttributeModifierEvent} listener
 * ({@link com.attributemerge.event.ItemAttributeMergeHandler}) that rewrites the modifier as the
 * item declares it. Nothing ticks.
 */
@Mod(AttributeMerge.MOD_ID)
public class AttributeMerge {

    public static final String MOD_ID = "attribute_merge";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public AttributeMerge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }

    /**
     * Resolve the merge table once, on both physical sides, the event fires client-side too, for
     * tooltips. Attributes are all registered by common setup.
     */
    private void onCommonSetup(final FMLCommonSetupEvent event) {
        MergeRules.resolve();
    }
}
