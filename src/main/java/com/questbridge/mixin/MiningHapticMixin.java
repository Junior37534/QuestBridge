package com.questbridge.mixin;

import com.questbridge.QuestBridgeMod;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MiningHapticMixin {
    private int hapticTick = 0;

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void onBlockBreaking(CallbackInfoReturnable<Boolean> cir) {
        // Send haptic every 4 ticks (approx 200ms) to avoid spamming network
        if (++hapticTick % 4 == 0) {
            QuestBridgeMod.broadcastHaptic(0.35f, 60);
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"))
    private void onStartBreaking(CallbackInfoReturnable<Boolean> cir) {
        // Immediate feedback for first hit (good for Creative mode)
        QuestBridgeMod.broadcastHaptic(0.5f, 100);
    }
}
