package xyz.yoinky3000.server_info_api.api;

import java.util.Map;

public record APIRequest(String uri, String method, Map<String, String> routeParams) {
}
