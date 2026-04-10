package top.timeblog.multiProxySync.listener;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import top.timeblog.multiProxySync.MultiProxySync;
import top.timeblog.multiProxySync.manage.Manage;


public class PlayerConnectProxyListener {
    private final MultiProxySync plugin;
    private final Manage core;

    public PlayerConnectProxyListener(MultiProxySync plugin, Manage core) {
        this.plugin = plugin;
        this.core = core;
    }

    @Subscribe
    public void onJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().info("{} joined the server.", player.getUsername());
        core.playerJoin(event.getPlayer().getUniqueId());

    }
}
