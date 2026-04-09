package top.timeblog.multiProxySync.manage;
import com.velocitypowered.api.proxy.Player;
import top.timeblog.multiProxySync.MultiProxySync;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
                        getAllServerPlayers();
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

    public Set<String> getAllServerPlayers(){
        Set<String> servers = redis.getAllServers();
        Set<String> allPlayers = new HashSet<>();
        servers.forEach(serverName->allPlayers.addAll(redis.getServerPlayers(serverName)));
        return allPlayers;
    }

    public int getAllServerPlayerCount(){
        return getAllServerPlayers().size();
    }

    public void playerJoin(UUID uuid) {
        try {
            long status = redis.addPlayer(uuid);
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
    public void playerLeave(UUID uuid) {
        try {
            long status = redis.remPlayer(uuid);
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
