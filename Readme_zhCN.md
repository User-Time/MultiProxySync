# MultiProxySync

![GitHub release](https://img.shields.io/github/v/release/User-Time/MultiProxySync?logo=github)
![Maven Central](https://img.shields.io/maven-central/v/top.time-blog/multiproxysync-api?logo=maven-central)
![License](https://img.shields.io/github/license/User-Time/MultiProxySync?logo=license)
![Velocity](https://img.shields.io/badge/Velocity-3.X-blue?logo=Velocity)
![Redis](https://img.shields.io/badge/Redis-required-red?logo=redis)
[![Modrinth](https://img.shields.io/badge/Modrinth-MultiProxySync-00AF5C?style=flat-square&logo=modrinth)](https://modrinth.com/plugin/multiproxysync)
[![Modrinth](https://img.shields.io/badge/MineBBS-MultiProxySync-8ab1ec?style=flat-square&logo=minebbs)](https://www.minebbs.com/resources/multiproxysync-velocity.15712/)

[**English**](https://github.com/User-Time/MultiProxySync) | [**中文**](https://github.com/User-Time/MultiProxySync/blob/master/Readme_zhCN.md)

<p align="center">
  <img src="https://raw.githubusercontent.com/User-Time/MultiProxySync/refs/heads/master/assets/banner2.png" alt="MultiProxySync Banner"/>
</p>

---

**MultiProxySync** 是一个面向分布式 Velocity 代理网络的插件。  
它通过 **Redis** 在多个 Velocity 代理之间同步玩家人数与玩家列表，让整个网络在所有入口点上都能显示一致的全局在线信息。

从 **2.2.0** 开始，插件在原有 Redis 数据同步基础上加入了 **Pub/Sub 实时更新机制**，当玩家进入、离开，或代理下线时，其他代理能够更快刷新本地在线人数缓存。

---

## ✨ 特性

- **全局同步**  
  在所有 Velocity 代理节点之间同步玩家人数与玩家列表。

- **Pub/Sub 实时刷新**  
  当玩家加入、离开或代理关闭时，通过 Redis Pub/Sub 通知其他代理快速刷新本地人数缓存。

- **更准确的在线人数**  
  自动接管 `ProxyPingEvent`，让服务器列表中显示的在线人数更接近全网真实总人数。

- **自愈清理**  
  通过 Redis TTL 自动清理宕机或异常掉线代理遗留的过期数据。

- **Redis 驱动**  
  基于 Redis Set 与 Pub/Sub 进行轻量同步，结构简单，性能稳定。

- **公共 API**  
  为其他插件提供只读 API，可获取全局代理与玩家同步数据。

- **MiniPlaceholders 支持**  
  检测到 `MiniPlaceholders` 后会自动注册占位符，可用于显示全局在线人数。

- **Maven Central**  
  公共 API 可直接通过 Maven Central 引入，无需手动安装本地 JAR。

---

## 📦 运行要求

- **Velocity** 代理服务器
- **Redis** 数据库

---

## 🛠️ 安装

1. 确保你已经部署并运行了一个 **Redis** 服务器。
2. 下载 `multiproxysync-plugin-2.2.0.jar`。
3. 将其放入所有 Velocity 代理实例的 `plugins` 文件夹中。
4. 首次启动每个代理以生成配置文件。
5. 编辑生成的 `config.yml`。
6. 重启所有代理实例。

### 可选依赖

- **MiniPlaceholders**  
  若服务器安装了 MiniPlaceholders，MultiProxySync 会自动启用占位符支持。

---

## 📄 配置示例

```yaml
plugin:
  serverName: Proxy-01
  enabled: true

redis:
  host: 127.0.0.1
  port: 6379
  password: YourPassword
```

### 配置说明

- `serverName` 必须在每个代理节点中保持唯一。
- `enabled` 用于控制插件是否初始化并注册 API。
- 所有代理节点都应连接到同一个 Redis 实例。

---

## 🔤 占位符

如果安装了 **MiniPlaceholders**，插件会自动注册以下占位符：

```text
<multiproxysync_global_player_count>
```

### 说明

- 该占位符返回当前代理缓存的全网在线人数。
- 人数会通过 Redis Pub/Sub 与心跳同步机制自动刷新。
- 若未安装 MiniPlaceholders，则不会注册占位符支持。

---

## 📦 API

<details>
<summary>点击展开</summary>

### Maven

```xml
<dependency>
    <groupId>top.time-blog</groupId>
    <artifactId>multiproxysync-api</artifactId>
    <version>2.2.0</version>
    <scope>provided</scope>
</dependency>
```

### Gradle

```kotlin
dependencies {
    compileOnly("top.time-blog:multiproxysync-api:2.2.0")
}
```

### 可用方法

```java
Set<String> getProxies();
Set<String> getAllPlayers();
Map<String, Set<String>> getPlayersByProxy();
int getAllPlayerCount();
Map<String, Integer> getPlayerCountByProxy();
```

### 使用示例

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

### API 说明

- API 为只读接口。
- Redis 连接与同步逻辑由 MultiProxySync 内部负责。
- 返回的玩家标识为 UUID 字符串，而不是玩家名称。
- API 会在插件初始化完成后可用。

</details>

---

## 💡 反馈与支持

如果你在使用过程中遇到问题，或有新的建议，欢迎提交 Issue：

👉 https://github.com/User-Time/MultiProxySync/issues

---

## 📝 开源协议

本项目基于 **Apache License 2.0** 开源。
