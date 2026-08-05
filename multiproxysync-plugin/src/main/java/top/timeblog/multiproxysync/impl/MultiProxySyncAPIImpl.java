package top.timeblog.multiproxysync.impl;

import top.timeblog.multiproxysync.api.MultiProxySyncAPI;
import top.timeblog.multiproxysync.manage.RedisManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static top.timeblog.multiproxysync.MultiProxySync.getPlayerCount;

public class MultiProxySyncAPIImpl implements MultiProxySyncAPI {

    private final RedisManager redis;

    public MultiProxySyncAPIImpl(RedisManager redis) {
        this.redis = redis;
    }

    @Override
    public Set<String> getProxies() {
        return redis.getAllServers();
    }

    @Override
    public Set<String> getAllPlayers() {
        Set<String> servers = redis.getAllServers();
        Set<String> allPlayers = new HashSet<>();

        servers.forEach(serverName -> {
            allPlayers.addAll(redis.getServerPlayers(serverName));
        });

        return allPlayers;
    }

    @Override
    public int getAllPlayerCount() {
        return getPlayerCount();
    }

    @Override
    public Map<String, Integer> getPlayerCountByProxy() {
        Set<String> Servers = redis.getAllServers();
        Map<String, Integer> playerCountByServer = new HashMap<>();

        Servers.forEach(serverName -> {
            playerCountByServer.put(serverName, redis.getServerPlayers(serverName).size());
        });

        return playerCountByServer;
    }

    @Override
    public Map<String, Set<String>> getPlayersByProxy() {
        Set<String> Servers = redis.getAllServers();
        Map<String, Set<String>> playersByServer = new HashMap<>();

        Servers.forEach(serverName -> {
            playersByServer.put(serverName, new HashSet<>(redis.getServerPlayers(serverName)));
        });

        return playersByServer;
    }
}