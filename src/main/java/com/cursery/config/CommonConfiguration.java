package com.cursery.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

public class CommonConfiguration
{
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> excludedCUrses;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> excludedItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> disabledItems;
    public final ForgeConfigSpec.ConfigValue<Boolean>                excludeTreasure;
    public final ForgeConfigSpec.ConfigValue<Boolean>                debugTries;
    public final ForgeConfigSpec.ConfigValue<Boolean>                showDesc;
    public final ForgeConfigSpec.ConfigValue<Boolean>                visualSuccess;
    public final ForgeConfigSpec.ConfigValue<Integer>                basecursechance;
    public final ForgeConfigSpec                                     ForgeConfigSpecBuilder;

    protected CommonConfiguration(final ForgeConfigSpec.Builder builder)
    {
        builder.push("Cursery settings");
        //COnfig: affect players or all entities
        builder.comment("Should enchanted books show a hint for curse magic, default:true");
        showDesc = builder.define("showDesc", true);

        builder.comment(
          "Add a curse id here to exclude it from beeing applied. "
            + "To put multiple values seperate them by commas like this:  [\"minecraft:curse\", \"mod:curse;\"] ");
        excludedCUrses = builder.defineList("excludedCUrses",
          List.of("minecraft:vanishing_curse")
          , e -> e instanceof String && ((String) e).contains(":"));

        builder.comment(
                "Add item which will be not cursed");
        excludedItems = builder.defineList("excludedItems",
                List.of()
                , e -> e instanceof String && ((String) e).contains(":"));

        builder.comment("Should applying treasure enchants be excluded, default:false");
        excludeTreasure = builder.define("excludeTreasure", false);

        builder.comment("Base curse application chance, scales up the more enchants the item has. Default:5 %");
        basecursechance = builder.defineInRange("basecursechance", 5, 1, 100);

        builder.comment("Whether to log debug messages about curse rng beeing rolled, default = false");
        debugTries = builder.define("debugTries", false);

        builder.comment("List of items to disable, they get replaced by air e.g. [\"minecraft:elytra\"]");
        disabledItems = builder.defineList("disabledItems",
          Collections.EMPTY_LIST
          , e -> e instanceof String && ((String) e).contains(":"));

        builder.comment("Should enchanting success play a sound and show particles, default:true");
        visualSuccess = builder.define("visualSuccess", true);

        // Escapes the current category level
        builder.pop();
        ForgeConfigSpecBuilder = builder.build();
    }
}
