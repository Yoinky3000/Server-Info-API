package xyz.yoinky3000.server_info_api.api;

import net.minecraft.server.MinecraftServer;

public interface APIRoute {
    default APIResponse handle(APIRequest request) {
        return new APIResponse(APIResponse.HttpStatus.NOT_IMPLEMENTED, APIResponse.MimeType.TEXT_PLAIN, "Handle for this route not implemented");
    }
    default APIResponse handle(APIRequest request, MinecraftServer server) {
        return handle(request);
    }
}


