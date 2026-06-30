package mk.finki.chat.common.model;

import java.util.Objects;


public class User {

    private final String username;
    private volatile boolean online;

    public User(String username) {
        this.username = Objects.requireNonNull(username, "username не смее да биде null");
        this.online = true;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return username.equalsIgnoreCase(user.username);
    }

    @Override
    public int hashCode() {
        return username.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return "User{username='" + username + "', online=" + online + '}';
    }
}