---

# MultiProxySync

**[English](https://github.com/User-Time/MultiProxySync) | 

---

## MultiProxySync

**MultiProxySync** is a high-performance Velocity plugin designed for distributed proxy networks. It synchronizes online player counts and player lists across multiple Velocity proxies using Redis, ensuring consistent data representation for your network.

###🌟 Key Features

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

## 🔗 About the Author

* **Author**: Time
* **Website**: [www.time-blog.top](https://www.time-blog.top)
* **GitHub**: [MultiProxySync Repository](https://www.google.com/search?q=https://github.com/%E4%BD%A0%E7%9A%84%E7%94%A8%E6%88%B7%E5%90%8D/MultiProxySync)


---
