package com.questbridge.mixin;

import com.questbridge.VirtualGlfwController;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Mixin into LWJGL's GLFW class to make our Quest controller appear as a 
 * real GLFW joystick/gamepad. This is what allows Controlify (and vanilla
 * Minecraft's joystick detection) to see our virtual controller.
 * 
 * We intercept the Java-level GLFW wrapper methods before they call native code.
 * When our virtual controller is connected and the requested JID matches our slot,
 * we return synthetic data instead of calling the real native function.
 * 
 * All methods use remap=false since LWJGL is not obfuscated/remapped.
 */
@Mixin(targets = "org.lwjgl.glfw.GLFW", remap = false)
public class GlfwJoystickMixin {

    /**
     * Intercept glfwJoystickPresent to report our virtual controller as present.
     */
    @Inject(method = "glfwJoystickPresent", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onJoystickPresent(int jid, CallbackInfoReturnable<Boolean> cir) {
        if (jid == VirtualGlfwController.VIRTUAL_JID && VirtualGlfwController.isPresent()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Intercept glfwJoystickIsGamepad to report our controller as a gamepad.
     * This makes Controlify use GLFWGamepadDriver instead of GLFWJoystickDriver.
     */
    @Inject(method = "glfwJoystickIsGamepad", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onJoystickIsGamepad(int jid, CallbackInfoReturnable<Boolean> cir) {
        if (jid == VirtualGlfwController.VIRTUAL_JID && VirtualGlfwController.isPresent()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Intercept glfwGetGamepadState to fill with our controller data.
     * This is what Controlify's GLFWGamepadDriver calls every tick.
     */
    @Inject(method = "glfwGetGamepadState", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetGamepadState(int jid, GLFWGamepadState state, CallbackInfoReturnable<Boolean> cir) {
        if (jid == VirtualGlfwController.VIRTUAL_JID && VirtualGlfwController.isPresent()) {
            boolean filled = VirtualGlfwController.fillGamepadState(state);
            cir.setReturnValue(filled);
        }
    }

    /**
     * Intercept glfwGetGamepadName to return our controller name.
     */
    @Inject(method = "glfwGetGamepadName", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetGamepadName(int jid, CallbackInfoReturnable<String> cir) {
        if (jid == VirtualGlfwController.VIRTUAL_JID && VirtualGlfwController.isPresent()) {
            cir.setReturnValue(VirtualGlfwController.CONTROLLER_NAME);
        }
    }

    /**
     * Intercept glfwGetJoystickName to return our controller name.
     */
    @Inject(method = "glfwGetJoystickName", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetJoystickName(int jid, CallbackInfoReturnable<String> cir) {
        if (jid == VirtualGlfwController.VIRTUAL_JID && VirtualGlfwController.isPresent()) {
            cir.setReturnValue(VirtualGlfwController.CONTROLLER_NAME);
        }
    }

    /**
     * Intercept glfwGetJoystickGUID to return a valid GUID.
     * Controlify uses this to look up controller type in its database.
     */
    @Inject(method = "glfwGetJoystickGUID", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetJoystickGUID(int jid, CallbackInfoReturnable<String> cir) {
        if (jid == VirtualGlfwController.VIRTUAL_JID && VirtualGlfwController.isPresent()) {
            cir.setReturnValue(VirtualGlfwController.CONTROLLER_GUID);
        }
    }

    /**
     * Intercept glfwGetJoystickAxes for fallback joystick mode.
     * Returns a FloatBuffer with 6 axes (standard gamepad layout).
     */
    @Inject(method = "glfwGetJoystickAxes", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetJoystickAxes(int jid, CallbackInfoReturnable<FloatBuffer> cir) {
        if (jid == VirtualGlfwController.VIRTUAL_JID && VirtualGlfwController.isPresent()) {
            com.questbridge.ControllerState cs = com.questbridge.QuestBridgeMod.getControllerState();
            float[] axes = cs.getAxes();
            float[] bv = cs.getButtonValues();
            
            // Standard gamepad: 6 axes (LX, LY, RX, RY, LT, RT)
            FloatBuffer buf = org.lwjgl.BufferUtils.createFloatBuffer(6);
            buf.put(axes[0]);                     // Left X
            buf.put(axes[1]);                     // Left Y
            buf.put(axes[2]);                     // Right X
            buf.put(axes[3]);                     // Right Y
            buf.put(bv[6] * 2.0f - 1.0f);        // Left Trigger
            buf.put(bv[7] * 2.0f - 1.0f);        // Right Trigger
            buf.flip();
            cir.setReturnValue(buf);
        }
    }

    /**
     * Intercept glfwGetJoystickButtons for fallback joystick mode.
     * Returns a ByteBuffer with standard button states.
     */
    @Inject(method = "glfwGetJoystickButtons", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetJoystickButtons(int jid, CallbackInfoReturnable<ByteBuffer> cir) {
        if (jid == VirtualGlfwController.VIRTUAL_JID && VirtualGlfwController.isPresent()) {
            com.questbridge.ControllerState cs = com.questbridge.QuestBridgeMod.getControllerState();
            boolean[] buttons = cs.getButtons();
            
            // 15 buttons: A,B,X,Y,LB,RB,Back,Start,Guide,LThumb,RThumb,DU,DR,DD,DL
            ByteBuffer buf = org.lwjgl.BufferUtils.createByteBuffer(15);
            buf.put((byte)(buttons[0] ? 1 : 0));  // A
            buf.put((byte)(buttons[1] ? 1 : 0));  // B
            buf.put((byte)(buttons[2] ? 1 : 0));  // X
            buf.put((byte)(buttons[3] ? 1 : 0));  // Y
            buf.put((byte)(buttons[4] ? 1 : 0));  // LB
            buf.put((byte)(buttons[5] ? 1 : 0));  // RB
            buf.put((byte) 0);                      // Back
            buf.put((byte)(cs.getMenuPressed() ? 1 : 0)); // Start
            buf.put((byte) 0);                      // Guide
            buf.put((byte)(cs.getLeftStickPressed() ? 1 : 0));  // Left Thumb
            buf.put((byte)(cs.getRightStickPressed() ? 1 : 0)); // Right Thumb
            buf.put((byte) 0);                      // DPad Up
            buf.put((byte) 0);                      // DPad Right
            buf.put((byte) 0);                      // DPad Down
            buf.put((byte) 0);                      // DPad Left
            buf.flip();
            cir.setReturnValue(buf);
        }
    }

    /**
     * Intercept glfwGetJoystickHats for fallback joystick mode.
     * Returns an empty hats buffer (Quest Touch has no D-Pad/hat switch).
     */
    @Inject(method = "glfwGetJoystickHats", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetJoystickHats(int jid, CallbackInfoReturnable<ByteBuffer> cir) {
        if (jid == VirtualGlfwController.VIRTUAL_JID && VirtualGlfwController.isPresent()) {
            ByteBuffer buf = org.lwjgl.BufferUtils.createByteBuffer(1);
            buf.put((byte) 0); // centered
            buf.flip();
            cir.setReturnValue(buf);
        }
    }
}
