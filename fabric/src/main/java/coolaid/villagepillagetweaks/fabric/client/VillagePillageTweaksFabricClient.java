package coolaid.villagepillagetweaks.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import coolaid.villagepillagetweaks.config.HandsOffMyConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class VillagePillageTweaksFabricClient implements ClientModInitializer {

    public static KeyMapping openConfig;
    public static KeyMapping showMarkings;
    public static KeyMapping requireSneaking;

    private static final KeyMapping.Category DISABLE_CATEGORY =
            KeyMapping.Category.register(Identifier.parse("villagepillagetweaks"));

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        // Open Config, Show Markings, and Require Sneaking keybind registries

        openConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.handsoffmyblock.open_config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                DISABLE_CATEGORY
        ));

        showMarkings = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.handsoffmyblock.show_markings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                DISABLE_CATEGORY
        ));

        requireSneaking = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.handsoffmyblock.require_sneaking",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                DISABLE_CATEGORY
        ));

        // Show Markings Logic
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (showMarkings.consumeClick()) {
                if (isOnServer(client)) {
                    sendUnavailableMessage(client);
                    continue;
                }

                Component enabled = Component.translatable("component.actionbar.enabled").withStyle(ChatFormatting.GREEN);
                Component disabled = Component.translatable("component.actionbar.disabled").withStyle(ChatFormatting.RED);

                HandsOffMyConfigManager.get().actionBarMessages = !HandsOffMyConfigManager.get().actionBarMessages;
                HandsOffMyConfigManager.save();

                if (client.player != null) {
                    client.player.sendOverlayMessage(
                            Component.translatable("message.actionbar.showMarkings")
                                    .append(HandsOffMyConfigManager.get().actionBarMessages ? enabled : disabled)
                    );
                }
            }
        });
        // Require Sneaking Logic
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (requireSneaking.consumeClick()) {
                if (isOnServer(client)) {
                    sendUnavailableMessage(client);
                    continue;
                }

                Component enabled = Component.translatable("component.actionbar.enabled").withStyle(ChatFormatting.GREEN);
                Component disabled = Component.translatable("component.actionbar.disabled").withStyle(ChatFormatting.RED);

                HandsOffMyConfigManager.get().requireSneaking = !HandsOffMyConfigManager.get().requireSneaking;
                HandsOffMyConfigManager.save();

                if (client.player != null) {
                    client.player.sendOverlayMessage(
                            Component.translatable("message.actionbar.sneakToggle")
                                    .append(HandsOffMyConfigManager.get().requireSneaking ? enabled : disabled)
                    );
                }
            }
        });

        // Open Config Logic
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.screen != null) {
                return;
            }

            if (openConfig.consumeClick()) {
                Minecraft.getInstance().setScreen(new ConfigScreenFabric(Minecraft.getInstance().screen));
            }
        });
    }

    private static boolean isOnServer(Minecraft client) {
        return client.player != null && !client.hasSingleplayerServer();
    }

    private static void sendUnavailableMessage(Minecraft client) {
        if (client.player != null) {
            client.player.sendOverlayMessage(Component.translatable("message.actionbar.onServer").withStyle(ChatFormatting.DARK_RED));
        }
    }
}