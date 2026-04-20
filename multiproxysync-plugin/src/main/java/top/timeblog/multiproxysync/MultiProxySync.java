package top.timeblog.multiproxysync;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import top.timeblog.multiproxysync.api.MultiProxySyncAPI;
import top.timeblog.multiproxysync.api.MultiProxySyncProvider;
import top.timeblog.multiproxysync.config.ConfigManager;
import top.timeblog.multiproxysync.impl.MultiProxySyncAPIImpl;
import top.timeblog.multiproxysync.listener.PingListener;
import top.timeblog.multiproxysync.listener.PlayerConnectProxyListener;
import top.timeblog.multiproxysync.listener.PlayerDisconnectProxyListener;
import top.timeblog.multiproxysync.manage.Manage;
import top.timeblog.multiproxysync.manage.RedisManager;
import top.timeblog.multiproxysync.placeholder.PlaceholderRegistry;

import java.nio.file.Path;

@Plugin(
        id = "multiproxysync",
        name = "MultiProxySync",
        version = "2.0.0",
        url = "https://www.time-blog.top",
        authors = {"Time"},
        dependencies = {
                @Dependency(id = "miniplaceholders", optional = true)
        }
)
public class MultiProxySync {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final RedisManager redis;
    private final MultiProxySyncAPI api;

    public static int playerCount = 0;
    public static String ServerName;

    private Manage core;
    private volatile boolean ready = false;
    private ConfigManager config;

    @Inject
    public MultiProxySync(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        this.redis = new RedisManager();
        this.api = new MultiProxySyncAPIImpl(redis);
    }

    public static int getPlayerCount() {
        return playerCount;
    }

    public static void setPlayerCount(int count) {
        playerCount = count;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Manage getCore() {
        return core;
    }

    public MultiProxySyncAPI getApi() {
        return api;
    }

    public boolean isReady() {
        return ready;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Starting MultiProxySync initialization...");

        config = new ConfigManager(dataDirectory, "config.yml");
        config.load();

        String host = config.getString("redis", "host");
        int port = config.getInt("redis", "port");
        String password = config.getString("redis", "password");
        ServerName = config.getString("plugin", "serverName");
        String pluginStatus = config.getString("plugin", "enabled");

        if (!pluginStatus.equalsIgnoreCase("true")) {
            logger.warn("The plugin is not running. Please modify the configuration in config.yml and then restart the server.");
            return;
        }

        redis.connect(host, port, password);
        redis.init();

        MultiProxySyncProvider.register(this.api);
        core = new Manage(this, redis);
        ready = true;

        logger.info("MultiProxySync API initialized.");

        if (server.getPluginManager().isLoaded("miniplaceholders")) {
            try {
                new PlaceholderRegistry(this).register();
                logger.info("MiniPlaceholders detected, placeholder support enabled.");
            } catch (NoClassDefFoundError e) {
                logger.warn("MiniPlaceholders seems present but API classes were not available, skipping placeholder registration.");
            }
        } else {
            logger.info("MiniPlaceholders not found, placeholder support disabled.");
        }

        server.getEventManager().register(this, new PlayerConnectProxyListener(this, core));
        server.getEventManager().register(this, new PlayerDisconnectProxyListener(this, core));
        server.getEventManager().register(this, new PingListener(this));

        logger.info("Plugin started successfully!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (!ready) {
            return;
        }

        MultiProxySyncProvider.unregister();

        try (redis.clients.jedis.Jedis rs = redis.get()) {
            rs.srem("serverList", ServerName);
            rs.del(ServerName + ":PlayerList");
        } catch (Exception e) {
            logger.warn("Redis cleanup failed.", e);
        }

        redis.close();
    }
}