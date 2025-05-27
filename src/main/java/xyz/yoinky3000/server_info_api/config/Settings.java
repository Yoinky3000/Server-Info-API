package xyz.yoinky3000.server_info_api.config;

import xyz.yoinky3000.server_info_api.annotations.ConfigSettings;

@ConfigSettings(fileName = "settings.json")
public class Settings {
    public int apiPort = 8080;
}
