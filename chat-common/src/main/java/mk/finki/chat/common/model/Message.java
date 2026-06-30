package mk.finki.chat.common.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Message {

    private final MessageType type;
    private final String sender;
    private final String recipient;      // null ако пораката не е приватна
    private final String content;
    private final LocalDateTime timestamp;

    public Message(MessageType type, String sender, String recipient, String content) {
        this.type = Objects.requireNonNull(type, "type не смее да биде null");
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isPrivate() {
        return type == MessageType.PRIVATE;
    }

    @Override
    public String toString() {
        return "Message{type=" + type + ", sender='" + sender + "', recipient='" + recipient
                + "', content='" + content + "', timestamp=" + timestamp + '}';
    }
}