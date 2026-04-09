package top.timeblog.multiProxySync;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;
//import top.timeblog.multiProxySync.api.MultiProxySyncAPI;
import top.timeblog.multiProxySync.config.ConfigManager;
import top.timeblog.multiProxySync.listener.PingListener;
import top.timeblog.multiProxySync.listener.PlayerConnectProxyListener;
import top.timeblog.multiProxySync.listener.PlayerDisconnectProxyListener;
//import top.timeblog.multiProxySync.manage.MultiProxySyncAPIImpl;
import top.timeblog.multiProxySync.manage.RedisManager;
import top.timeblog.multiProxySync.manage.Manage;

import java.nio.file.Path;

@Plugin(id = "multiproxysync", name = "MultiProxySync", version = "1.0.0", url = "https://www.time-blog.top", authors = {"Time"})
public class MultiProxySync {
    private final ProxyServer server;
    private final Logger logger;
    public static int playerCount = 0;
    private Manage core;
    private final Path dataDirectory;
//    private MultiProxySyncAPI api;



    @Inject
    public MultiProxySync(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    ConfigManager config;
    RedisManager redis = new RedisManager();
    public Manage getManage() {
        return core;
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
    public static String ServerName;
//    @Subscribe
//    public void onInit(ProxyInitializeEvent event) {
//        this.api = new MultiProxySyncAPIImpl(redis);
//    }


    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        config = new ConfigManager(dataDirectory, "config.yml");
        config.load();
        String host = config.getString("redis","host");
        int port = config.getInt("redis","port");
        String password = config.getString("redis","password");
        ServerName = config.getString("plugin","serverName");
        String PluginStatus = config.getString("plugin","enabled");
        if (PluginStatus.equalsIgnoreCase("true")){
            redis.connect(host, port, password);
            redis.init();
            core = new Manage(this, redis);
            server.getEventManager().register(this,new PlayerConnectProxyListener(this,core));
            server.getEventManager().register(this,new PlayerDisconnectProxyListener(this,core));
            server.getEventManager().register(this,new PingListener(this));
            logger.info("Plugin started successfully!");
        }else {
            logger.warn("The plugin is not running. Please modify the configuration in config.yml and then restart the server.");
        }
    }
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        try (redis.clients.jedis.Jedis rs = redis.get()) {
            rs.srem("serverList", ServerName);
        } catch (Exception e) {
            logger.warn("Redis cleanup failed.");
        }
        redis.close();
    }

}
