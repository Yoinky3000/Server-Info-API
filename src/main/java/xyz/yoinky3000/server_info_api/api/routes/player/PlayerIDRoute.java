package xyz.yoinky3000.server_info_api.api.routes.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import xyz.yoinky3000.server_info_api.ServerInfoAPI;
import xyz.yoinky3000.server_info_api.annotations.RouteSettings;
import xyz.yoinky3000.server_info_api.api.APIRequest;
import xyz.yoinky3000.server_info_api.api.APIResponse;
import xyz.yoinky3000.server_info_api.api.APIRoute;
import xyz.yoinky3000.server_info_api.config.Players.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RouteSettings(path = "/player/:criteria", requireMinecraftServer = true)
public class PlayerIDRoute implements APIRoute {
    @Override
    public APIResponse handle(APIRequest request, MinecraftServer server) {
        String criteria = request.routeParams().get("criteria");

        Optional<Player> playerFind = ServerInfoAPI.players.getData().find(criteria);
        if (playerFind.isEmpty()) return new APIResponse(APIResponse.HttpStatus.NOT_FOUND, APIResponse.MimeType.TEXT_PLAIN, "No player match with search criteria - " + criteria);

        Player playerData = playerFind.get();
        PlayerEntity playerEntity = server.getPlayerManager().getPlayer(playerData.uuid);
        Map<String, Object> res = new HashMap<>();
        res.put("uuid", playerData.uuid);
        res.put("name", playerData.name);
        if (playerEntity == null) {
            res.put("online", false);
        } else {
            res.put("online", true);
            Map<String, Object> info = new HashMap<>();
            Vec3d playerPos = playerEntity.getPos();
            info.put("currentPos", Map.of("x", playerPos.x, "y", playerPos.y, "z", playerPos.z));
            ChunkPos playerChunkPos = playerEntity.getChunkPos();
            info.put("currentChunkPos", Map.of("x", playerChunkPos.x, "z", playerChunkPos.z));
            String dimensionKey = playerEntity.getWorld().getRegistryKey().getValue().toString();
            info.put("dimension", dimensionKey);
            res.put("info", info);
        }

        return new APIResponse(APIResponse.HttpStatus.OK, APIResponse.MimeType.APPLICATION_JSON, ServerInfoAPI.GSON.toJson(res));
    }
}
