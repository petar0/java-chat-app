package mk.finki.chat.client.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import mk.finki.chat.client.network.ServerConnection;
import mk.finki.chat.common.model.Message;
import mk.finki.chat.common.model.MessageType;
import mk.finki.chat.common.protocol.ChatProtocol;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ChatController {

    @FXML private VBox loginBox;
    @FXML private TextField usernameField;
    @FXML private Button loginButton;
    @FXML private Label loginErrorLabel;

    @FXML private VBox chatBox;
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private ListView<String> usersList;
    @FXML private Button sendButton;

    private ServerConnection connection;
    private String currentUsername;
    private ObservableList<String> onlineUsers = FXCollections.observableArrayList();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        chatBox.setVisible(false);
        chatBox.setManaged(false);
        usersList.setItems(onlineUsers);
        
        // Add double-click to user list for private messages
        usersList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = usersList.getSelectionModel().getSelectedItem();
                if (selected != null && !selected.equals(currentUsername)) {
                    messageField.setText("@" + selected + " ");
                    messageField.requestFocus();
                    messageField.positionCaret(messageField.getText().length());
                }
            }
        });
    }

    @FXML
    private void onLogin() {
        String user = usernameField.getText().trim();
        if (user.isEmpty()) {
            loginErrorLabel.setText("Внесете корисничко име!");
            return;
        }

        loginButton.setDisable(true);
        
        connection = new ServerConnection();
        connection.setOnMessageReceived(msg -> Platform.runLater(() -> handleIncomingMessage(msg)));
        connection.setOnConnectionLost(error -> Platform.runLater(() -> {
            loginErrorLabel.setText(error);
            showLoginScreen();
        }));

        // Мрежна конекција на посебна нишка за да не блокира GUI
        new Thread(() -> {
            boolean success = connection.connect(ChatProtocol.DEFAULT_HOST, ChatProtocol.DEFAULT_PORT, user);
            Platform.runLater(() -> {
                if (success) {
                    currentUsername = user;
                    showChatScreen();
                } else {
                    loginButton.setDisable(false);
                }
            });
        }).start();
    }

    @FXML
    private void onSendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || connection == null) return;

        Message msg;
        if (text.startsWith("@")) {
            int spaceIndex = text.indexOf(' ');
            if (spaceIndex > 0) {
                String recipient = text.substring(1, spaceIndex);
                String content = text.substring(spaceIndex + 1);
                msg = new Message(MessageType.PRIVATE, currentUsername, recipient, content);
            } else {
                appendChatSystem("Грешка: Неправилен формат за приватна порака. Користи: @корисник порака");
                return;
            }
        } else {
            msg = new Message(MessageType.CHAT, currentUsername, null, text);
        }

        connection.send(msg);
        messageField.clear();
        
        // Ехо за испратената порака
        if (msg.getType() == MessageType.CHAT) {
            appendChat("Ти", msg.getContent(), msg.getTimestamp().format(TIME_FORMATTER), false);
        } else {
            appendChat("Ти (до " + msg.getRecipient() + ")", msg.getContent(), msg.getTimestamp().format(TIME_FORMATTER), true);
        }
    }

    private void handleIncomingMessage(Message msg) {
        String time = msg.getTimestamp().format(TIME_FORMATTER);
        switch (msg.getType()) {
            case SYSTEM -> {
                // Првата порака е CSV листа на online корисници
                onlineUsers.clear();
                if (!msg.getContent().isEmpty()) {
                    onlineUsers.addAll(Arrays.asList(msg.getContent().split(",")));
                }
            }
            case CHAT -> appendChat(msg.getSender(), msg.getContent(), time, false);
            case PRIVATE -> appendChat(msg.getSender() + " (приватно)", msg.getContent(), time, true);
            case JOIN -> {
                onlineUsers.add(msg.getSender());
                appendChatSystem(msg.getContent());
            }
            case LEAVE -> {
                onlineUsers.remove(msg.getSender());
                appendChatSystem(msg.getContent());
            }
            case ERROR -> appendChatSystem("ГРЕШКА: " + msg.getContent());
            default -> {}
        }
    }

    private void appendChat(String sender, String content, String time, boolean isPrivate) {
        String line = String.format("[%s] %s: %s\n", time, sender, content);
        chatArea.appendText(line);
    }

    private void appendChatSystem(String content) {
        chatArea.appendText(String.format(" *** %s ***\n", content));
    }

    private void showChatScreen() {
        loginBox.setVisible(false);
        loginBox.setManaged(false);
        chatBox.setVisible(true);
        chatBox.setManaged(true);
        chatArea.clear();
        messageField.requestFocus();
    }

    private void showLoginScreen() {
        chatBox.setVisible(false);
        chatBox.setManaged(false);
        loginBox.setVisible(true);
        loginBox.setManaged(true);
        loginButton.setDisable(false);
        onlineUsers.clear();
    }

    public void shutdown() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}
