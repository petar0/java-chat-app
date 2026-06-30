package mk.finki.chat.server.service;

import mk.finki.chat.common.model.Message;
import mk.finki.chat.server.ClientHandler;
import mk.finki.chat.server.registry.ClientRegistry;

/**
 * Одговорен само за ЕДНО нешто (Single Responsibility Principle) -
 * да испрати порака до точните примачи. ClientHandler не треба да
 * знае КАКО да најде сите клиенти или КАКО да провери дали некој е
 * online - само ja предава пораката понатаму на овој сервис.
 */
public class BroadcastService {

    private final ClientRegistry registry;

    public BroadcastService(ClientRegistry registry) {
        this.registry = registry;
    }

    /** Испраќа порака до сите конектирани клиенти. */
    public void broadcast(Message message) {
        for (ClientHandler client : registry.getAllClients()) {
            client.send(message);
        }
    }

    /** Испраќа порака до сите ОСВЕН подателот (на пр. обични CHAT пораки). */
    public void broadcastExcept(Message message, String excludedUsername) {
        for (ClientHandler client : registry.getAllClients()) {
            if (!client.getUsername().equals(excludedUsername)) {
                client.send(message);
            }
        }
    }

    /**
     * Испраќа приватна порака до конкретен корисник.
     *
     * @return false ако примачот не е online (пораката не е испорачана)
     */
    public boolean sendPrivate(Message message, String recipientUsername) {
        ClientHandler recipient = registry.get(recipientUsername);
        if (recipient == null) {
            return false;
        }
        recipient.send(message);
        return true;
    }
}
