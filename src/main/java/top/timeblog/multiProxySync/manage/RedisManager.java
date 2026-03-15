package top.timeblog.multiProxySync.manage;
import com.velocitypowered.api.proxy.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;
import top.timeblog.multiProxySync.MultiProxySync;

import java.util.Collection;
import java.util.Set;

public class RedisManager {
    private static JedisPool pool;
    private String getPlayerListKey() {
        return MultiProxySync.ServerName + ":PlayerList";
    }

    public void updatePlayerList(Collection<Player> playerList){
        try(Jedis rs = get()) {
            if (!playerList.isEmpty() && playerList.size() != 0) {
                String tmp = getPlayerListKey() + ":tmp";
                rs.del(tmp);
                String[] players = playerList.stream()
                        .map(Player::getUsername)
                        .toArray(String[]::new);
                rs.sadd(tmp, players);
                rs.rename(tmp, getPlayerListKey());
                rs.expire(getPlayerListKey(),30);
            }else {
                rs.del(getPlayerListKey());
            }
        }
    }

    public void connect(String host, int port, String password) {
        pool = new JedisPool(
                new JedisPoolConfig(),
                host,
                port,
                2000,
                password
        );
        System.out.println("Run connect fun!");
    }

    public void init() {
        try (Jedis rs = get()){
            rs.sadd("serverList", MultiProxySync.ServerName);
            rs.set("playerCount", "0", SetParams.setParams().nx());
        }
    }
    public long addPlayer(Player player) {
        try (Jedis rs = get()){
            long status = rs.sadd(getPlayerListKey(), player.getUsername());
            rs.expire(getPlayerListKey(), 30);
            return status;
        }
    }
    public long remPlayer(Player player) {
        try (Jedis rs = get()){
            long status = rs.srem(getPlayerListKey(), player.getUsername());
            rs.expire(getPlayerListKey(), 30);
            return status;
        }
    }
    public Set<String> getAllServers() {
        try (Jedis rs = get()){
            return rs.smembers("serverList");
        }
    }
    public Set<String> getServerPlayers(String server) {
        try (Jedis rs = get()){
            return rs.smembers(server+":PlayerList");
        }
    }

    public void setPlayerCount(int playerCount) {
        try (Jedis rs = get()){
            rs.set("playerCount", Integer.toString(playerCount));
            rs.expire("playerCount", 30);
        }
    }

    public Jedis get() {
        return pool.getResource();
    }
    public void close() {
        pool.close();
    }
}
