package coolaid.villagepillagetweaks.neoforge.client;

import com.mojang.blaze3d.platform.InputConstants;
import coolaid.villagepillagetweaks.config.HandsOffMyConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

// NEOFORGE CLIENT SETUP IS SO CONFUSING BRUH
@EventBusSubscriber(modid = "villagepillagetweaks", value = Dist.CLIENT)
public final class VillagePillageTweaksNeoForgeClient {

    public static KeyMapping openConfig;
    public static KeyMapping showMarkings;
    public static KeyMapping requireSneaking;

    // NeoForge keybinds logic
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.parse("handsoffmyblock"));

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {

        openConfig = new KeyMapping(
                "key.handsoffmyblock.open_config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                CATEGORY
        );

        showMarkings = new KeyMapping(
                "key.handsoffmyblock.show_markings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY
        );

        requireSneaking = new KeyMapping(
                "key.handsoffmyblock.require_sneaking",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY
        );

        event.register(openConfig);
        event.register(showMarkings);
        event.register(requireSneaking);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.screen != null) {
            return;
        }

        if (openConfig.consumeClick()) {
            client.setScreen(new ConfigScreenNeoForge(client.screen));
        }

        toggleKey(
                showMarkings,
                "message.actionbar.showMarkings",
                HandsOffMyConfigManager.get().actionBarMessages,
                v -> HandsOffMyConfigManager.get().actionBarMessages = v,
                client
        );

        toggleKey(
                requireSneaking,
                "message.actionbar.sneakToggle",
                HandsOffMyConfigManager.get().requireSneaking,
                v -> HandsOffMyConfigManager.get().requireSneaking = v,
                client
        );
    }

    // Helper Methods
    private static void toggleKey(
            KeyMapping key, String messageKey, boolean currentValue, BooleanConsumer setter, Minecraft client
    ) {

        if (!key.consumeClick()) return;

        if (isOnServer(client)) {
            sendUnavailableMessage(client);
            return;
        }

        boolean newValue = !currentValue;

        setter.accept(newValue);
        HandsOffMyConfigManager.save();

        Component enabled = Component.translatable("component.actionbar.enabled").withStyle(ChatFormatting.GREEN);
        Component disabled = Component.translatable("component.actionbar.disabled").withStyle(ChatFormatting.RED);

        if (client.player != null) {
            client.player.displayClientMessage(
                    Component.translatable(messageKey).append(newValue ? enabled : disabled),
                    true
            );
        }
    }

    private static boolean isOnServer(Minecraft client) {
        return client.player != null && !client.hasSingleplayerServer();
    }

    private static void sendUnavailableMessage(Minecraft client) {
        if (client.player != null) {
            client.player.displayClientMessage(Component.translatable("message.actionbar.onServer").withStyle(ChatFormatting.DARK_RED), true);
        }
    }

    @FunctionalInterface
    private interface BooleanConsumer {
        void accept(boolean value);
    }
}