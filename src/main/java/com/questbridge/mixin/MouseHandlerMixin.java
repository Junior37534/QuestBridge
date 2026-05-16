package com.questbridge.mixin;

import com.questbridge.ControllerState;
import com.questbridge.QuestBridgeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fallback camera injection for when Controlify is NOT installed.
 * Injects right-stick camera rotation directly into the mouse handler.
 * 
 * When Controlify is installed, this mixin still runs but is short-circuited
 * because Controlify handles look input through its own systems (reading
 * from the GLFW gamepad state we provide).
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
    private void injectCameraDelta(CallbackInfo ci) {
        // Skip if Controlify is handling controller input
        if (QuestBridgeMod.isControlifyMode()) return;
        
        ControllerState state = QuestBridgeMod.getControllerState();
        if (!state.isConnected()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.screen != null || client.player == null) return;

        float rx = state.getAxes()[2]; // Right stick X
        float ry = state.getAxes()[3]; // Right stick Y

        float deadzone = 0.1f;
        if (Math.abs(rx) > deadzone || Math.abs(ry) > deadzone) {
            float sensitivity = 5.0f;
            client.player.turn(rx * sensitivity, ry * sensitivity);
        }
    }
}
