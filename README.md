# MultiProxySync

![GitHub release](https://img.shields.io/github/v/release/User-Time/MultiProxySync?logo=github)
![Maven Central](https://img.shields.io/maven-central/v/net.time-cloud/multiproxysync-api?logo=maven-central)
![License](https://img.shields.io/github/license/User-Time/MultiProxySync?logo=license)
![Velocity](https://img.shields.io/badge/Velocity-3.X+-blue?logo=Velocity)
![Redis](https://img.shields.io/badge/Redis-required-red?logo=redis)
[![Modrinth](https://img.shields.io/badge/Modrinth-MultiProxySync-00AF5C?style=flat-square\&logo=modrinth)](https://modrinth.com/plugin/multiproxysync)
[![MineBBS](https://img.shields.io/badge/MineBBS-MultiProxySync-8ab1ec?style=flat-square\&logo=minebbs)](https://www.minebbs.com/resources/multiproxysync-velocity.15712/)

[**English**](https://github.com/User-Time/MultiProxySync) | [**中文**](https://github.com/User-Time/MultiProxySync/blob/master/Readme_zhCN.md)

<p align="center">
  <img src="https://raw.githubusercontent.com/User-Time/MultiProxySync/refs/heads/master/assets/banner2.png" alt="MultiProxySync Banner"/>
</p>

---

MultiProxySync is a Velocity plugin for distributed proxy networks.

It uses Redis to synchronize player counts and player lists across multiple Velocity proxies, allowing all entry points in a network to share a consistent global online count and player state.

MultiProxySync combines periodic synchronization with Redis Pub/Sub updates and automatic proxy health tracking to keep synchronized data accurate even when proxy nodes restart, disconnect, or crash unexpectedly.

---

## ✨ Features

* **Global player synchronization** — Synchronizes player counts and player lists across Velocity proxies.
* **Redis Pub/Sub** — Quickly propagates player count changes between proxy nodes.
* **Proxy health tracking** — Uses Redis ZSET heartbeats and Redis server time to track active proxies and automatically remove stale nodes.
* **Public API** — Provides read-only access to synchronized proxy and player data.
* **MiniPlaceholders** — Optional global player count placeholder support.
* **bStats** — Includes anonymous usage metrics and proxy network size statistics.
* **Maven Central** — The API is published directly to Maven Central.

### Proxy Heartbeat

Each proxy registers itself when starting and refreshes its heartbeat every **10 seconds**.

Proxy nodes that have not updated for more than **30 seconds** are treated as offline and automatically removed from the active proxy list.

Heartbeat timestamps use **Redis server time**, avoiding inconsistencies caused by different proxy system clocks.

---

## 📦 Requirements

* Velocity 3.x+
* Redis
* MiniPlaceholders *(optional)*

---

## 🛠️ Installation

1. Prepare a Redis server.
2. Download the latest `multiproxysync-plugin` release.
3. Place it in the `plugins` directory of every Velocity proxy.
4. Start the proxies and edit the generated `config.yml`.
5. Make sure every proxy connects to the same Redis instance.

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
* All proxies must use the same Redis instance.

---

## 🔤 MiniPlaceholders

When MiniPlaceholders is installed, MultiProxySync registers:

```text
<multiproxysync_global_player_count>
```

Example:

```text
Global online: <multiproxysync_global_player_count>
```

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

```java
Set<String> getProxies();
Set<String> getAllPlayers();
Map<String, Set<String>> getPlayersByProxy();
int getAllPlayerCount();
Map<String, Integer> getPlayerCountByProxy();
```

`getProxies()` and proxy-based statistics only include currently active proxy nodes.

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

* The API is read-only.
* Player identifiers are UUID strings.
* Redis connections are managed internally.
* The API becomes available after MultiProxySync finishes initialization.

</details>

---

## ⚠️ API Migration

Starting with **2.3.0**, the Maven Group ID and Java package have changed.

```text
top.time-blog  →  net.time-cloud
top.timeblog   →  net.timecloud
```

The API method signatures remain unchanged.

Plugins using the MultiProxySync API must update their dependency coordinates and Java imports.

---

## 🗺️ Version Roadmap

### 2.4.x

**2.4.x will be the final release line focused exclusively on player synchronization.**

After the 2.4.x feature set is finalized, this branch will enter maintenance mode:

* Bug fixes will continue.
* No new features will be added.

### 3.0.0+

Starting with **3.0.0**, MultiProxySync will expand beyond player synchronization and introduce additional cross-proxy synchronization capabilities.

---

## 💡 Feedback & Support

Issues and suggestions are welcome:

https://github.com/User-Time/MultiProxySync/issues

---

## 📝 License

Licensed under the **Apache License 2.0**.
