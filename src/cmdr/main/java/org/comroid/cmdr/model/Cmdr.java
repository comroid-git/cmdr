package org.comroid.cmdr.model;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface Cmdr {
    String OPTION_PREFIX = "§§";

    Map<String, CommandBlob> getCommands();

    Stream<Object> getExtraArguments();

    Set<CommandBlob> registerCommands(Class<?>... cls);

    Object handleThrowable(Throwable t);

    Object handleInvalidArguments(CommandBlob cmd, String[] userArgs);

    void handleResponse(Object o, Object[] extraArgs);

    default String prefixAutofillOption(String option) {
        return option;
    }

    interface Underlying extends Cmdr {
        Cmdr getUnderlyingCmdr();

        @Override
        default Map<String, CommandBlob> getCommands() {
            return getUnderlyingCmdr().getCommands();
        }

        @Override
        default Stream<Object> getExtraArguments() {
            return getUnderlyingCmdr().getExtraArguments();
        }

        @Override
        default Set<CommandBlob> registerCommands(Class<?>... cls) {
            return getUnderlyingCmdr().registerCommands(cls);
        }

        @Override
        default Object handleThrowable(Throwable t) {
            return getUnderlyingCmdr().handleThrowable(t);
        }

        @Override
        default Object handleInvalidArguments(CommandBlob cmd, String[] userArgs) {
            return getUnderlyingCmdr().handleInvalidArguments(cmd, userArgs);
        }

        @Override
        default void handleResponse(Object o, Object[] extraArgs) {
            getUnderlyingCmdr().handleResponse(o, extraArgs);
        }
    }
}
