package xyz.yoinky3000.server_info_api.handler;

import fi.iki.elonen.NanoHTTPD;
import org.reflections.Reflections;
import xyz.yoinky3000.server_info_api.ServerInfoAPI;
import xyz.yoinky3000.server_info_api.annotations.RouteSettings;
import xyz.yoinky3000.server_info_api.api.APIRequest;
import xyz.yoinky3000.server_info_api.api.APIResponse;
import xyz.yoinky3000.server_info_api.api.APIRoute;
import xyz.yoinky3000.server_info_api.api.RouteDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class APIHandler extends NanoHTTPD {
    private static final List<RouteDefinition> routeDefinitions = new ArrayList<>();

    public APIHandler(int port) throws IOException {
        super(port);
        this.port = port;
    }
    public int port;
    public enum RegisterResult {
        OVERRIDED, OCCUPIED, SUCCEED, FAILED
    }

    @Override
    public Response serve(IHTTPSession session) {
        String path = normalizePath(session.getUri());

        for (RouteDefinition def : routeDefinitions) {
            Map<String, String> params = def.match(path);
            if (params != null) {
                if (def.requireMinecraftServer && ServerInfoAPI.SERVER == null) return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Server not ready.");
                APIResponse res = def.handler.handle(new APIRequest(session.getUri(), session.getMethod().name(), params), ServerInfoAPI.SERVER);
                return newFixedLengthResponse(
                        Response.Status.lookup(res.getStatusCode()),
                        res.getMimeType(),
                        res.getBody()
                );
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
    }

    @Override
    public void start() throws IOException {
        String template = """
    
    =========================================================
    API running on port %d
    If you want to change the port, edit %s
    =========================================================
    """;
        ServerInfoAPI.LOGGER.info(String.format(template, ServerInfoAPI.config.getData().apiPort, ServerInfoAPI.config.getPath()));
        super.start();
    }

    public void registerInternalRoutes() {
        Reflections reflections = new Reflections("xyz.yoinky3000.server_info_api.api.routes");

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(RouteSettings.class)) {
            if (!APIRoute.class.isAssignableFrom(clazz)) continue;

            try {
                APIRoute handler = (APIRoute) clazz.getDeclaredConstructor().newInstance();
                registerRoute(handler, false, true);
            } catch (Exception e) {
                RouteSettings routeAnnotation = clazz.getAnnotation(RouteSettings.class);
                String path = routeAnnotation.path();
                ServerInfoAPI.LOGGER.error("Failed to register route: {}", path, e);
            }
        }
    }

    public static RegisterResult registerRoute(APIRoute handler, boolean override, boolean internal) {
        Class<?> clazz = handler.getClass();

        if (!clazz.isAnnotationPresent(RouteSettings.class)) {
            ServerInfoAPI.LOGGER.error("Cannot register route: Missing @RouteSettings annotation on {}", clazz.getName());
            return RegisterResult.FAILED;
        }

        RouteSettings annotation = clazz.getAnnotation(RouteSettings.class);
        String path = annotation.path();
        boolean requireMinecraftServer = annotation.requireMinecraftServer();

        return registerRoute(path, handler, requireMinecraftServer, override, internal);
    }

    public static RegisterResult registerRoute(String path, APIRoute handler, boolean requireMinecraftServer, boolean override, boolean internal) {
        path = normalizePath(path);
        Optional<RouteDefinition> routeStatus = routeAvailability(path);

        boolean overrided = false;
        if (routeStatus.isPresent()) {
            if (!override) {
                ServerInfoAPI.LOGGER.error("Cannot register route {}, occupied", path);
                return RegisterResult.OCCUPIED;
            } else {
                ServerInfoAPI.LOGGER.warn("Override enabled, removing old handler of route {}", path);
                routeDefinitions.remove(routeStatus.get());
                overrided = true;
            }
        }

        routeDefinitions.add(new RouteDefinition(path, handler, requireMinecraftServer));
        ServerInfoAPI.LOGGER.info("{}Registered {}route: {}", overrided ? "Overrided and " : "", !internal ? "External " : "", path);
        return overrided ? RegisterResult.OVERRIDED : RegisterResult.SUCCEED;
    }

    private static Optional<RouteDefinition> routeAvailability(String path) {
        return routeDefinitions.stream()
                .filter(def -> def.routePattern.equals(path))
                .findFirst();
    }

    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        path = path.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        path = path.replaceAll("/+", "/");
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            return "/";
        }
        return path;
    }
}
