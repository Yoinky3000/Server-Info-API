package xyz.yoinky3000.server_info_api.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteDefinition {
    public final String routePattern;
    public final Pattern regex;
    public final List<String> paramNames;
    public final APIRoute handler;
    public final boolean requireMinecraftServer;

    public RouteDefinition(String routePattern, APIRoute handler, boolean requireMinecraftServer) {
        this.routePattern = routePattern;
        this.handler = handler;
        this.paramNames = new ArrayList<>();
        this.requireMinecraftServer = requireMinecraftServer;

        String[] parts = routePattern.replaceAll("^/+", "").replaceAll("/+$", "").split("/");
        StringBuilder regexBuilder = new StringBuilder("^/");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith(":")) {
                paramNames.add(part.substring(1));
                regexBuilder.append("([^/]+)");
            } else {
                regexBuilder.append(Pattern.quote(part));
            }
            if (i < parts.length - 1) {
                regexBuilder.append("/");
            }
        }
        regexBuilder.append("$");
        this.regex = Pattern.compile(regexBuilder.toString());
    }

    public Map<String, String> match(String path) {
        Matcher matcher = regex.matcher(path);
        if (!matcher.matches()) return null;

        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < paramNames.size(); i++) {
            params.put(paramNames.get(i), matcher.group(i + 1));
        }
        return params;
    }
}
