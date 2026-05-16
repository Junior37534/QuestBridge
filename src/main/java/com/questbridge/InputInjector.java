package com.questbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.Inventory;

/**
 * Fallback input injector that directly presses Minecraft keys from controller state.
 * 
 * When Controlify is installed, this injector is DISABLED — Controlify reads the 
 * virtual GLFW gamepad state from our GlfwJoystickMixin and handles all input 
 * mapping, sensitivity, and remapping through its own UI.
 * 
 * When Controlify is NOT installed, this provides basic controller-to-keyboard
 * mapping so the mod works standalone.
 */
public class InputInjector {
    private final ControllerState state;
    private static final float DEADZONE = 0.3f;
    
    private boolean wasScrollUp = false;
    private boolean wasScrollDown = false;

    /** 
     * Whether Controlify (or another controller mod) is detected.
     * When true, we skip direct key injection and let the other mod handle it.
     */
    private boolean controlifyDetected = false;
    private boolean detectionDone = false;

    public InputInjector(ControllerState state) {
        this.state = state;
    }

    /**
     * Check once whether Controlify is loaded via Fabric Loader.
     */
    private void detectControlify() {
        if (detectionDone) return;
        detectionDone = true;
        try {
            controlifyDetected = net.fabricmc.loader.api.FabricLoader.getInstance()
                    .isModLoaded("controlify");
            if (controlifyDetected) {
                System.out.println("[QuestBridge] Controlify detected! Disabling direct input injection. " +
                        "Use Controlify's settings to remap controller buttons.");
            } else {
                System.out.println("[QuestBridge] Controlify not detected. Using built-in input mapping.");
            }
        } catch (Exception e) {
            controlifyDetected = false;
        }
    }

    private float lastHealth = -1;
    private int hapticCooldown = 0;

    public void tick(Minecraft client) {
        detectControlify();
        
        // --- Haptic Feedback Polling (Works even in Controlify mode) ---
        if (hapticCooldown > 0) hapticCooldown--;

        if (client.player != null) {
            float health = client.player.getHealth();
            if (lastHealth != -1 && health < lastHealth && hapticCooldown == 0) {
                // Player took damage
                QuestBridgeMod.broadcastHaptic(0.85f, 180);
                hapticCooldown = 10; // Prevent spamming within 0.5s
            }
            lastHealth = health;

            // Check if player is eating/drinking
            if (client.player.isUsingItem() && hapticCooldown == 0) {
                net.minecraft.world.item.ItemStack useItem = client.player.getUseItem();
                if (!useItem.isEmpty()) {
                    String anim = useItem.getUseAnimation().name();
                    if (anim.equals("EAT") || anim.equals("DRINK")) {
                        // Subtle vibration while eating/drinking
                        QuestBridgeMod.broadcastHaptic(0.25f, 50);
                        hapticCooldown = 5; 
                    }
                }
            }
        }

        if (!state.isConnected()) {
            return;
        }

        // When Controlify is handling input, we do nothing here for key injection.
        if (controlifyDetected) {
            return;
        }

        // --- Fallback: direct key injection when no controller mod is installed ---
        
        if (client.screen != null) {
            return;
        }

        Options options = client.options;
        float[] axes = state.getAxes();
        boolean[] buttons = state.getButtons();
        float[] buttonValues = state.getButtonValues();

        // Left stick -> WASD
        float lx = axes[0];
        float ly = axes[1];
        
        options.keyUp.setDown(ly < -DEADZONE);
        options.keyDown.setDown(ly > DEADZONE);
        options.keyLeft.setDown(lx < -DEADZONE);
        options.keyRight.setDown(lx > DEADZONE);

        // Buttons
        options.keyJump.setDown(buttons[0]); // A
        options.keyShift.setDown(buttons[1]); // B
        
        // Triggers -> Attack / Use
        options.keyAttack.setDown(buttonValues[7] > 0.5f); // RT
        options.keyUse.setDown(buttonValues[6] > 0.5f); // LT
        
        // Bumpers -> Hotbar scroll
        boolean isScrollUp = buttons[4]; // LB
        boolean isScrollDown = buttons[5]; // RB
        
        if ((isScrollUp && !wasScrollUp) || (isScrollDown && !wasScrollDown)) {
            if (client.player != null) {
                Inventory inv = client.player.getInventory();
                int current = inv.getSelectedSlot();
                int delta = (isScrollUp && !wasScrollUp) ? -1 : 1;
                inv.setSelectedSlot((current + delta + 9) % 9);
            }
        }
        
        wasScrollUp = isScrollUp;
        wasScrollDown = isScrollDown;
        
        // X button -> Inventory
        if (buttons[2] && client.player != null) {
            options.keyInventory.setDown(true);
        } else {
            options.keyInventory.setDown(false);
        }
    }

    /**
     * Whether Controlify is handling controller input for us.
     */
    public boolean isControlifyMode() {
        detectControlify();
        return controlifyDetected;
    }
}
