package me.coolaid.villagepillagetweaks.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ConfigScreenFactory {
    private ConfigScreenFactory() {
    }

    public static Screen create(Screen parent, Runnable onSave) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.screen.title").withStyle(ChatFormatting.BOLD))
                .category(mainCategory())
                .save(() -> {
                    ConfigManager.save();
                    onSave.run();
                })
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory mainCategory() {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("config.screen.title"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("config.category.handsOffMyBlock"))
                        .option(createMarkerItemOption())
                        .option(createWorkstationOption())
                        .option(createBedOption())
                        .option(createSneakOption())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("config.category.pathfindingChanges"))
                        .option(createPathfindingOption())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("config.category.miscTweaks"))
                        .option(createRaidRadiusOption())
                        .option(createNamedTradersOption())
                        .option(createNamedTraderLlamasOption())
                        .build())
                .build();
    }

    private static Option<String> createMarkerItemOption() {
        return Option.<String>createBuilder()
                .name(Component.translatable("config.button.label"))
                .description(dev.isxander.yacl3.api.OptionDescription.of(Component.translatable("config.description.markerItem")))
                .binding(
                        ConfigManager.defaults().markerItem.toString(),
                        () -> ConfigManager.get().markerItem.toString(),
                        value -> {
                            Identifier id = Identifier.tryParse(value);
                            Item item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                            if (id != null && item != null && item != Items.AIR) {
                                ConfigManager.get().markerItem = id;
                            }
                        }
                )
                .controller(StringControllerBuilder::create)
                .build();
    }

    private static Option<Integer> createRaidRadiusOption() {
        return Option.<Integer>createBuilder()
                .name(Component.translatable("config.button.raidSpawnRadius"))
                .description(dev.isxander.yacl3.api.OptionDescription.of(Component.translatable("config.description.raidSpawnRadius")))
                .binding(
                        ConfigManager.defaults().raidSpawnRadius,
                        () -> ConfigManager.get().raidSpawnRadius,
                        value -> ConfigManager.get().raidSpawnRadius = value
                )
                .controller(option -> IntegerSliderControllerBuilder.create(option)
                        .range(0, 96)
                        .step(1)
                        .formatValue(value -> Component.literal(String.valueOf(value))))
                .build();
    }

    private static Option<Boolean> createNamedTradersOption() {
        return createBooleanOption(
                "config.button.namedTraders",
                "config.description.namedTraders",
                () -> ConfigManager.get().namedTraders,
                value -> ConfigManager.get().namedTraders = value,
                ConfigManager.defaults().namedTraders
        );
    }

    private static Option<Boolean> createNamedTraderLlamasOption() {
        return createBooleanOption(
                "config.button.namedTraderLlamas",
                "config.description.namedTraderLlamas",
                () -> ConfigManager.get().namedTraderLlamas,
                value -> ConfigManager.get().namedTraderLlamas = value,
                ConfigManager.defaults().namedTraderLlamas
        );
    }

    private static Option<Boolean> createWorkstationOption() {
        return createBooleanOption(
                "config.button.workstationToggle",
                "config.description.workstationToggle",
                () -> ConfigManager.get().enableWorkstationMarking,
                value -> ConfigManager.get().enableWorkstationMarking = value,
                ConfigManager.defaults().enableWorkstationMarking
        );
    }

    private static Option<Boolean> createBedOption() {
        return createBooleanOption(
                "config.button.bedToggle",
                "config.description.bedToggle",
                () -> ConfigManager.get().enableBedMarking,
                value -> ConfigManager.get().enableBedMarking = value,
                ConfigManager.defaults().enableBedMarking
        );
    }

    private static Option<Boolean> createSneakOption() {
        return createBooleanOption(
                "config.button.sneakToggle",
                "config.description.sneakToggle",
                () -> ConfigManager.get().requireSneaking,
                value -> ConfigManager.get().requireSneaking = value,
                ConfigManager.defaults().requireSneaking
        );
    }

    private static Option<Boolean> createPathfindingOption() {
        return createBooleanOption(
                "config.button.tweakToggle",
                "config.description.trapdoorTweaks",
                () -> ConfigManager.get().pathfindingTweaks,
                value -> ConfigManager.get().pathfindingTweaks = value,
                ConfigManager.defaults().pathfindingTweaks
        );
    }

    private static Option<Boolean> createBooleanOption(
            String translationKey,
            String descriptionKey,
            java.util.function.Supplier<Boolean> getter,
            java.util.function.Consumer<Boolean> setter,
            boolean defaultValue
    ) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable(translationKey))
                .description(dev.isxander.yacl3.api.OptionDescription.of(Component.translatable(descriptionKey)))
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }
}