package mk.finki.chat.server;

import mk.finki.chat.common.protocol.ChatProtocol;
import mk.finki.chat.server.registry.ClientRegistry;
import mk.finki.chat.server.service.BroadcastService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Влезна точка на серверот. Слуша за нови TCP конекции на ServerSocket
 * и за секоja прифатена конекција доделува посебна нишка преку
 * ExecutorService (thread pool) - НЕ рачно "new Thread()" по клиент,
 * бидејќи pool-от повторно ги користи нишките наместо постојано да
 * создава/уништува нови (поефикасно, и е "production" пракса).
 */
public class ChatServer {

    private final int port;
    private final ClientRegistry registry = new ClientRegistry();
    private final BroadcastService broadcastService = new BroadcastService(registry);
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat серверот слуша на порта " + port + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); // блокира додека не дојде нов клиент
                System.out.println("Нова конекција од " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, registry, broadcastService);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Грешка во серверот: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(ChatProtocol.DEFAULT_PORT);
        server.start();
    }
}
