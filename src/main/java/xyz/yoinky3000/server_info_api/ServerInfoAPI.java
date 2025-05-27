package xyz.yoinky3000.server_info_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.yoinky3000.server_info_api.api.APIRoute;
import xyz.yoinky3000.server_info_api.config.Players;
import xyz.yoinky3000.server_info_api.config.Settings;
import xyz.yoinky3000.server_info_api.handler.APIHandler;
import xyz.yoinky3000.server_info_api.handler.ConfigHandler;

import java.io.IOException;
import java.util.UUID;

public class ServerInfoAPI implements ModInitializer {
	public static final String MOD_ID = "ServerInfoAPI";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static MinecraftServer SERVER;
	public static ConfigHandler<Settings> config = new ConfigHandler<>(Settings.class);
	public static ConfigHandler<Players> players = new ConfigHandler<>(Players.class);

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			LOGGER.warn("Client environment detected — skipping mod initialization.");
			return;
		}
		LOGGER.info("Server Info API Starting!");
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			SERVER = server;
			LOGGER.info("Sever reference initialized");
		});
		config.load();
		players.load();

		try {
			APIHandler apiHandler = new APIHandler(config.getData().apiPort);
			apiHandler.registerInternalRoutes();
			apiHandler.start();
		} catch (IOException e) {
			throw new RuntimeException("Cannot start API server: " + e);
		}
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;

			UUID playerUuid = player.getUuid();
			String playerName = player.getName().getString();

			LOGGER.debug("Player {} (UUID: {}) joined, updating player list", playerName, playerUuid);
			players.getData().updateOrInsertPlayer(playerName, playerUuid);
			players.save();
		});
	}
	public static APIHandler.RegisterResult registerRoute(APIRoute route) {
		return APIHandler.registerRoute(route, false, false);
	}
	public static APIHandler.RegisterResult registerRoute(APIRoute route, boolean override) {
		return APIHandler.registerRoute(route, override, false);
	}
	public static APIHandler.RegisterResult registerRoute(String path, APIRoute route) {
		return APIHandler.registerRoute(path, route, false, false, false);
	}
	public static APIHandler.RegisterResult registerRoute(String path, APIRoute route, boolean override) {
		return APIHandler.registerRoute(path, route, false, override, false);
	}
	public static APIHandler.RegisterResult registerRoute(String path, APIRoute route, boolean requireMinecraftServer, boolean override) {
		return APIHandler.registerRoute(path, route, requireMinecraftServer, override, false);
	}
}