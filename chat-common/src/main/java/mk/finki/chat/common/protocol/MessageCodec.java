package mk.finki.chat.common.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import mk.finki.chat.common.exception.ProtocolException;
import mk.finki.chat.common.model.Message;

import java.time.LocalDateTime;

public final class MessageCodec {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private MessageCodec() {
        // utility класа - намерно нема јавен конструктор
    }

    public static String encode(Message message) {
        return GSON.toJson(message);
    }

    public static Message decode(String json) {
        try {
            return GSON.fromJson(json, Message.class);
        } catch (JsonSyntaxException e) {
            throw new ProtocolException("Невалидна JSON порака: " + json, e);
        }
    }
}
