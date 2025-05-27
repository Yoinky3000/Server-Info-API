package xyz.yoinky3000.server_info_api.handler;

import xyz.yoinky3000.server_info_api.ServerInfoAPI;
import xyz.yoinky3000.server_info_api.annotations.ConfigSettings;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConfigHandler<T> {
    private T instance;
    private final Class<T> clazz;
    private final String fileName;
    private final File configFile;
    public ConfigHandler(Class<T> as) {
        if (!as.isAnnotationPresent(ConfigSettings.class)) {
            ServerInfoAPI.LOGGER.error("Cannot identify config settings: Missing @ConfigSettings annotation on {}", as.getName());
            throw new IllegalArgumentException("Missing @ConfigSettings annotation on " + as.getName());
        }
        clazz = as;
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();
        } catch (NoSuchMethodException e) {
            ServerInfoAPI.LOGGER.error("Class {} must have a public no-argument constructor for configuration.", as.getName(), e);
            throw new RuntimeException("Configuration class " + as.getName() + " must have a public no-argument constructor.", e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            ServerInfoAPI.LOGGER.error("Failed to create instance of config class {}.", as.getName(), e);
            throw new RuntimeException("Failed to instantiate configuration class " + as.getName(), e);
        }
        ConfigSettings annotation = clazz.getAnnotation(ConfigSettings.class);
        fileName = annotation.fileName();
        configFile = new File(getPath());
    }
    public T getData() {
        return this.instance;
    }
    public String getPath() {
        return "config/Server Info API/" + fileName;
    }

    public void load() {
        if (!configFile.exists()) {
            ServerInfoAPI.LOGGER.warn("Config file {} not exist, creating", getPath());
            save();
        } else {
            try (Reader reader = new FileReader(configFile)) {
                instance = ServerInfoAPI.GSON.fromJson(reader, clazz);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ServerInfoAPI.LOGGER.debug("Loaded config:\n{}", ServerInfoAPI.GSON.toJson(instance));
    }

    public void save() {
        try {
            configFile.getParentFile().mkdirs(); // make sure config/ exists
            try (Writer writer = new FileWriter(configFile)) {
                ServerInfoAPI.GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
