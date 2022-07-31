package org.comroid.cmdr.model;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface Cmdr {
    String OPTION_PREFIX = "§§";
    String OPTION_OBTAINER_PREFIX = "°";

    Map<String, CommandBlob> getCommands();

    Stream<Object> getExtraArguments();

    Set<CommandBlob> registerCommands(Cmdr cmdr, Class<?>... cls);

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
        default Set<CommandBlob> registerCommands(Cmdr cmdr, Class<?>... cls) {
            return getUnderlyingCmdr().registerCommands(cmdr, cls);
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
