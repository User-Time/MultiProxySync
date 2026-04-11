# MultiProxySync

[**English**](https://github.com/User-Time/MultiProxySync) | [**中文**](https://github.com/User-Time/MultiProxySync/blob/master/Readme_zhCN.md)

---

**MultiProxySync** 是一个专为分布式 Velocity 代理网络设计的高性能插件。它通过 Redis 在多个 Velocity 代理之间同步在线玩家人数与玩家列表，从而确保整个网络中的数据表现始终一致。

### 🌟 核心特性

* **全局同步**：在所有代理节点之间统一在线人数与玩家列表。
* **自愈能力**：通过 Redis TTL 自动清理异常宕机节点遗留的数据。
* **高性能**：基于 Redis Set 进行同步，具有优秀的性能表现。
* **零配置 Ping 同步**：自动接管 `ProxyPingEvent`，准确显示全网总在线人数。
* **公共 API**：向其他插件开放同步后的玩家与代理数据。
* **Maven 支持**：API 可通过 Maven Central 正常引入，无需手动安装本地 JAR。

---

## 🛠️ 安装

1. 确保你已经部署并运行了一个 **Redis** 服务器。
2. 将 `multiproxysync-plugin-2.0.0.jar` 放入所有 Velocity 代理的 `plugins` 文件夹中。
3. 启动代理服务器一次以生成 `config.yml`。
4. 在 `plugins/multiproxysync/config.yml` 中填写 Redis 连接信息。
5. 重启所有代理实例。

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

* `serverName` 必须在每个代理节点中保持唯一。
* `enabled` 用于控制插件是否初始化并注册 API。
* 所有代理节点都应连接到同一个 Redis 实例。

---

## 📦 Maven 依赖

公共 API 已发布到 Maven Central，可通过以下方式引入：

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

MultiProxySync 为其他 Velocity 插件提供了公共 API。

通过该 API，外部插件可以访问所有代理之间已同步的玩家数据。

### 可用方法

```java
Set<String> getProxies();
Set<String> getAllPlayers();
Map<String, Set<String>> getPlayersByProxy();
int getAllPlayerCount();
Map<String, Integer> getPlayerCountByProxy();
```

### 方法说明

* `getProxies()`  
  返回当前系统中已跟踪的所有代理名称。

* `getAllPlayers()`  
  返回全网所有在线玩家的 UUID 字符串集合。

* `getPlayersByProxy()`  
  按代理分组返回在线玩家的 UUID 字符串。

* `getAllPlayerCount()`  
  返回整个代理网络中同步后的总在线人数。

* `getPlayerCountByProxy()`  
  按代理返回对应的在线人数统计。

---

## 🧩 使用示例

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

### API 可用性

使用 `MultiProxySyncProvider.getOrNull()` 获取 API。

如果返回值不为 `null`，则说明 API 已可用并可直接调用。

---

## 📝 说明

* 该 API 为只读接口。
* Redis 连接管理仍由 MultiProxySync 内部负责。
* 返回的玩家标识为 UUID 字符串，而不是玩家名。
* API 会在插件完成初始化后可用。

---

## 🔗 关于作者

* **Author**: Time
* **Website**: [www.time-blog.top](https://www.time-blog.top)
* **GitHub**: [MultiProxySync 项目主页](https://github.com/User-Time/MultiProxySync)

### 📝 开源协议

本项目基于 **Apache-2.0 License** 开源。
