package top.timeblog.multiproxysync;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import top.timeblog.multiproxysync.api.MultiProxySyncAPI;
import top.timeblog.multiproxysync.impl.MultiProxySyncAPIImpl;
import top.timeblog.multiproxysync.config.ConfigManager;
import top.timeblog.multiproxysync.listener.PingListener;
import top.timeblog.multiproxysync.listener.PlayerConnectProxyListener;
import top.timeblog.multiproxysync.listener.PlayerDisconnectProxyListener;
import top.timeblog.multiproxysync.manage.Manage;
import top.timeblog.multiproxysync.manage.RedisManager;
import top.timeblog.multiproxysync.api.MultiProxySyncProvider;

import java.nio.file.Path;

@Plugin(
        id = "multiproxysync",
        name = "MultiProxySync",
        version = "2.0.0",
        url = "https://www.time-blog.top",
        authors = {"Time"}
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

    ConfigManager config;

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

    public static void setPlayerCount(int Count) {
        playerCount = Count;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
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
        String PluginStatus = config.getString("plugin", "enabled");

        if (PluginStatus.equalsIgnoreCase("true")) {
            redis.connect(host, port, password);
            redis.init();
            MultiProxySyncProvider.register(this.api);
            ready = true;
            logger.info("MultiProxySync API initialized.");

            core = new Manage(this, redis);

            server.getEventManager().register(this, new PlayerConnectProxyListener(this, core));
            server.getEventManager().register(this, new PlayerDisconnectProxyListener(this, core));
            server.getEventManager().register(this, new PingListener(this));

            logger.info("Plugin started successfully!");
        } else {
            logger.warn("The plugin is not running. Please modify the configuration in config.yml and then restart the server.");
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        MultiProxySyncProvider.unregister();
        try (redis.clients.jedis.Jedis rs = redis.get()) {
            rs.srem("serverList", ServerName);
            rs.del(ServerName + ":PlayerList");
        } catch (Exception e) {
            logger.warn("Redis cleanup failed.");
        }

        redis.close();
    }

    public MultiProxySyncAPI getApi() {
        return api;
    }

    public boolean isReady() {
        return ready;
    }
}