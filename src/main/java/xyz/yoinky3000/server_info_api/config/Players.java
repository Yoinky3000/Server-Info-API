package xyz.yoinky3000.server_info_api.config;

import xyz.yoinky3000.server_info_api.ServerInfoAPI;
import xyz.yoinky3000.server_info_api.annotations.ConfigSettings;

import java.util.*;

@ConfigSettings(fileName = "players.json")
public class Players {
    public List<Player> list = new ArrayList<>();

    public static class Player {
        public String name;
        public UUID uuid;
        public Player (String name, UUID uuid) {
            this.name = name;
            this.uuid = uuid;
        }
    }

    public void updateOrInsertPlayer(String name, UUID uuid) {
        boolean found = false;
        for (Player player : list) {
            if (Objects.equals(player.uuid, uuid)) {
                if (!player.name.equals(name)) {
                    player.name = name;
                    ServerInfoAPI.LOGGER.debug("Updated player name for UUID " + uuid + " to " + name);
                } else {
                    ServerInfoAPI.LOGGER.debug("Player " + name + " (UUID " + uuid + ") already exists with same name. No update needed.");
                }
                found = true;
                break;
            }
        }
        if (!found) {
            Player newPlayer = new Player(name, uuid);
            list.add(newPlayer);
            ServerInfoAPI.LOGGER.debug("Added new player: " + name + " (UUID: " + uuid + ")");
        }
    }
    public Optional<Player> find(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }
        return list.stream()
                .filter(p -> p.uuid != null && p.uuid.equals(uuid)) // Ensure p.uuid is not null before equals
                .findFirst();
    }

    public Optional<Player> find(String criteria) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return Optional.empty();
        }
        return list.stream()
                .filter(p -> p.name != null && (p.name.equals(criteria) || p.uuid.toString().equals(criteria)))
                .findFirst();
    }
}
