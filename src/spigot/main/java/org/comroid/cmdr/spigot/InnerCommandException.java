package org.comroid.cmdr.spigot;

import org.comroid.cmdr.spigot.MessageColorizer;
import org.comroid.cmdr.spigot.SpigotCmdr;

public class InnerCommandException extends RuntimeException {
    private final MessageColorizer level;

    public MessageColorizer getLevel() {
        return level;
    }

    public String getIngameText() {
        return "[" + getClass().getSimpleName() + "] " + getMessage();
    }

    public InnerCommandException() {
        super();
        this.level = SpigotCmdr.ExceptionColorizer;
    }

    public InnerCommandException(String message) {
        super(message);
        this.level = SpigotCmdr.ExceptionColorizer;
    }

    public InnerCommandException(String message, Throwable cause) {
        super(message, cause);
        this.level = SpigotCmdr.ExceptionColorizer;
    }

    public InnerCommandException(Throwable cause) {
        super(cause);
        this.level = SpigotCmdr.ExceptionColorizer;
    }

    public InnerCommandException(MessageColorizer level) {
        super();
        this.level = level;
    }

    public InnerCommandException(MessageColorizer level, String message) {
        super(message);
        this.level = level;
    }

    public InnerCommandException(MessageColorizer level, String message, Throwable cause) {
        super(message, cause);
        this.level = level;
    }

    public InnerCommandException(MessageColorizer level, Throwable cause) {
        super(cause);
        this.level = level;
    }
}
