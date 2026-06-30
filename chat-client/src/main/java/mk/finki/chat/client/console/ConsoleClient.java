package mk.finki.chat.client.console;

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
import java.util.Scanner;

public class ConsoleClient {

    private final String host;
    private final int port;
    private String username;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public ConsoleClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            
            Scanner scanner = new Scanner(System.in);
            System.out.print("Внеси корисничко име: ");
            username = scanner.nextLine().trim();

            // Send login message
            Message loginMessage = new Message(MessageType.LOGIN, username, null, "Логирање");
            out.println(MessageCodec.encode(loginMessage));

            // Start listening thread
            Thread listenThread = new Thread(this::listenForMessages);
            listenThread.setDaemon(true);
            listenThread.start();

            System.out.println("Поврзан си! Пишувај пораки (или 'exit' за излез).");
            
            // Main loop for sending messages
            while (true) {
                String text = scanner.nextLine();
                if ("exit".equalsIgnoreCase(text.trim())) {
                    break;
                }
                if (text.trim().isEmpty()) {
                    continue;
                }
                
                // Parse for private message e.g. "@username hello"
                Message msg;
                if (text.startsWith("@")) {
                    int spaceIndex = text.indexOf(' ');
                    if (spaceIndex != -1) {
                        String recipient = text.substring(1, spaceIndex);
                        String content = text.substring(spaceIndex + 1);
                        msg = new Message(MessageType.PRIVATE, username, recipient, content);
                    } else {
                        System.out.println("Неправилен формат. Користи: @корисник порака");
                        continue;
                    }
                } else {
                    msg = new Message(MessageType.CHAT, username, null, text);
                }
                
                out.println(MessageCodec.encode(msg));
            }

        } catch (IOException e) {
            System.err.println("Грешка при поврзување: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    private void listenForMessages() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                try {
                    Message message = MessageCodec.decode(line);
                    displayMessage(message);
                } catch (ProtocolException e) {
                    System.err.println("Примена невалидна порака: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Конекцијата со серверот е прекината.");
        }
    }

    private void displayMessage(Message message) {
        String time = message.getTimestamp().toLocalTime().toString().substring(0, 8); // HH:mm:ss
        switch (message.getType()) {
            case CHAT -> System.out.printf("[%s] %s: %s%n", time, message.getSender(), message.getContent());
            case PRIVATE -> System.out.printf("[%s] [ПРИВАТНО] %s: %s%n", time, message.getSender(), message.getContent());
            case JOIN, LEAVE, SYSTEM -> System.out.printf("[%s] * %s *%n", time, message.getContent());
            case ERROR -> System.err.printf("[%s] ГРЕШКА: %s%n", time, message.getContent());
            default -> {}
        }
    }

    private void closeConnections() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConsoleClient client = new ConsoleClient(ChatProtocol.DEFAULT_HOST, ChatProtocol.DEFAULT_PORT);
        client.start();
    }
}
