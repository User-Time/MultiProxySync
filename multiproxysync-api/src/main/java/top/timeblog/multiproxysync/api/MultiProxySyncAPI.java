package top.timeblog.multiproxysync.api;

import java.util.Map;
import java.util.Set;

public interface MultiProxySyncAPI {
    /**
     * Retrieves the names of all proxies currently tracked by the system.
     *
     * @return a set of proxy names
     */
    Set<String> getProxies();

    /**
     * Retrieves all online player UUID strings across all proxies.
     *
     * @return a set of player UUID strings
     */
    Set<String> getAllPlayers();

    /**
     * Retrieves online player UUID strings grouped by proxy.
     *
     * @return a mapping of proxy name to the set of player UUID strings connected to that proxy
     */
    Map<String, Set<String>> getPlayersByProxy();

    /**
     * Retrieves the total number of online players across all proxies.
     *
     * @return total online player count
     */
    int getAllPlayerCount();

    /**
     * Retrieves the number of online players for each proxy.
     *
     * @return a mapping of proxy name to its corresponding online player count
     */
    Map<String, Integer> getPlayerCountByProxy();
}
