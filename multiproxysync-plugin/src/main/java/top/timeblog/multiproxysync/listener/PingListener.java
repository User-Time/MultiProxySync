package top.timeblog.multiproxysync.listener;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import top.timeblog.multiproxysync.MultiProxySync;


public class PingListener {
    private final MultiProxySync plugin;
    public PingListener(MultiProxySync plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        event.setPing(
                event.getPing()
                        .asBuilder()
                        .onlinePlayers(MultiProxySync.getPlayerCount())
                        .build()
        );
        plugin.getLogger().debug("PingReturn!");
    }
}
