package top.timeblog.multiproxysync.manage;
import com.velocitypowered.api.proxy.Player;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.params.SetParams;
import top.timeblog.multiproxysync.MultiProxySync;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class RedisManager {
    private static JedisPool pool;
    public static final String PLAYER_COUNT_CHANNEL = "multiproxysync:player-count:update";

    private Thread subscriberThread;
    private JedisPubSub subscriber;

    private String getPlayerListKey() {
        return MultiProxySync.ServerName + ":PlayerList";
    }

    public void updatePlayerList(Collection<Player> playerList){
        try(Jedis rs = get()) {
            if (!playerList.isEmpty() && playerList.size() != 0) {
                String tmp = getPlayerListKey() + ":tmp";
                rs.del(tmp);
                String[] players = playerList.stream()
                        .map(player -> player.getUniqueId().toString())
                        .toArray(String[]::new);
                rs.sadd(tmp, players);
                rs.rename(tmp, getPlayerListKey());
                rs.expire(getPlayerListKey(),30);
            } else {
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

    public long addPlayer(UUID uuid) {
        try (Jedis rs = get()){
            long status = rs.sadd(getPlayerListKey(), String.valueOf(uuid));
            rs.expire(getPlayerListKey(), 30);
            return status;
        }
    }

    public long remPlayer(UUID uuid) {
        try (Jedis rs = get()){
            long status = rs.srem(getPlayerListKey(), String.valueOf(uuid));
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
            return rs.smembers(server + ":PlayerList");
        }
    }

    public void setPlayerCount(int playerCount) {
        try (Jedis rs = get()){
            rs.set("playerCount", Integer.toString(playerCount));
            rs.expire("playerCount", 30);
        }
    }
    public void publishPlayerCountUpdate() {
        try (Jedis rs = get()) {
            rs.publish(PLAYER_COUNT_CHANNEL, MultiProxySync.ServerName);
        }
    }

    public void startPlayerCountSubscriber(Logger logger, Runnable callback) {
        subscriberThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (Jedis rs = get()) {
                    subscriber = new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            if (!PLAYER_COUNT_CHANNEL.equals(channel)) {
                                return;
                            }

                            if (MultiProxySync.ServerName.equals(message)) {
                                return;
                            }

                            try {
                                callback.run();
                            } catch (Exception e) {
                                logger.warn("Failed to handle Redis pub/sub callback.", e);
                            }
                        }
                    };

                    logger.info("Started Redis subscriber for channel: {}", PLAYER_COUNT_CHANNEL);
                    rs.subscribe(subscriber, PLAYER_COUNT_CHANNEL);

                } catch (Exception e) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    logger.warn("Redis subscriber disconnected, retrying in 2 seconds...", e);

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            logger.info("Redis subscriber stopped.");
        }, "MultiProxySync-RedisSubscriber");

        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    public void stopSubscriber() {
        try {
            if (subscriber != null) {
                subscriber.unsubscribe();
            }
        } catch (Exception ignored) {
        }

        try {
            if (subscriberThread != null) {
                subscriberThread.interrupt();
            }
        } catch (Exception ignored) {
        }
    }

    public Jedis get() {
        return pool.getResource();
    }
    public void close() {
        pool.close();
    }
}
