package gg.atn.client;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

public class MysticAutoflyClient implements ClientModInitializer {
	public static KeyBinding enabled;
	public static boolean active = true;
	private static int pendingCheckTicks = -1;


	@Override
	public void onInitializeClient() {
		enabled = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.mystic-autofly.toggle", // Translation key
				InputUtil.Type.KEYSYM, // KEYSYM for keyboard, MOUSE for mouse
				GLFW.GLFW_KEY_R, // Default key
				"key.categories.mystic-autofly" // Category
			));

		// Toggle `active` when the keybind is pressed
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;
			while (enabled.wasPressed()) {
				active = !active;
				client.player.sendMessage(Text.literal("MysticAutofly " + (active ? "enabled" : "disabled")), true);
			}
			// delayed execution
			if (pendingCheckTicks > 0) {
				pendingCheckTicks--;
			} else if (pendingCheckTicks == 0) {
				pendingCheckTicks = -1;
				checkAndEnableFlight(client);
			}
		});

		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> pendingCheckTicks = 15);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> pendingCheckTicks = 20);

	}
	private void checkAndEnableFlight(MinecraftClient client) {
			if (!active) return; // don't do anything if disabled
			if (client.player == null) return; // no null players
			if (client.getServer() != null) return; // running an integrated server (singleplayer) — skip
			if ("minecraft:pinataworld".equals(client.player.getWorld().getRegistryKey().getValue().toString())) return; // don't try enabling in pinata
			if (client.player.getAbilities().flying) return; // do nothing if already flying
			if (client.player.getAbilities().allowFlying) return; // do nothing if already flying
			client.getNetworkHandler().sendChatCommand("fly");
	}
}