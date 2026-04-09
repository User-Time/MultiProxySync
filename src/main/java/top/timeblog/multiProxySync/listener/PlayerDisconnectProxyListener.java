package top.timeblog.multiProxySync.listener;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import top.timeblog.multiProxySync.MultiProxySync;
import top.timeblog.multiProxySync.manage.Manage;


public class PlayerDisconnectProxyListener {
    private final MultiProxySync plugin;
    private final Manage core;

    public PlayerDisconnectProxyListener(MultiProxySync plugin, Manage core) {
        this.plugin = plugin;
        this.core = core;
    }

    @Subscribe
    public void onLeave(DisconnectEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().info("{} left the server.", player.getUsername());
        core.playerLeave(event.getPlayer().getUniqueId());
    }
}
