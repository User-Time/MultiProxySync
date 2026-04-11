# MultiProxySync

[**English**](https://github.com/User-Time/MultiProxySync) | [**Chinese**](https://github.com/User-Time/MultiProxySync/blob/master/Readme_zhCN.md)

---

**MultiProxySync** is a high-performance Velocity plugin designed for distributed proxy networks. It synchronizes online player counts and player lists across multiple Velocity proxies using Redis, ensuring consistent data representation across your entire network.

### 🌟 Key Features

* **Global Sync**: Unified player count and synchronized player lists across all proxy nodes.
* **Self-Healing**: Automatic cleanup of stale data from crashed nodes using Redis TTL.
* **High Performance**: Optimized around Redis Sets for efficient synchronized access.
* **Zero-Config Ping**: Automatically hooks into `ProxyPingEvent` to display accurate global player counts.
* **Public API**: Exposes synchronized player and proxy data for use by other plugins.
* **Maven Ready**: The API can be added through Maven Central without manually installing local JAR files.

---

## 🛠️ Installation

1. Ensure you have a **Redis** server running.
2. Place `multiproxysync-plugin-2.0.0.jar` into the `plugins` folder of all your Velocity proxies.
3. Start your proxies once to generate the `config.yml`.
4. Configure your Redis credentials in `plugins/multiproxysync/config.yml`.
5. Restart all proxy instances.

---

## 📄 Configuration Example

```yaml
plugin:
  serverName: Proxy-01
  enabled: true

redis:
  host: 127.0.0.1
  port: 6379
  password: YourPassword
```

### Configuration Notes

* `serverName` must be unique for each proxy node.
* `enabled` controls whether the plugin initializes and registers its API.
* Redis credentials must point to the same Redis instance for all proxies in the network.

---

## 📦 Maven Dependency

The public API is available on Maven Central and can be added like this:

```xml
<dependency>
    <groupId>top.time-blog</groupId>
    <artifactId>multiproxysync-api</artifactId>
    <version>2.0.0</version>
    <scope>provided</scope>
</dependency>
```

---

## 🔌 API

MultiProxySync provides a public API for other Velocity plugins.

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

* `getPlayersByProxy()`  
  Returns player UUID strings grouped by proxy.

* `getAllPlayerCount()`  
  Returns the total synchronized online player count across the network.

* `getPlayerCountByProxy()`  
  Returns the online player count grouped by proxy.

---

## 🧩 Usage Example

```java
import top.timeblog.multiproxysync.api.MultiProxySyncAPI;
import top.timeblog.multiproxysync.api.MultiProxySyncProvider;

MultiProxySyncAPI api = MultiProxySyncProvider.getOrNull();
if (api == null) {
    System.out.println("MultiProxySync API is not available yet.");
    return;
}

int totalPlayers = api.getAllPlayerCount();
Set<String> allPlayers = api.getAllPlayers();
Map<String, Integer> countByProxy = api.getPlayerCountByProxy();
Map<String, Set<String>> playersByProxy = api.getPlayersByProxy();

System.out.println("Total players: " + totalPlayers);
System.out.println("All players: " + allPlayers);
System.out.println("Count by proxy: " + countByProxy);
System.out.println("Players by proxy: " + playersByProxy);
```

### API Availability

Use `MultiProxySyncProvider.getOrNull()` to obtain the API.

If the returned value is not `null`, the API is available and ready to use.

---

## 📝 Notes

* The API is read-only.
* Redis connection management remains internal to MultiProxySync.
* Returned player identifiers are UUID strings.
* The API becomes available after the plugin has completed initialization.

---

## 💡 Feedback & Support

If you encounter any issues or have ideas for improvements, feel free to open an issue:

👉 https://github.com/User-Time/MultiProxySync/issues


### 📝 License

This project is licensed under the **Apache License 2.0**.
