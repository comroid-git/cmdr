package org.comroid.cmdr.model;

public interface CommandHandler {
    Object handleThrowable(Throwable t);

    Object handleInvalidArguments(CommandBlob cmd, String[] userArgs);

    void handleResponse(Object o, Object[] extraArgs);
}
