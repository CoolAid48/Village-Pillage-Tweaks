package coolaid.villagepillagetweaks.neoforge.client;

import coolaid.villagepillagetweaks.config.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

// NEOFORGE CLIENT SETUP IS SO CONFUSING BRUH
@EventBusSubscriber(modid = "villagepillagetweaks", value = Dist.CLIENT)
public final class VillagePillageTweaksNeoForgeClient {

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
        ConfigManager.save();

        Component enabled = Component.translatable("component.actionbar.enabled").withStyle(ChatFormatting.GREEN);
        Component disabled = Component.translatable("component.actionbar.disabled").withStyle(ChatFormatting.RED);

        if (client.player != null) {
            client.player.sendOverlayMessage(
                    Component.translatable(messageKey).append(newValue ? enabled : disabled)
            );
        }
    }

    private static boolean isOnServer(Minecraft client) {
        return client.player != null && !client.hasSingleplayerServer();
    }

    private static void sendUnavailableMessage(Minecraft client) {
        if (client.player != null) {
            client.player.sendOverlayMessage(Component.translatable("message.actionbar.onServer").withStyle(ChatFormatting.DARK_RED));
        }
    }

    @FunctionalInterface
    private interface BooleanConsumer {
        void accept(boolean value);
    }
}