# 🚀 Server Info API Mod

**Access and Extend Server Information via HTTP API**

This mod is designed to empower Minecraft server owners and mod developers by providing a robust and easily extendable HTTP API. Get real-time server statistics and seamlessly integrate your server's and mod's data into external applications or web interfaces.

---

## ✨ Why You Need This Mod

Are you looking to:
* 📊 **Monitor your server** via HTTP request?
* 🎮 Display **live player counts** on your website?
* 🤖 Create **custom Discord bots** that interact with your server data?
* 🔌 Allow **other mods to effortlessly expose their data** through a unified API?

The Server Info API mod makes all of this a lot easier! It sets up a dedicated API endpoint for your Minecraft server, offering both essential predefined routes and a flexible system for developers to add their own.

---

## ⚙️ Quick Setup

By default, the API runs on port `8080`. You can easily change this port to suit your needs by modifying the `apiPort` setting in `config/Server Info API/settings.json`.

---

## 🌐 API Endpoints - What You Can Access

This mod comes with powerful predefined routes to get you started immediately.

### 📊 `/status`

Get a comprehensive overview of your server's vital statistics, including disk usage, CPU load, RAM consumption, and current player counts.

**Example Response:**
```json
{
  "disk": {
    "totalGB": 1606,
    "freeGB": 343,
    "usedMB": 25
  },
  "players": {
    "online": 0,
    "max": 20
  },
  "cpu": {
    "usagePercent": 9.865470852017937
  },
  "ram": {
    "totalMB": 63033,
    "usedMB": 31469
  }
}
```

### 👥 `/players`

Show the total number of players who have ever joined your server and see a list of all currently online players.

**Example Response:**
```json
{
  "totalPlayers": 1,
  "onlinePlayers": []
}
```

### 👤 `/player/:criteria`

Retrieve detailed information about a specific player by using their in-game name or UUID as the `:criteria`.

**Example Response:**
```json
{
  "name": "PLAYER_NAME",
  "online": true,
  "uuid": "PLAYER_UUID",
  "info": {
    "currentChunkPos": {
      "z": 1,
      "x": 0
    },
    "currentPos": {
      "z": 23.423937096484163,
      "x": 7.907748838070694,
      "y": 100.12983446114534
    },
    "dimension": "minecraft:the_nether"
  }
}
```

---

## 🚀 For Developers

This mod isn't just for server owners; it's built with extensibility in mind! Any mod developer can easily register custom API routes to expose their mod's unique data or functionalities.

### 1. Create Your Route Class

Define your API endpoint by creating a class that implements the `APIRoute` interface. You can use the `@RouteSettings` annotation to configure its path and behavior.

```java
import xyz.yoinky3000.server_info_api.annotations.RouteSettings;
import xyz.yoinky3000.server_info_api.api.APIRequest;
import xyz.yoinky3000.server_info_api.api.APIResponse;
import xyz.yoinky3000.server_info_api.api.APIRoute;
import net.minecraft.server.MinecraftServer; // Import for the server parameter

// --- Option 1: Basic Route (no server required) ---
// This route will respond with "test" and does not require the MinecraftServer to be fully loaded.
@RouteSettings(path = "/my-simple-test-route") // requireMinecraftServer is false by default
public class MySimpleTestRoute implements APIRoute {
    @Override
    public APIResponse handle(APIRequest apiRequest) {
        // You don't have access to the MinecraftServer object here
        return new APIResponse(APIResponse.HttpStatus.OK, APIResponse.MimeType.TEXT_PLAIN, "Hello from my simple route!");
    }
}

// --- Option 2: Route Requiring MinecraftServer ---
// This route will only serve requests once the MinecraftServer is ready.
// If a request comes in before the server is ready, an HTTP 500 (INTERNAL_SERVER_ERROR)
// will be automatically sent.
@RouteSettings(path = "/my-server-data-route", requireMinecraftServer = true)
public class MyServerDataRoute implements APIRoute {
    @Override
    public APIResponse handle(APIRequest request, MinecraftServer server) {
        // Here, 'server' is guaranteed not to be null if requireMinecraftServer is true
        int onlinePlayers = server.getCurrentPlayerCount();
        return new APIResponse(APIResponse.HttpStatus.OK, APIResponse.MimeType.TEXT_PLAIN,
            "Currently online players: " + onlinePlayers);
    }
}
```
**`@RouteSettings` explained:**
* `path`: The API endpoint (e.g., `/my-custom-data`).
* `requireMinecraftServer`: If `true`, the route will only serve requests when the Minecraft server is fully initialized. If a request arrives before the server is ready, an HTTP 500 (Internal Server Error) response is sent automatically. This is `false` by default.

### 2. Register Your Route Class

Once you've created your `APIRoute` class, register it with the `ServerInfoAPI`. You can do this at any time, even outside of `onInitialize` if your mod's logic requires it. The route becomes active as soon as it's registered!

```java
import xyz.yoinky3000.server_info_api.ServerInfoAPI;
import net.fabricmc.api.ModInitializer; // Assuming Fabric ModInitializer

public class CustomMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // ... your other initialization code ...

        // Registering a route using the annotation (recommended for clarity)
        ServerInfoAPI.registerRoute(new MyServerDataRoute()); // The annotation handles path and server requirement

        // Alternatively, if you prefer not to use annotations, or for dynamic paths:
        // You can explicitly define path, server requirement, and override behavior.
        // ServerInfoAPI.registerRoute("/my-dynamic-route", new MySimpleTestRoute());
        // ServerInfoAPI.registerRoute("/admin-data", new MyServerDataRoute(), true, true); // Path, Route, requiresServer, override

        System.out.println("Custom API routes registered!");
    }
}
```
**`registerRoute` Overloads for Flexibility:**
The `ServerInfoAPI` offers various `registerRoute` methods to give you full control:
* `public static APIHandler.RegisterResult registerRoute(APIRoute route)`: Registers a route configured by its `@RouteSettings` annotation. `override` is `false` by default.
* `public static APIHandler.RegisterResult registerRoute(APIRoute route, boolean override)`: Registers an annotated route, allowing you to explicitly `override` existing routes at the same path.
* `public static APIHandler.RegisterResult registerRoute(String path, APIRoute route)`: Registers a route with a specified `path`. `requireMinecraftServer` is `false` and `override` is `false` by default.
* `public static APIHandler.RegisterResult registerRoute(String path, APIRoute route, boolean override)`: Registers a route with a specified `path`, allowing you to `override` existing routes.
* `public static APIHandler.RegisterResult registerRoute(String path, APIRoute route, boolean requireMinecraftServer, boolean override)`: Provides full control over `path`, `APIRoute` instance, `requireMinecraftServer` status, and `override` behavior.

### 3. Dynamic Route Patterns

You can define dynamic segments in your route paths using `:SOMETHING_HERE`. This is perfect for routes like `/player/:criteria` where `criteria` is a placeholder for a player name or UUID.

To access these dynamic parameters within your `handle` method:

```java
import xyz.yoinky3000.server_info_api.api.APIRequest;
import xyz.yoinky3000.server_info_api.api.APIResponse;
import xyz.yoinky3000.server_info_api.api.APIRoute;
import net.minecraft.server.MinecraftServer;

@RouteSettings(path = "/my-data/:item_id")
public class ItemDataRoute implements APIRoute {
    @Override
    public APIResponse handle(APIRequest request, MinecraftServer server) {
        // Retrieve the value of the 'item_id' segment from the URL
        String itemId = request.routeParams().get("item_id"); //

        if (itemId != null) {
            // Logic to fetch data for this item ID
            return new APIResponse(APIResponse.HttpStatus.OK, APIResponse.MimeType.TEXT_PLAIN, "Requested item ID: " + itemId);
        } else {
            return new APIResponse(APIResponse.HttpStatus.BAD_REQUEST, APIResponse.MimeType.TEXT_PLAIN, "Item ID not provided.");
        }
    }
}
```

---

## 🤝 Contribution & Support

Feel free to open issues for bug reports or feature requests on the GitHub repository. Contributions are always welcome!