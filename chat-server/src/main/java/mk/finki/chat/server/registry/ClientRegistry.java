package mk.finki.chat.server.registry;

import mk.finki.chat.server.ClientHandler;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Централен, thread-safe регистар на сите тековно конектирани клиенти
 * (username -> неговиот ClientHandler).
 *
 * Повеќе нишки (по една по клиент) истовремено читаат и пишуваат во
 * оваа структура - затоа MORA да биде ConcurrentHashMap, не обичен
 * HashMap (обичен HashMap може тивко да ги расипе податоците или да
 * фрли ConcurrentModificationException под паралелен пристап).
 */
public class ClientRegistry {

    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    /**
     * Регистрира нов клиент под дадено username.
     *
     * @return false ако username е веќе зафатен (логинот треба да се одбие)
     */
    public boolean register(String username, ClientHandler handler) {
        return clients.putIfAbsent(username, handler) == null;
    }

    public void unregister(String username) {
        clients.remove(username);
    }

    public ClientHandler get(String username) {
        return clients.get(username);
    }

    public Collection<ClientHandler> getAllClients() {
        return clients.values();
    }

    public Collection<String> getOnlineUsernames() {
        return clients.keySet();
    }
}
