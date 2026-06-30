package mk.finki.chat.server;

import mk.finki.chat.common.exception.ProtocolException;
import mk.finki.chat.common.model.Message;
import mk.finki.chat.common.model.MessageType;
import mk.finki.chat.common.protocol.MessageCodec;
import mk.finki.chat.server.registry.ClientRegistry;
import mk.finki.chat.server.service.BroadcastService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Опслужува ЕДЕН конектиран клиент. Секоja инстанца работи на своja
 * посебна нишка (доделена преку ExecutorService во ChatServer) - со тоа
 * серверот истовремено разговара со многу клиенти без еден бавен клиент
 * да ги блокира сите останати.
 *
 * Имплементира Runnable (не "extends Thread") намерно - класата опишува
 * "задача што се извршува", не "е нишка". Со Runnable, инстанцата може
 * да се предаде на ExecutorService thread pool, не мора рачно да
 * управуваме со создавање/уништување нишки.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ClientRegistry registry;
    private final BroadcastService broadcastService;

    private BufferedReader in;
    private PrintWriter out;

    // volatile: други нишки (на пр. BroadcastService од нивни сопствени
    // handler-нишки) читаат го ова поле - мора веднаш да ja видат
    // најновата вредност штом е поставена.
    private volatile String username;

    public ClientHandler(Socket socket, ClientRegistry registry, BroadcastService broadcastService) {
        this.socket = socket;
        this.registry = registry;
        this.broadcastService = broadcastService;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            if (!handleLogin()) {
                return; // login не успеа - finally подолу сепак ja чисти конекцијата
            }

            String line;
            while ((line = in.readLine()) != null) {
                handleIncoming(line);
            }
        } catch (IOException e) {
            System.out.println("Конекцијата со " + username + " е прекината: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    /** Прва порака по конекција мора да биде LOGIN со посакувано username. */
    private boolean handleLogin() throws IOException {
        String line = in.readLine();
        if (line == null) {
            return false;
        }

        Message loginMessage = MessageCodec.decode(line);
        String requestedUsername = loginMessage.getSender();

        // Важно: го поставуваме username ПРЕД registry.register(), за други
        // нишки веднаш да го видат точно штом овој handler стане видлив
        // во регистарот (наместо привремено да видат null).
        this.username = requestedUsername;

        if (!registry.register(requestedUsername, this)) {
            send(new Message(MessageType.ERROR, "SERVER", requestedUsername,
                    "Корисничкото име е веќе зафатено."));
            this.username = null; // не е регистриран - disconnect() не треба да емитува LEAVE
            return false;
        }

        System.out.println(username + " се најави.");

        // Прати му ja на новиот клиент тековната листа на online корисници
        String onlineList = String.join(",", registry.getOnlineUsernames());
        send(new Message(MessageType.SYSTEM, "SERVER", username, onlineList));

        // Извести ги сите ОСТАНАТИ дека некој нов се приклучи
        broadcastService.broadcastExcept(
                new Message(MessageType.JOIN, username, null, username + " се приклучи."),
                username
        );
        return true;
    }

    /** Декодира и дистрибуира една примена порака според нejзиниот тип. */
    private void handleIncoming(String rawJson) {
        Message message;
        try {
            message = MessageCodec.decode(rawJson);
        } catch (ProtocolException e) {
            System.out.println("Невалидна порака од " + username + ": " + e.getMessage());
            return;
        }

        switch (message.getType()) {
            case CHAT -> broadcastService.broadcastExcept(message, username);
            case PRIVATE -> {
                boolean delivered = broadcastService.sendPrivate(message, message.getRecipient());
                if (!delivered) {
                    send(new Message(MessageType.ERROR, "SERVER", username,
                            "Корисникот '" + message.getRecipient() + "' не е online."));
                }
            }
            default -> System.out.println(
                    "Непознат/неочекуван тип порака од " + username + ": " + message.getType());
        }
    }

    /** Испраќа порака до ОВОЈ конкретен клиент преку неговиот socket. */
    public void send(Message message) {
        out.println(MessageCodec.encode(message));
    }

    public String getUsername() {
        return username;
    }

    private void disconnect() {
        if (username != null) {
            registry.unregister(username);
            broadcastService.broadcast(new Message(MessageType.LEAVE, username, null, username + " замина."));
            System.out.println(username + " се одjави.");
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
