# MultiProxySync

[**English**](https://github.com/User-Time/MultiProxySync) | [**中文**](https://github.com/User-Time/MultiProxySync/blob/master/Readme_zhCN.md)

---

**MultiProxySync** 是一个专为分布式 Velocity 代理网络设计的高性能插件。通过 Redis 在多个 Velocity 代理之间同步在线玩家人数与玩家列表，从而确保整个网络中的数据表现始终一致。

### 🌟 核心特性

* **全局同步**：在所有代理节点之间统一在线人数显示。
* **自愈能力**：通过 Redis TTL 自动清理异常宕机节点遗留的数据。
* **高性能**：基于 Redis Set 实现，拥有优秀的性能表现。
* **零配置 Ping 同步**：自动接管 `ProxyPingEvent`，准确显示全网总在线人数。
* **公共 API**：向其他插件开放同步后的玩家数据。

---

## 🛠️ 安装

1. 确保你已经部署并运行了一个 **Redis** 服务器。
2. 将 `MultiProxySync.jar` 放入所有 Velocity 代理的 `plugins` 文件夹中。
3. 启动代理服务器以生成 `config.yml`。
4. 在 `plugins/multiproxysync/config.yml` 中填写 Redis 连接信息。
5. 重启所有代理实例。

---

## 📄 配置示例

```yaml
plugin:
  serverName: Proxy-01  # 每个节点都必须使用唯一名称
  enabled: true
redis:
  host: 127.0.0.1
  port: 6379
  password: YourPassword
```

---

## 📘 配置项说明

| 配置项 | 说明 |
| --- | --- |
| `plugin.serverName` | 当前代理节点的唯一标识，用于区分不同代理。 |
| `plugin.enabled` | 是否启用插件。 |
| `redis.host` | Redis 服务器地址。 |
| `redis.port` | Redis 服务器端口。 |
| `redis.password` | Redis 认证密码。 |

---

## 🔌 API

MultiProxySync 也为其他 Velocity 插件提供了公共 API。

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

* `getAllPlayerCount()`  
  返回整个代理网络中同步后的总在线人数。

* `getPlayersByProxy()`  
  按代理分组返回在线玩家的 UUID 字符串。

* `getPlayerCountByProxy()`  
  按代理返回对应的在线人数统计。

### 使用示例

```java
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

### 说明

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
