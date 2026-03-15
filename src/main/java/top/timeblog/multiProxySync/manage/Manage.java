package top.timeblog.multiProxySync.manage;
import com.velocitypowered.api.proxy.Player;
import top.timeblog.multiProxySync.MultiProxySync;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static top.timeblog.multiProxySync.MultiProxySync.playerCount;

public class Manage {
    private final MultiProxySync plugin;
    private final RedisManager redis;

    public Manage(MultiProxySync server, RedisManager redis) {
        this.plugin = server;
        this.redis = redis;
        // 启动定时更新
        startHeartbeat();
    }
    public void updatePlayerList(){
        redis.updatePlayerList(getAllPlayer());
    }
    private void startHeartbeat() {
        plugin.getServer().getScheduler()
                .buildTask(plugin, () -> {
                    try {
                        updatePlayerList();
                        // 每次更新服务器心跳，TTL = 30秒
                        getAllServerPlayer();
                        playerCount = getAllServerPlayerCount();
                        redis.setPlayerCount(playerCount);

                    } catch (Exception e) {
                        plugin.getLogger().warn("Failed to update server", e);
                    }
                })
                .repeat(10, java.util.concurrent.TimeUnit.SECONDS) // 每10秒执行一次
                .schedule();
    }

    public Collection<Player> getAllPlayer(){
        return plugin.getServer().getAllPlayers();
    }

    public Object[] getAllServerPlayer(){
        Set<String> Servers = redis.getAllServers();
        Set<String> AllPlayers = new HashSet<>();
        Servers.forEach(serverName->{
            AllPlayers.addAll(redis.getServerPlayers(serverName));
        });
        return AllPlayers.toArray();
    }

    public int getAllServerPlayerCount(){
        return getAllServerPlayer().length;
    }

    public void playerJoin(Player player) {
        try {
            long status = redis.addPlayer(player);
            if (status == 1) {
                plugin.getLogger().debug("Player count +1");

                playerCount += 1;
                playerCount = getAllServerPlayerCount();
                redis.setPlayerCount(playerCount);
                MultiProxySync.setPlayerCount(playerCount);
            }else {
                plugin.getLogger().warn("Player count increase failed");
                plugin.getLogger().warn("Perhaps the server crashed before? This might have caused Redis's TTL to not have expired yet?");
                plugin.getLogger().warn("If it's not a Redis configuration issue, please submit an issue.");
            }
        } catch (Exception e) {
            plugin.getLogger().error("PlayerJoin Server Redis Exception: ", e);
        }
    }
    public void playerLeave(Player player) {
        try {
            long status = redis.remPlayer(player);
            if (status == 1) {
                plugin.getLogger().debug("Player count -1");

                playerCount -= 1;
                playerCount = getAllServerPlayerCount();
                System.out.println("SetCount: " + playerCount);
                redis.setPlayerCount(playerCount);
                MultiProxySync.setPlayerCount(playerCount);
            }else {
                plugin.getLogger().warn("Player count reduction failure");
                plugin.getLogger().warn("Perhaps it's because the server experienced lag times exceeding 20 seconds?");
                plugin.getLogger().warn("If it's not a Redis configuration issue, please submit an issue.");
            }
        } catch (Exception e) {
            plugin.getLogger().error("PlayerLeave Server Redis Exception: ", e);
        }
    }
}
