package com.questbridge;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

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
