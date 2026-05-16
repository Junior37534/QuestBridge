package com.questbridge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ControllerState {
    // Volatile for safe read from game tick thread without locking
    private volatile float[] axes = new float[4];     // LX, LY, RX, RY
    private volatile boolean[] buttons = new boolean[8]; // A,B,X,Y,LB,RB,LT,RT
    private volatile float[] buttonValues = new float[8]; // analog values (triggers)
    private volatile boolean connected = false;
    private volatile long lastUpdate = 0;
    
    // Additional buttons for thumbstick presses and menu
    private volatile boolean leftStickPressed = false;
    private volatile boolean rightStickPressed = false;
    private volatile boolean menuPressed = false;

    public synchronized void update(JsonObject data) {
        float[] newAxes = new float[4];
        boolean[] newButtons = new boolean[8];
        float[] newButtonValues = new float[8];
        boolean newLeftStickPressed = false;
        boolean newRightStickPressed = false;
        boolean newMenuPressed = false;

        if (data.has("left") && data.get("left").isJsonObject()) {
            JsonObject left = data.getAsJsonObject("left");
            if (left.has("axes")) {
                JsonArray a = left.getAsJsonArray("axes");
                if (a.size() >= 4) {
                    newAxes[0] = a.get(2).getAsFloat(); // Thumbstick X is usually index 2 in WebXR
                    newAxes[1] = a.get(3).getAsFloat(); // Thumbstick Y is usually index 3
                }
            }
            if (left.has("buttons")) {
                JsonArray b = left.getAsJsonArray("buttons");
                // Quest Touch left controller WebXR button layout:
                // [0] = trigger, [1] = squeeze/grip, [2] = unused, [3] = thumbstick press, [4] = X, [5] = Y
                if (b.size() > 0) {
                    JsonObject trigger = b.get(0).getAsJsonObject();
                    newButtons[6] = trigger.get("pressed").getAsBoolean();
                    newButtonValues[6] = trigger.get("value").getAsFloat();
                }
                if (b.size() > 1) {
                    JsonObject grip = b.get(1).getAsJsonObject();
                    newButtons[4] = grip.get("pressed").getAsBoolean(); // LB
                }
                if (b.size() > 3) {
                    JsonObject thumbstick = b.get(3).getAsJsonObject();
                    newLeftStickPressed = thumbstick.get("pressed").getAsBoolean();
                }
                if (b.size() > 4) {
                    JsonObject xBtn = b.get(4).getAsJsonObject();
                    newButtons[2] = xBtn.get("pressed").getAsBoolean(); // X
                }
                if (b.size() > 5) {
                    JsonObject yBtn = b.get(5).getAsJsonObject();
                    newButtons[3] = yBtn.get("pressed").getAsBoolean(); // Y
                }
                // Y button double-tap or menu can be mapped here
                if (b.size() > 6) {
                    JsonObject menuBtn = b.get(6).getAsJsonObject();
                    newMenuPressed = menuBtn.get("pressed").getAsBoolean();
                }
            }
        }

        if (data.has("right") && data.get("right").isJsonObject()) {
            JsonObject right = data.getAsJsonObject("right");
            if (right.has("axes")) {
                JsonArray a = right.getAsJsonArray("axes");
                if (a.size() >= 4) {
                    newAxes[2] = a.get(2).getAsFloat();
                    newAxes[3] = a.get(3).getAsFloat();
                }
            }
            if (right.has("buttons")) {
                JsonArray b = right.getAsJsonArray("buttons");
                // Quest Touch right controller WebXR button layout:
                // [0] = trigger, [1] = squeeze/grip, [2] = unused, [3] = thumbstick press, [4] = A, [5] = B
                if (b.size() > 0) {
                    JsonObject trigger = b.get(0).getAsJsonObject();
                    newButtons[7] = trigger.get("pressed").getAsBoolean();
                    newButtonValues[7] = trigger.get("value").getAsFloat();
                }
                if (b.size() > 1) {
                    JsonObject grip = b.get(1).getAsJsonObject();
                    newButtons[5] = grip.get("pressed").getAsBoolean(); // RB
                }
                if (b.size() > 3) {
                    JsonObject thumbstick = b.get(3).getAsJsonObject();
                    newRightStickPressed = thumbstick.get("pressed").getAsBoolean();
                }
                if (b.size() > 4) {
                    JsonObject aBtn = b.get(4).getAsJsonObject();
                    newButtons[0] = aBtn.get("pressed").getAsBoolean(); // A
                }
                if (b.size() > 5) {
                    JsonObject bBtn = b.get(5).getAsJsonObject();
                    newButtons[1] = bBtn.get("pressed").getAsBoolean(); // B
                }
            }
        }

        this.axes = newAxes;
        this.buttons = newButtons;
        this.buttonValues = newButtonValues;
        this.leftStickPressed = newLeftStickPressed;
        this.rightStickPressed = newRightStickPressed;
        this.menuPressed = newMenuPressed;
        this.connected = true;
        this.lastUpdate = System.currentTimeMillis();
    }

    public synchronized void setDisconnected() {
        this.connected = false;
    }

    public float[] getAxes() { return axes; }
    public boolean[] getButtons() { return buttons; }
    public float[] getButtonValues() { return buttonValues; }
    public boolean getLeftStickPressed() { return leftStickPressed; }
    public boolean getRightStickPressed() { return rightStickPressed; }
    public boolean getMenuPressed() { return menuPressed; }
    public boolean isConnected() { 
        return connected && (System.currentTimeMillis() - lastUpdate < 2000); 
    }
}
