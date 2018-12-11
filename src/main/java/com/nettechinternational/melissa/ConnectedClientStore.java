package com.nettechinternational.melissa;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class ConnectedClientStore {

    private static ConnectedClientStore CONNECTED_CLIENTS;
    private final Map<String, Client> CLIENTS;

    public static ConnectedClientStore get() {
        if (CONNECTED_CLIENTS == null) {
            CONNECTED_CLIENTS = new ConnectedClientStore();
        }

        return CONNECTED_CLIENTS;
    }

    private ConnectedClientStore() {
        CLIENTS = new ConcurrentHashMap();
    }

    public void put(Client client) {
        CLIENTS.put(client.getSocketId(), client);
    }

    public void remove(Client client) {
        CLIENTS.remove(client.getSocketId());
    }

    public List<Client> filterClients(Predicate<Client> pr) {
        return CLIENTS.values()
                .parallelStream()
                .filter(pr)
                .collect(Collectors.toList());

    }

}
