package coolaid.villagepillagetweaks.config;

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
                .title(Component.translatable("text.configScreen.title").withStyle(ChatFormatting.BOLD))
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
                .name(Component.translatable("text.configScreen.title"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("text.configCategory.handsOffMyBlock"))
                        .option(createMarkerItemOption())
                        .option(createWorkstationOption())
                        .option(createBedOption())
                        .option(createSneakOption())
                        .option(createActionBarOption())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("text.configCategory.dynamicRaidRadius"))
                        .option(createRaidRadiusOption())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("text.configCategory.namedTraders"))
                        .option(createComingSoonOption())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("text.configCategory.pathfindingTweaks"))
                        .option(createComingSoonOption())
                        .build())
                .build();
    }

    private static Option<String> createMarkerItemOption() {
        return Option.<String>createBuilder()
                .name(Component.translatable("text.configScreen.label"))
                .description(dev.isxander.yacl3.api.OptionDescription.of(Component.literal("minecraft:stick")))
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

    private static Option<String> createComingSoonOption() {
        return Option.<String>createBuilder()
                .name(Component.translatable("text.configOption.comingSoon"))
                .binding("-", () -> "-", value -> {
                })
                .controller(StringControllerBuilder::create)
                .build();
    }

    private static Option<Integer> createRaidRadiusOption() {
        return Option.<Integer>createBuilder()
                .name(Component.translatable("text.configOption.raidSpawnRadius"))
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

    private static Option<Boolean> createWorkstationOption() {
        return createBooleanOption(
                "text.configButton.workstationToggle",
                () -> ConfigManager.get().enableWorkstationMarking,
                value -> ConfigManager.get().enableWorkstationMarking = value,
                ConfigManager.defaults().enableWorkstationMarking
        );
    }

    private static Option<Boolean> createBedOption() {
        return createBooleanOption(
                "text.configButton.bedToggle",
                () -> ConfigManager.get().enableBedMarking,
                value -> ConfigManager.get().enableBedMarking = value,
                ConfigManager.defaults().enableBedMarking
        );
    }

    private static Option<Boolean> createSneakOption() {
        return createBooleanOption(
                "text.configButton.sneakToggle",
                () -> ConfigManager.get().requireSneaking,
                value -> ConfigManager.get().requireSneaking = value,
                ConfigManager.defaults().requireSneaking
        );
    }

    private static Option<Boolean> createActionBarOption() {
        return createBooleanOption(
                "text.configButton.showMarkings",
                () -> ConfigManager.get().actionBarMessages,
                value -> ConfigManager.get().actionBarMessages = value,
                ConfigManager.defaults().actionBarMessages
        );
    }

    private static Option<Boolean> createBooleanOption(
            String translationKey,
            java.util.function.Supplier<Boolean> getter,
            java.util.function.Consumer<Boolean> setter,
            boolean defaultValue
    ) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable(translationKey))
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }
}