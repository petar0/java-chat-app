package mk.finki.chat.client.network;

import mk.finki.chat.common.exception.ProtocolException;
import mk.finki.chat.common.model.Message;
import mk.finki.chat.common.model.MessageType;
import mk.finki.chat.common.protocol.ChatProtocol;
import mk.finki.chat.common.protocol.MessageCodec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ServerConnection {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    
    private Consumer<Message> onMessageReceived;
    private Consumer<String> onConnectionLost;

    public void setOnMessageReceived(Consumer<Message> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void setOnConnectionLost(Consumer<String> onConnectionLost) {
        this.onConnectionLost = onConnectionLost;
    }

    public boolean connect(String host, int port, String username) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            // Праќање LOGIN порака
            Message loginMsg = new Message(MessageType.LOGIN, username, null, "Логирање");
            send(loginMsg);

            // Очекуваме првиот одговор да е или SYSTEM (успешно) или ERROR (грешка)
            String response = in.readLine();
            if (response == null) {
                return false;
            }
            
            Message msg = MessageCodec.decode(response);
            if (msg.getType() == MessageType.ERROR) {
                if (onConnectionLost != null) {
                    onConnectionLost.accept(msg.getContent());
                }
                close();
                return false;
            } else if (msg.getType() == MessageType.SYSTEM) {
                // Првата порака System содржи CSV од online users
                if (onMessageReceived != null) {
                    onMessageReceived.accept(msg);
                }
                startListening();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (onConnectionLost != null) {
                onConnectionLost.accept("Неуспешно поврзување: " + e.getMessage());
            }
            close();
            return false;
        }
    }

    public void send(Message message) {
        if (out != null) {
            out.println(MessageCodec.encode(message));
        }
    }

    private void startListening() {
        listenerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    try {
                        Message message = MessageCodec.decode(line);
                        if (onMessageReceived != null) {
                            onMessageReceived.accept(message);
                        }
                    } catch (ProtocolException e) {
                        System.err.println("Невалидна порака: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                if (onConnectionLost != null) {
                    onConnectionLost.accept("Врската со серверот е прекината.");
                }
            } finally {
                close();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void disconnect() {
        close();
    }

    private void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }
}
