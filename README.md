# MultiProxySync

![GitHub release](https://img.shields.io/github/v/release/User-Time/MultiProxySync?logo=github)
![Maven Central](https://img.shields.io/maven-central/v/net.time-cloud/multiproxysync-api?logo=maven-central)
![License](https://img.shields.io/github/license/User-Time/MultiProxySync?logo=license)
![Velocity](https://img.shields.io/badge/Velocity-3.0+-blue?logo=Velocity)
![Redis](https://img.shields.io/badge/Redis-required-red?logo=redis)
[![Modrinth](https://img.shields.io/badge/Modrinth-MultiProxySync-00AF5C?style=flat-square\&logo=modrinth)](https://modrinth.com/plugin/multiproxysync)
[![MineBBS](https://img.shields.io/badge/MineBBS-MultiProxySync-8ab1ec?style=flat-square\&logo=minebbs)](https://www.minebbs.com/resources/multiproxysync-velocity.15712/)

[**English**](https://github.com/User-Time/MultiProxySync) | [**中文**](https://github.com/User-Time/MultiProxySync/blob/master/Readme_zhCN.md)

<p align="center">
  <img src="https://raw.githubusercontent.com/User-Time/MultiProxySync/refs/heads/master/assets/banner2.png" alt="MultiProxySync Banner"/>
</p>

---

MultiProxySync is a Velocity plugin for distributed proxy networks.

It uses **Redis** to synchronize player counts and player lists across multiple Velocity proxies, allowing different proxy entry points to share a consistent global online count and player state.

MultiProxySync combines periodic synchronization, Redis Pub/Sub updates, and proxy health tracking to keep synchronized data accurate when proxies restart, disconnect, or crash unexpectedly.

---

## ✨ Features

* **Multi-proxy player synchronization** — Synchronizes player counts and player lists across multiple Velocity proxies.
* **Redis Pub/Sub** — Quickly notifies other proxies when players join, leave, or proxy state changes.
* **Proxy health tracking** — Uses Redis ZSET heartbeats to track active proxies and automatically remove stale nodes.
* **Consistent online count** — Handles `ProxyPingEvent` so different proxy entry points display the same global online count.
* **Public API** — Provides read-only access to synchronized proxy and player data.
* **MiniPlaceholders** — Optional placeholder support for the global online player count.
* **bStats** — Provides anonymous usage metrics and proxy network size statistics.

### Proxy Heartbeat

Each proxy registers itself when it starts.

Proxies that have not updated their heartbeat for more than **30 seconds** are considered offline and automatically removed from the active proxy list.

Heartbeat timestamps use **Redis server time**, preventing differences between proxy system clocks from affecting proxy status detection.

---

## 📦 Requirements

* Velocity 3.x+
* Redis
* MiniPlaceholders *(optional)*

---

## 🛠️ Installation

1. Set up a Redis server.
2. Download the latest `multiproxysync-plugin`.
3. Place the plugin in the `plugins` directory of every Velocity proxy.
4. Start the proxies and edit the generated `config.yml`.
5. Make sure all MultiProxySync instances connect to the same Redis server.

---

## 📄 Configuration

```yaml
plugin:
  serverName: Proxy-01
  enabled: true

redis:
  host: 127.0.0.1
  port: 6379
  password: YourPassword
```

* `serverName` must be unique for every proxy.
* `enabled` controls whether MultiProxySync is initialized.
* All proxies must connect to the same Redis instance.

---

## 🔤 MiniPlaceholders

When MiniPlaceholders is installed, MultiProxySync automatically registers:

```text
<multiproxysync_global_player_count>
```

Example:

```text
Global online: <multiproxysync_global_player_count>
```

The placeholder returns the synchronized global online player count.

---

## 📦 API

<details>
<summary>Click to expand</summary>

### Maven

```xml
<dependency>
    <groupId>net.time-cloud</groupId>
    <artifactId>multiproxysync-api</artifactId>
    <version>2.3.0</version>
    <scope>provided</scope>
</dependency>
```

### Gradle

```kotlin
dependencies {
    compileOnly("net.time-cloud:multiproxysync-api:2.3.0")
}
```

### Available Methods

* `getProxies()`
  Returns all currently active Velocity proxy nodes.

* `getAllPlayers()`
  Returns the UUIDs of all online players across the network.

* `getPlayersByProxy()`
  Returns each proxy and its corresponding set of online player UUIDs.

* `getAllPlayerCount()`
  Returns the total number of online players across the network.

* `getPlayerCountByProxy()`
  Returns each proxy and its corresponding online player count.

> Proxy-related data only includes nodes with a valid heartbeat. Player identifiers are UUID strings.

### Example

```java
import net.timecloud.multiproxysync.api.MultiProxySyncAPI;
import net.timecloud.multiproxysync.api.MultiProxySyncProvider;

MultiProxySyncAPI api = MultiProxySyncProvider.getOrNull();
if (api == null) {
    return;
}

int totalPlayers = api.getAllPlayerCount();
Set<String> players = api.getAllPlayers();
Map<String, Integer> countByProxy = api.getPlayerCountByProxy();
```

### Notes

* Player identifiers are UUID strings.

</details>

---

## ⚠️ API Migration

Starting with **2.3.0**, the Maven Group ID and Java package have changed:

```text
top.time-blog  →  net.time-cloud
top.timeblog   →  net.timecloud
```

---

## 🗺️ Version Roadmap

MultiProxySync will continue to maintain the current version line while developing future versions for different use cases. You can choose the version that best fits your needs and preferences.

### 2.3.0+ — Core Feature Maintenance

The **2.3.0+** line will remain focused on synchronizing player counts and player lists.

No unnecessary features will be added. Development will mainly focus on maintenance, bug fixes, and stability improvements.

Minor updates may still introduce improvements or fixes that are directly related to the core synchronization functionality when necessary.

### 3.0.0+ — Feature Expansion

Starting with **3.0.0**, MultiProxySync will expand beyond its existing player synchronization functionality and introduce additional features.

You can continue using **2.3.0+** if you only need the core synchronization features, or use **3.0.0+** and later versions if you want the expanded functionality.

---

## 💡 Feedback & Support

If you encounter any issues or have suggestions, feel free to open an Issue:

https://github.com/User-Time/MultiProxySync/issues

---

## 📝 License

This project is licensed under the **Apache License 2.0**.
