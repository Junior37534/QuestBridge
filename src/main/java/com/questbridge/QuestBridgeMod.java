package com.questbridge;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;

public class QuestBridgeMod implements ClientModInitializer {
    private static final ControllerState controllerState = new ControllerState();
    private QuestHttpServer httpServer;
    private QuestWebSocketServer webSocketServer;
    private InputInjector inputInjector;

    private static QuestBridgeMod instance;

    @Override
    public void onInitializeClient() {
        instance = this;
        System.out.println("[QuestBridge] Inicializando mod client-side (Unobfuscated 26.1.2)...");

        httpServer = new QuestHttpServer();
        httpServer.start(7373);

        webSocketServer = new QuestWebSocketServer(controllerState);
        webSocketServer.start(7374);

        inputInjector = new InputInjector(controllerState);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            inputInjector.tick((Minecraft) (Object) client);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                Component message = Component.literal("§7[§aQuestBridge§7] ")
                    .append(Component.literal("Click here to open http://localhost:7373")
                        .withStyle(style -> style
                            .withClickEvent(new ClickEvent.OpenUrl(java.net.URI.create("http://localhost:7373")))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Open controller web panel in Quest Browser")))
                            .withUnderlined(true)
                            .withColor(TextColor.fromRgb(0x1C7BFF))
                        )
                    );
                client.player.sendSystemMessage(message);
            }
        });
    }

    public static void broadcastHaptic(float intensity, int durationMs) {
        if (instance != null && instance.webSocketServer != null) {
            instance.webSocketServer.broadcastHaptic(intensity, durationMs);
        }
    }

    public static ControllerState getControllerState() {
        return controllerState;
    }

    /**
     * Check if Controlify is handling input. Used by mixins to decide
     * whether to inject fallback input or let Controlify handle it.
     */
    public static boolean isControlifyMode() {
        // We check this lazily through a static accessor - the InputInjector
        // does the actual detection. If it hasn't been created yet, we return false.
        // This is safe because mixins that call this run on the render/game thread.
        try {
            return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("controlify");
        } catch (Exception e) {
            return false;
        }
    }
}
