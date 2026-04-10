# MultiProxySync

[**English**](https://github.com/User-Time/MultiProxySync) | [**Chinese**](https://github.com/User-Time/MultiProxySync/blob/master/Readme_zhCN.md)

---

**MultiProxySync** is a high-performance Velocity plugin designed for distributed proxy networks. It synchronizes online player counts and player lists across multiple Velocity proxies using Redis, ensuring consistent data representation for your network.

### 🌟 Key Features

* **Global Sync**: Unified player count across all proxy nodes.
* **Self-Healing**: Automatic cleanup of data from crashed nodes using Redis TTL.
* **High Performance**: Optimized with Redis Sets (O(1) complexity).
* **Zero-Config Ping**: Automatically hooks into `ProxyPingEvent` to show accurate total counts.
* **Public API**: Exposes synchronized player data for use by other plugins.

---

## 🛠️ Installation

1. Ensure you have a **Redis** server running.
2. Place `MultiProxySync.jar` into the `plugins` folder of all your Velocity proxies.
3. Start your proxies to generate the `config.yml`.
4. Configure your Redis credentials in `plugins/multiproxysync/config.yml`.
5. Restart all instances.

---

## 📄 Configuration Example

```yaml
plugin:
  serverName: Proxy-01  # Unique identifier for each node
  enabled: true
redis:
  host: 127.0.0.1
  port: 6379
  password: YourPassword
```

---

## 🔌 API

MultiProxySync also provides a public API for other Velocity plugins.

This allows external plugins to access synchronized player data across all connected proxies.

### Available Methods

```java
Set<String> getProxies();
Set<String> getAllPlayers();
Map<String, Set<String>> getPlayersByProxy();
int getAllPlayerCount();
Map<String, Integer> getPlayerCountByProxy();
```
### Method Description

* `getProxies()`  
  Returns all currently known proxy names.

* `getAllPlayers()`  
  Returns a set of player UUID strings across every proxy.

* `getAllPlayerCount()`  
  Returns the total synchronized online player count across the network.

* `getPlayersByProxy()`  
  Returns online player UUID strings grouped by proxy.

* `getPlayerCountByProxy()`  
  Returns the online player count grouped by proxy.

### Usage Example

```java
import top.timeblog.multiProxySync.MultiProxySync;
import top.timeblog.multiProxySync.api.MultiProxySyncAPI;

MultiProxySync plugin = (MultiProxySync) proxyServer.getPluginManager()
        .getPlugin("multiproxysync")
        .flatMap(container -> container.getInstance().map(instance -> (MultiProxySync) instance))
        .orElse(null);

if (plugin != null && plugin.isReady()) {
    MultiProxySyncAPI api = plugin.getApi();

    int totalPlayers = api.getAllPlayerCount();
    Set<String> allPlayers = api.getAllPlayers();
    Map<String, Integer> countByProxy = api.getPlayerCountByProxy();
    Map<String, Set<String>> playersByProxy = api.getPlayersByProxy();

    System.out.println("Total players: " + totalPlayers);
    System.out.println("All players: " + allPlayers);
    System.out.println("Count by proxy: " + countByProxy);
    System.out.println("Players by proxy: " + playersByProxy);
}
```

### Notes

* The API is read-only.
* Redis connection management remains internal to MultiProxySync.
* Returned player identifiers are UUID strings.
* The API becomes available after the plugin has finished initialization.

---
