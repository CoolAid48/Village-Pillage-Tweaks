package coolaid.villagepillagetweaks.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class VillagePillageTweaksFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
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