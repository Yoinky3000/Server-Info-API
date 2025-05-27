package xyz.yoinky3000.server_info_api.api.routes;

import net.minecraft.server.MinecraftServer;
import xyz.yoinky3000.server_info_api.ServerInfoAPI;
import xyz.yoinky3000.server_info_api.annotations.RouteSettings;
import xyz.yoinky3000.server_info_api.api.APIRequest;
import xyz.yoinky3000.server_info_api.api.APIResponse;
import xyz.yoinky3000.server_info_api.api.APIRoute;
import xyz.yoinky3000.server_info_api.config.Players.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RouteSettings(path = "/players", requireMinecraftServer = true)
public class PlayersRoute implements APIRoute {
    @Override
    public APIResponse handle(APIRequest request, MinecraftServer server) {
        Map<String, Object> res = new HashMap<>();
        List<Player> totalPlayers = ServerInfoAPI.players.getData().list;
        res.put("totalPlayers", totalPlayers.size());
        List<Player> onlinePlayers = totalPlayers.stream().filter((p) -> server.getPlayerManager().getPlayer(p.uuid) != null).toList();
        res.put("onlinePlayers", onlinePlayers);

        return new APIResponse(APIResponse.HttpStatus.OK, APIResponse.MimeType.APPLICATION_JSON, ServerInfoAPI.GSON.toJson(res));
    }
}
