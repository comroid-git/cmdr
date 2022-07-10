package org.comroid.cmdr.model;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface Cmdr {
    Set<CommandBlob> registerCommand(Class<?> cls);

    Map<String, CommandBlob> getCommands();

    Stream<Object> getExtraArguments();

    Object handleThrowable(Throwable t);
    void handleResponse(Object o);

    interface Underlying extends Cmdr {
        Cmdr getUnderlyingCmdr();

        @Override
        default Set<CommandBlob> registerCommand(Class<?> cls) {
            return getUnderlyingCmdr().registerCommand(cls);
        }

        @Override
        default Map<String, CommandBlob> getCommands() {
            return getUnderlyingCmdr().getCommands();
        }

        @Override
        default Stream<Object> getExtraArguments() {
            return getUnderlyingCmdr().getExtraArguments();
        }

        @Override
        default Object handleThrowable(Throwable t) {
            return getUnderlyingCmdr().handleThrowable(t);
        }

        @Override
        default void handleResponse(Object o) {
            getUnderlyingCmdr().handleResponse(o);
        }
    }
}
