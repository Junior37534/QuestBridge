package com.questbridge;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

/**
 * Virtual GLFW gamepad controller that translates QuestBridge's WebXR controller
 * state into standard GLFW gamepad data, making it visible to Controlify and
 * any other mod that polls GLFW joystick/gamepad functions.
 *
 * We claim GLFW_JOYSTICK_1 (slot 0) as our virtual controller. If a real 
 * controller is already present on that slot (unlikely on Amethyst-Android),
 * we'll still override it while connected.
 */
public class VirtualGlfwController {

    /** The GLFW joystick slot we claim */
    public static final int VIRTUAL_JID = GLFW.GLFW_JOYSTICK_1;

    /** Fake name for the virtual controller */
    public static final String CONTROLLER_NAME = "QuestBridge Virtual Controller";

    /** 
     * Fake GUID matching Xbox 360 controller format.
     * GLFW uses SDL2-format GUIDs, and Controlify uses them to identify controller type.
     * This GUID format makes Controlify treat it as an Xbox controller.
     * Format: bustype-vendor-product-version (16 hex chars each)
     */
    public static final String CONTROLLER_GUID = "030000005e040000fd02000000007200";

    /**
     * Fills a GLFWGamepadState struct with data from our ControllerState.
     * Maps Quest Touch controllers to standard Xbox-style layout:
     * 
     * Axes:
     *   GLFW_GAMEPAD_AXIS_LEFT_X  = Left thumbstick X
     *   GLFW_GAMEPAD_AXIS_LEFT_Y  = Left thumbstick Y
     *   GLFW_GAMEPAD_AXIS_RIGHT_X = Right thumbstick X
     *   GLFW_GAMEPAD_AXIS_RIGHT_Y = Right thumbstick Y
     *   GLFW_GAMEPAD_AXIS_LEFT_TRIGGER  = Left trigger (-1.0 to 1.0, GLFW convention)
     *   GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER = Right trigger (-1.0 to 1.0, GLFW convention)
     * 
     * Buttons:
     *   GLFW_GAMEPAD_BUTTON_A (South) = A (right controller)
     *   GLFW_GAMEPAD_BUTTON_B (East)  = B (right controller)
     *   GLFW_GAMEPAD_BUTTON_X (West)  = X (left controller)
     *   GLFW_GAMEPAD_BUTTON_Y (North) = Y (left controller)
     *   GLFW_GAMEPAD_BUTTON_LEFT_BUMPER  = Left grip/squeeze
     *   GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER = Right grip/squeeze
     *   GLFW_GAMEPAD_BUTTON_LEFT_THUMB   = Left thumbstick press
     *   GLFW_GAMEPAD_BUTTON_RIGHT_THUMB  = Right thumbstick press
     *   GLFW_GAMEPAD_BUTTON_START  = Menu button (if mapped)
     *   GLFW_GAMEPAD_BUTTON_BACK   = not mapped
     *   GLFW_GAMEPAD_BUTTON_GUIDE  = not mapped
     */
    public static boolean fillGamepadState(GLFWGamepadState state) {
        ControllerState cs = QuestBridgeMod.getControllerState();
        boolean connected = cs.isConnected();

        float[] axes = connected ? cs.getAxes() : new float[4];
        boolean[] buttons = connected ? cs.getButtons() : new boolean[8];
        float[] buttonValues = connected ? cs.getButtonValues() : new float[8];

        // Axes - direct mapping from our internal format
        // Left stick
        state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X, axes[0]);
        state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y, axes[1]);
        // Right stick
        state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X, axes[2]);
        state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y, axes[3]);
        
        // Triggers: GLFW convention is -1.0 (released) to 1.0 (fully pressed)
        // If disconnected, set to -1.0 (fully released)
        if (connected) {
            state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER, buttonValues[6] * 2.0f - 1.0f);
            state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER, buttonValues[7] * 2.0f - 1.0f);
        } else {
            state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER, -1.0f);
            state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER, -1.0f);
        }

        // Buttons
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_A, (byte)(buttons[0] ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE));
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_B, (byte)(buttons[1] ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE));
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_X, (byte)(buttons[2] ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE));
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_Y, (byte)(buttons[3] ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE));
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, (byte)(buttons[4] ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE));
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER, (byte)(buttons[5] ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE));
        
        boolean l3 = connected && cs.getLeftStickPressed();
        boolean r3 = connected && cs.getRightStickPressed();
        boolean menu = connected && cs.getMenuPressed();

        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB, (byte)(l3 ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE));
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB, (byte)(r3 ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE));
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_BACK, (byte) GLFW.GLFW_RELEASE);
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_START, (byte)(menu ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE));
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE, (byte) GLFW.GLFW_RELEASE);
        
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP, (byte) GLFW.GLFW_RELEASE);
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, (byte) GLFW.GLFW_RELEASE);
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN, (byte) GLFW.GLFW_RELEASE);
        state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT, (byte) GLFW.GLFW_RELEASE);

        return true;
    }

    /**
     * Returns whether our virtual controller is active.
     * We always return true so it's detected at startup.
     */
    public static boolean isPresent() {
        return true; 
    }
}
