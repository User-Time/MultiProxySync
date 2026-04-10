# MultiProxySync

**MultiProxySync** 是一个专为 Velocity 代理服务器设计的高性能插件。它通过 Redis 实现了多个代理端（Proxy）之间的玩家总数与玩家列表的实时同步，完美解决了分布式架构下 `Ping` 列表人数显示不一致的问题。

---

## 🌟 核心特性

* **分布式人数同步**：无论玩家从哪个网关接入，所有网关显示的在线人数始终保持全局统一。
* **实时性与补偿**：结合“事件驱动”（进出即刻同步）与“定时心跳”（10秒补偿），确保数据的准确性。
* **自动故障恢复 (Self-Healing)**：利用 Redis TTL 机制，若某个代理端意外宕机，其数据将在 30 秒内自动剔除，告别“幽灵玩家”。
* **极轻量化**：基于 Redis 原生数据结构设计，在大规模并发下依然保持极低延迟。
* **无感知集成**：自动劫持 ProxyPingEvent，玩家在服务器列表看到的即是跨服总人数。

---

## 🛠️ 安装步骤

1. **准备环境**：确保你拥有一个可用的 Redis 数据库。
2. **下载与投放**：下载 `MultiProxySync.jar` 并放入所有 Velocity 节点的 `plugins` 文件夹。
3. **首次运行**：启动服务器以生成默认配置文件。
4. **配置参数**：编辑 `plugins/multiproxysync/config.yml`：
```yaml
plugin:
  serverName: Proxy-01  # 每个节点的名称必须唯一
  enabled: true
redis:
  host: 127.0.0.1
  port: 6379
  password: YourPassword

```


5. **重启生效**：重启所有 Velocity 实例即可完成同步。

---

## 📄 配置文件说明

| 配置项 | 说明 |
| --- | --- |
| `plugin.serverName` | 当前代理端的唯一标识符，用于区分不同节点。 |
| `plugin.enabled` | 是否启用插件功能。 |
| `redis.host` | Redis 服务器的 IP 地址。 |
| `redis.password` | Redis 认证密码（若无密码请留空）。 |

---

## 🔗 关于作者

* **Author**: Time
* **Website**: [www.time-blog.top](https://www.time-blog.top)
* **GitHub**: [MultiProxySync 项目主页](https://www.google.com/search?q=https://github.com/%E4%BD%A0%E7%9A%84%E7%94%A8%E6%88%B7%E5%90%8D/MultiProxySync)

### 📝 开源协议

本项目采用 [Apache-2.0 License] 开源
