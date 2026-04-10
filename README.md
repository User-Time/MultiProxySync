# MultiProxySync

[**English**](https://github.com/User-Time/MultiProxySync) | [**Chinese**](https://github.com/User-Time/MultiProxySync/blob/master/Readme_zhCN.md)


---

**MultiProxySync** is a high-performance Velocity plugin designed for distributed proxy networks. It synchronizes online player counts and player lists across multiple Velocity proxies using Redis, ensuring consistent data representation for your network.

### 🌟 Key Features

* **Global Sync**: Unified player count across all proxy nodes.
* **Self-Healing**: Automatic cleanup of data from crashed nodes using Redis TTL.
* **High Performance**: Optimized with Redis Sets (O(1) complexity).
* **Zero-Config Ping**: Automatically hooks into `ProxyPingEvent` to show accurate total counts.

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
