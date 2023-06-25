package org.comroid.cmdr.model;

public interface CommandHandler {
    Object handleThrowable(String[] parts, Throwable t);

    Object handleInvalidCommand(String[] parts);

    Object handleInvalidArguments(CommandBlob cmd, String[] args);

    void handleResponse(Object o, Object[] extra);
}
