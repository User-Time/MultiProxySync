package net.timecloud.multiproxysync;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;
import net.timecloud.multiproxysync.api.MultiProxySyncAPI;
import net.timecloud.multiproxysync.api.MultiProxySyncProvider;
import net.timecloud.multiproxysync.config.ConfigManager;
import net.timecloud.multiproxysync.impl.MultiProxySyncAPIImpl;
import net.timecloud.multiproxysync.listener.PingListener;
import net.timecloud.multiproxysync.listener.PlayerConnectProxyListener;
import net.timecloud.multiproxysync.listener.PlayerDisconnectProxyListener;
import net.timecloud.multiproxysync.manage.Manage;
import net.timecloud.multiproxysync.manage.RedisManager;
import net.timecloud.multiproxysync.placeholder.PlaceholderRegistry;
import net.timecloud.multiproxysync.update.UpdateChecker;

import java.nio.file.Path;

@Plugin(
        id = "multiproxysync",
        name = "MultiProxySync",
        version = "2.3.0",
        url = "https://github.com/User-Time/MultiProxySync",
        authors = {"Time"},
        dependencies = {
                @Dependency(id = "miniplaceholders", optional = true)
        }
)
public class MultiProxySync {
    private static final int BSTATS_PLUGIN_ID = 33753;
    private final Metrics.Factory metricsFactory;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final RedisManager redis;
    private final MultiProxySyncAPI api;

    public static volatile int playerCount = 0;
    public static String ServerName;

    private Manage core;
    private volatile boolean ready = false;
    private ConfigManager config;

    @Inject
    public MultiProxySync(ProxyServer server,
                          Logger logger,
                          @DataDirectory Path dataDirectory,
                          Metrics.Factory metricsFactory
    ) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.redis = new RedisManager();
        this.api = new MultiProxySyncAPIImpl(redis);
        this.metricsFactory = metricsFactory;
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
        Metrics metrics = metricsFactory.make(this, BSTATS_PLUGIN_ID);;

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
        redis.startPlayerCountSubscriber(logger, () -> {
            try {
                core.refreshLocalPlayerCount();
            } catch (Exception e) {
                logger.warn("Failed to sync player count from pub/sub", e);
            }
        });
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
        startUpdateCheck();

        metrics.addCustomChart(new SimplePie(
                "proxyNetworkSize",
                () -> {
                    try {
                        long count = redis.getActiveProxyCount();

                        if (count <= 0) return null;
                        if (count == 1) return "1";
                        if (count == 2) return "2";
                        if (count == 3) return "3";
                        if (count <= 5) return "4-5";
                        if (count <= 10) return "6-10";
                        return "11+";
                    } catch (Exception e) {
                        return null;
                    }
                }
        ));
    }

    private void startUpdateCheck() {
        String currentVersion = server.getPluginManager()
                .fromInstance(this)
                .flatMap(plugin -> plugin.getDescription().getVersion())
                .orElse("unknown");

        server.getScheduler()
                .buildTask(this, () -> UpdateChecker.check(logger, currentVersion))
                .schedule();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (!ready) {
            return;
        }

        MultiProxySyncProvider.unregister();

        try {
            // 主动关闭心跳, 防止关闭后的一瞬间发送导致延迟30s
            core.stopHeartbeat();
            redis.removeProxy();
            try (redis.clients.jedis.Jedis rs = redis.get()) {
                rs.del(ServerName + ":PlayerList");
            }
        } catch (Exception e) {
            logger.warn("Redis cleanup failed.", e);
        }

        try {
            redis.publishPlayerCountUpdate();
        } catch (Exception e) {
            logger.warn("Failed to publish player count update on shutdown.", e);
        }

        redis.stopSubscriber();
        redis.close();
    }
}
