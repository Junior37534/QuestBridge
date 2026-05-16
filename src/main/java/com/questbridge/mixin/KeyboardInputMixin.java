package com.questbridge.mixin;

import com.questbridge.ControllerState;
import com.questbridge.QuestBridgeMod;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fallback analog movement injection for when Controlify is NOT installed.
 * Replaces binary WASD movement with smooth analog stick values.
 * 
 * When Controlify is installed, this is skipped because Controlify provides
 * its own analog movement through its input system.
 */
@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends ClientInput {

    @Inject(method = "tick", at = @At("TAIL"))
    private void injectAnalogMovement(CallbackInfo ci) {
        // Skip if Controlify is handling controller input
        if (QuestBridgeMod.isControlifyMode()) return;
        
        ControllerState state = QuestBridgeMod.getControllerState();
        if (!state.isConnected()) return;

        float lx = state.getAxes()[0];
        float ly = state.getAxes()[1];
        
        float deadzone = 0.15f;
        
        // Se o joystick estiver fora da deadzone, substituímos o moveVector binário pelo analógico
        if (Math.abs(lx) > deadzone || Math.abs(ly) > deadzone) {
            // Minecraft moveVector: X é left/right (positivo é esquerda), Y é forward/backward (positivo é frente)
            // WebXR axes: lx (negativo esquerda, positivo direita), ly (negativo cima/frente, positivo baixo/trás)
            
            // Precisamos inverter e ajustar para o sistema do Minecraft
            float impulseLeft = -lx; // Left/Right
            float impulseForward = -ly; // Forward/Backward
            
            this.moveVector = new Vec2(impulseLeft, impulseForward);
        }
    }
}
