package coolaid.villagepillagetweaks.neoforge.client;

import coolaid.villagepillagetweaks.config.HandsOffMyConfigManager;
import coolaid.villagepillagetweaks.neoforge.HandsOffMyBlockNeoForge;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ConfigScreenNeoForge extends Screen {
    private final Screen parent;
    private EditBox markerInput;
    private Component statusText;
    private int statusColor;
    private StringWidget titleWidget;

    public ConfigScreenNeoForge(Screen parent) {
        super(Component.translatable("text.configScreen.title"));
        this.parent = parent;
        this.statusText = Component.empty();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 100;
        boolean onServer = minecraft.player != null && !minecraft.hasSingleplayerServer();
        Tooltip unavailableTooltip = Tooltip.create(Component.translatable("message.actionbar.onServer"));

        // Marker item label
        this.addRenderableWidget(new StringWidget(
                centerX - 100, y, 200, 20,
                Component.translatable("text.configScreen.label"),
                this.font
        ));

        // EditBox for marker item
        markerInput = new EditBox(this.font, centerX - 100, y + 15, 200, 20,
                Component.literal(""));
        if (HandsOffMyConfigManager.get().markerItem != null) {
            markerInput.setValue(HandsOffMyConfigManager.get().markerItem.toString());
        }
        markerInput.active = !onServer;
        markerInput.setEditable(!onServer);
        if (onServer) {
            markerInput.setTooltip(unavailableTooltip);
        }
        this.addRenderableWidget(markerInput);

        y += 45;

        int buttonWidth = 130;
        int buttonHeight = 20;
        int buttonSpacing = 10;
        int startX = centerX - buttonWidth - (buttonSpacing / 2);

        Component enabled = Component.translatable("component.actionbar.enabled").withStyle(ChatFormatting.GREEN);
        Component disabled = Component.translatable("component.actionbar.disabled").withStyle(ChatFormatting.RED);

        // Top row: Workstation & Sneak toggles
        Button workstationToggle = Button.builder(
                Component.translatable("text.configButton.workstationToggle", Component.translatable(HandsOffMyConfigManager.get().enableWorkstationMarking ? "component.configButton.yes" : "component.configButton.no")),
                btn -> toggleWorkstation(btn, enabled, disabled)).bounds(startX, y, buttonWidth, buttonHeight).build();
        workstationToggle.active = !onServer;
        if (onServer) {
            workstationToggle.setTooltip(unavailableTooltip);
        }
        this.addRenderableWidget(workstationToggle);

        Button sneakToggle = Button.builder(
                Component.translatable("text.configButton.sneakToggle", Component.translatable(HandsOffMyConfigManager.get().requireSneaking ? "component.configButton.yes" : "component.configButton.no")),
                btn -> toggleSneak(btn, enabled, disabled)).bounds(startX + buttonWidth + buttonSpacing, y, buttonWidth, buttonHeight).build();
        sneakToggle.active = !onServer;
        if (onServer) {
            sneakToggle.setTooltip(unavailableTooltip);
        }
        this.addRenderableWidget(sneakToggle);

        // Second row: Bed & Pathfinding
        y += buttonHeight + 5;
        Button bedToggle = Button.builder(
                Component.translatable("text.configButton.bedToggle", Component.translatable(HandsOffMyConfigManager.get().enableBedMarking ? "component.configButton.yes" : "component.configButton.no")),
                btn -> toggleBed(btn, enabled, disabled)).bounds(startX, y, buttonWidth, buttonHeight).build();
        bedToggle.active = !onServer;
        if (onServer) {
            bedToggle.setTooltip(unavailableTooltip);
        }
        this.addRenderableWidget(bedToggle);

        Button tweakToggle = Button.builder(
                Component.translatable("text.configButton.tweakToggle", Component.translatable(HandsOffMyConfigManager.get().pathfindingTweaks ? "component.configButton.yes" : "component.configButton.no")),
                btn -> toggleTweak(btn, enabled, disabled)).bounds(startX + buttonWidth + buttonSpacing, y, buttonWidth, buttonHeight).build();
        tweakToggle.active = !onServer;
        if (onServer) {
            tweakToggle.setTooltip(unavailableTooltip);
        }
        this.addRenderableWidget(tweakToggle);

        // Third row: Action Bar toggle
        y += buttonHeight + 5;
        Button actionBarToggle = Button.builder(
                Component.translatable("text.configButton.showMarkings", Component.translatable(HandsOffMyConfigManager.get().actionBarMessages ? "component.configButton.yes" : "component.configButton.no")),
                btn -> toggleActionBar(btn, enabled, disabled)).bounds(startX, y, buttonWidth, buttonHeight).build();
        actionBarToggle.active = !onServer;
        if (onServer) {
            actionBarToggle.setTooltip(unavailableTooltip);
        }
        this.addRenderableWidget(actionBarToggle);

        // Save & Exit button
        Button saveAndExitButton = Button.builder(
                Component.translatable("text.configButton.save_and_exit"),
                btn -> {
                    saveConfig();
                    minecraft.setScreen(parent);
                }).bounds(centerX - 60, y + buttonHeight + 4, 120, 20).build();
        this.addRenderableWidget(saveAndExitButton);

        // Status text widget
        this.addRenderableWidget(new StringWidget(centerX - 100, y, 200, 20, statusText, font) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                if (!statusText.getString().isEmpty()) {
                    graphics.drawCenteredString(font, statusText, getX() + getWidth() / 2, getY(), statusColor);
                }
            }
        });
    }

    private void toggleWorkstation(Button btn, Component enabled, Component disabled) {
        HandsOffMyConfigManager.get().enableWorkstationMarking = !HandsOffMyConfigManager.get().enableWorkstationMarking;
        btn.setMessage(Component.translatable("text.configButton.workstationToggle",
                Component.translatable(HandsOffMyConfigManager.get().enableWorkstationMarking ? "component.configButton.yes" : "component.configButton.no")));
        HandsOffMyConfigManager.save();
        sendActionBarMessage("message.actionbar.workstationToggle",
                HandsOffMyConfigManager.get().enableWorkstationMarking ? enabled : disabled);
    }

    private void toggleSneak(Button btn, Component enabled, Component disabled) {
        HandsOffMyConfigManager.get().requireSneaking = !HandsOffMyConfigManager.get().requireSneaking;
        btn.setMessage(Component.translatable("text.configButton.sneakToggle",
                Component.translatable(HandsOffMyConfigManager.get().requireSneaking ? "component.configButton.yes" : "component.configButton.no")));
        HandsOffMyConfigManager.save();
        sendActionBarMessage("message.actionbar.sneakToggle",
                HandsOffMyConfigManager.get().requireSneaking ? enabled : disabled);
    }

    private void toggleBed(Button btn, Component enabled, Component disabled) {
        HandsOffMyConfigManager.get().enableBedMarking = !HandsOffMyConfigManager.get().enableBedMarking;
        btn.setMessage(Component.translatable("text.configButton.bedToggle",
                Component.translatable(HandsOffMyConfigManager.get().enableBedMarking ? "component.configButton.yes" : "component.configButton.no")));
        HandsOffMyConfigManager.save();
        sendActionBarMessage("message.actionbar.bedToggle",
                HandsOffMyConfigManager.get().enableBedMarking ? enabled : disabled);
    }

    private void toggleTweak(Button btn, Component enabled, Component disabled) {
        HandsOffMyConfigManager.get().pathfindingTweaks = !HandsOffMyConfigManager.get().pathfindingTweaks;
        btn.setMessage(Component.translatable("text.configButton.tweakToggle",
                Component.translatable(HandsOffMyConfigManager.get().pathfindingTweaks ? "component.configButton.yes" : "component.configButton.no")));
        HandsOffMyConfigManager.save();
        sendActionBarMessage("message.actionbar.tweakToggle",
                HandsOffMyConfigManager.get().pathfindingTweaks ? enabled : disabled);
    }

    private void toggleActionBar(Button btn, Component enabled, Component disabled) {
        HandsOffMyConfigManager.get().actionBarMessages = !HandsOffMyConfigManager.get().actionBarMessages;
        btn.setMessage(Component.translatable("text.configButton.showMarkings",
                Component.translatable(HandsOffMyConfigManager.get().actionBarMessages ? "component.configButton.yes" : "component.configButton.no")));
        HandsOffMyConfigManager.save();
        sendActionBarMessage("message.actionbar.showMarkings",
                HandsOffMyConfigManager.get().actionBarMessages ? enabled : disabled);
    }

    private void sendActionBarMessage(String key, Component message) {
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable(key).append(message), true);
        }
    }

    private void saveConfig() {
        String input = markerInput.getValue().trim();
        Identifier id = Identifier.tryParse(input);
        Item item = (id != null) ? BuiltInRegistries.ITEM.getOptional(id).orElse(null) : null;

        if (id == null || item == null || item == Items.AIR) {
            statusText = Component.translatable("message.actionbar.configFailed");
            statusColor = 0xFF5555;
            if (minecraft.player != null) minecraft.player.displayClientMessage(statusText, true);
            return;
        }

        if (HandsOffMyConfigManager.get().markerItem.equals(id)) {
            statusText = Component.literal("");
            return;
        }

        HandsOffMyConfigManager.get().markerItem = id;
        HandsOffMyConfigManager.save();
        HandsOffMyBlockNeoForge.MARKER_ITEM = item;

        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.translatable("message.actionbar.configSaved", item.getName()).withStyle(ChatFormatting.GREEN),
                    true
            );
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        super.render(graphics, mouseX, mouseY, delta);

        // Title widget
        Component title = Component.translatable("text.configScreen.title").withStyle(ChatFormatting.BOLD);
        int textWidth = this.font.width(title) + 25;
        if (titleWidget == null) {
            titleWidget = new StringWidget((this.width - textWidth) / 2, 10, textWidth, 9, title, font);
            this.addRenderableWidget(titleWidget);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
