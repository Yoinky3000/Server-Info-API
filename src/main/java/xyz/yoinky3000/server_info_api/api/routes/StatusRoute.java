package xyz.yoinky3000.server_info_api.api.routes;

import net.minecraft.server.MinecraftServer;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import xyz.yoinky3000.server_info_api.ServerInfoAPI;
import xyz.yoinky3000.server_info_api.annotations.RouteSettings;
import xyz.yoinky3000.server_info_api.api.APIRequest;
import xyz.yoinky3000.server_info_api.api.APIResponse;
import xyz.yoinky3000.server_info_api.api.APIRoute;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@RouteSettings(path = "/status")
public class StatusRoute implements APIRoute {
    @Override
    public APIResponse handle(APIRequest request, MinecraftServer server) {
        Map<String, Object> stats;
        if (server == null) stats = ServerStats.collect();
        else stats = ServerStats.collect(server);
        String json = ServerInfoAPI.GSON.toJson(stats);

        return new APIResponse(APIResponse.HttpStatus.OK, APIResponse.MimeType.APPLICATION_JSON, json);
    }
}

class ServerStats {
    private static final SystemInfo systemInfo = new SystemInfo();
    private static final CentralProcessor processor = systemInfo.getHardware().getProcessor();
    private static final GlobalMemory memory = systemInfo.getHardware().getMemory();
    public static Map<String, Object> collect(MinecraftServer server) {
        Map<String, Object> data = collect();

        // Disk
        Path serverDir = server.getRunDirectory().toFile().toPath();
        long serverSizeBytes = getFolderSize(serverDir);
        long serverSizeMB = serverSizeBytes / (1024L * 1024L);
        @SuppressWarnings("unchecked")
        Map<String, Object> diskData = (Map<String, Object>) data.get("disk");
        diskData.put("usedMB", serverSizeMB);

        // PlayersRoute
        int online = server.getPlayerManager().getCurrentPlayerCount();
        int max = server.getPlayerManager().getMaxPlayerCount();
        data.put("players", Map.of("online", online, "max", max));

        return data;
    }
    public static Map<String, Object> collect() {
        Map<String, Object> data = new HashMap<>();

        // CPU
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        Map<String, Object> cpuMap = new HashMap<>();
        cpuMap.put("usagePercent", cpuLoad);
        data.put("cpu", cpuMap);

        // RAM
        long totalMemory = memory.getTotal() / (1024 * 1024);
        long availableMemory = memory.getAvailable() / (1024 * 1024);
        long usedMemory = totalMemory - availableMemory;
        Map<String, Object> ramMap = new HashMap<>();
        ramMap.put("usedMB", usedMemory);
        ramMap.put("totalMB", totalMemory);
        data.put("ram", ramMap);

        // Disk
        File root = new File(".");
        long totalDisk = root.getTotalSpace() / (1024L * 1024L * 1024L);
        long freeDisk = root.getFreeSpace() / (1024L * 1024L * 1024L);
        Map<String, Object> diskMap = new HashMap<>();
        diskMap.put("freeGB", freeDisk);
        diskMap.put("totalGB", totalDisk);
        diskMap.put("usedMB", "Unknown");
        data.put("disk", diskMap);

        // PlayersRoute
        data.put("players", "Unknown");

        return data;
    }

    public static long getFolderSize(Path folderPath) {
        try (Stream<Path> files = Files.walk(folderPath)) {
            return files
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
