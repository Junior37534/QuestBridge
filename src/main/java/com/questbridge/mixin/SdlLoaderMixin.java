package com.questbridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept Controlify's SDL native loader.
 * 
 * On Amethyst-Android, Controlify prefers its SDL3 backend. However, 
 * our virtual controller is currently implemented at the GLFW level.
 * 
 * By forcing the SDL load to fail, we trigger Controlify's fallback to its
 * GLFW backend, which will then use our GlfwJoystickMixin and see our 
 * virtual Quest controller.
 */
@Mixin(targets = "dev.isxander.controlify.driver.sdl.SDLNativesLoader", remap = false)
public class SdlLoaderMixin {
    
    /**
     * Overrides Controlify's SDL loading to always return false.
     * This forces Controlify to use its GLFW manager instead.
     */
    @Inject(method = "tryLoad", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onTryLoad(CallbackInfoReturnable<Boolean> cir) {
        // We only want to do this if we actually want to use the QuestBridge.
        // For now, we'll always do it to ensure the virtual controller is seen.
        System.out.println("[QuestBridge] Forcing SDL load failure in Controlify to enable GLFW fallback mode.");
        cir.setReturnValue(false);
    }
}
