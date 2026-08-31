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

**MultiProxySync** 是一个面向分布式 Velocity 代理网络的插件。

通过 **Redis** 在多个 Velocity 代理之间同步玩家人数与玩家列表，使不同代理服务器入口能够共享一致的全局在线人数和玩家状态。

MultiProxySync 结合周期同步、Redis Pub/Sub 实时更新和代理节点健康检测，在代理重启、断开或异常崩溃时仍能保持同步数据准确。

---

## ✨ 特性

* **多代理服玩家同步** — 在多个 Velocity 代理之间同步玩家人数与玩家列表。
* **Redis Pub/Sub** — 玩家加入、离开或代理状态变化时快速通知其他节点刷新数据。
* **代理服状态检测** — 基于 Redis ZSET 心跳记录有效代理，并自动清理失效节点。
* **统一在线人数** — 接管 `ProxyPingEvent`，在不同服务器入口显示一致的全网在线人数。
* **公共 API** — 为其他插件提供只读的代理与玩家同步数据。
* **MiniPlaceholders** — 可选的全局在线人数占位符支持。
* **bStats** — 提供匿名使用统计及代理网络规模统计。

### 代理心跳

服务器启动后进行注册，

超过 **30 秒**未更新的代理服会被视为离线，并从在线服务器列表中自动清理。

心跳时间戳使用 **Redis 服务器时间**，避免不同代理服务器系统时钟存在偏差时影响节点状态判断。

---

## 📦 运行要求

* Velocity 3.x+
* Redis
* MiniPlaceholders *(可选)*

---

## 🛠️ 安装

1. Redis
2. 下载最新版 `multiproxysync-plugin`。
3. 将插件放入所有 Velocity 代理服务器的 `plugins` 目录。
4. 启动代理并编辑生成的 `config.yml`。
5. 确保所有插件连接到同一个 Redis

---

## 📄 配置

```yaml
plugin:
  serverName: Proxy-01
  enabled: true

redis:
  host: 127.0.0.1
  port: 6379
  password: YourPassword
```

* `serverName` 必须在每个代理节点中保持唯一。
* `enabled` 用于控制 MultiProxySync 是否初始化。
* 所有代理节点必须连接到同一个 Redis 实例。

---

## 🔤 MiniPlaceholders

安装 MiniPlaceholders 后，MultiProxySync 会自动注册：

```text
<multiproxysync_global_player_count>
```

示例：

```text
全服在线：<multiproxysync_global_player_count>
```

该占位符返回当前同步的全网在线人数。

---

## 📦 API

<details>
<summary>点击展开</summary>

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

### 可用方法

* `getProxies()`
  获取当前所有有效的 Velocity 代理节点。

* `getAllPlayers()`
  获取全网在线玩家 UUID 集合。

* `getPlayersByProxy()`
  获取各代理节点及其对应的在线玩家 UUID 集合。

* `getAllPlayerCount()`
  获取全网在线玩家总数。

* `getPlayerCountByProxy()`
  获取各代理服务器节点及其对应的在线玩家数量。

> 代理相关数据仅包含当前心跳有效的节点，玩家标识均为 UUID 字符串。

### 使用示例

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

### API 说明
* 玩家标识使用 UUID 字符串。

</details>

---

## ⚠️ API 迁移说明

从 **2.3.0** 开始，Maven Group ID 与 Java 包名已调整：

```text
top.time-blog  →  net.time-cloud
top.timeblog   →  net.timecloud
```

---

🗺️ 版本规划

MultiProxySync 后续将围绕不同的使用需求，分别维护现有版本并推进新版本开发。你可以根据自己的实际需求和偏好，自由选择适合自己的版本。

2.3.0+：基础功能维护

2.3.0+ 将专注于玩家人数和玩家列表的同步功能，后续不再增加非必要功能，仅围绕现有功能进行维护、Bug 修复和稳定性改进。

如有必要，也可能通过小版本更新增加与基础同步功能直接相关的改进或修复。

3.0.0+：功能拓展

从 3.0.0 开始，MultiProxySync 将在现有玩家同步功能的基础上进行后续功能拓展与开发。

你可以根据自己的使用需求和心情，自由选择继续使用 2.3.0+，或尝试 3.0.0+ 及后续版本。

---

## 💡 反馈与支持

如果在使用过程中遇到问题或有新的建议，欢迎提交 Issue：

https://github.com/User-Time/MultiProxySync/issues

---

## 📝 开源协议

本项目基于 **Apache License 2.0** 开源。
