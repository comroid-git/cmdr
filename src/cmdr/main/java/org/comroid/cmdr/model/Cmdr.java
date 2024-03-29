package org.comroid.cmdr.model;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface Cmdr extends CommandHandler {
    String OPTION_PREFIX = "§§";
    String OPTION_OBTAINER_PREFIX = "°";

    Map<String, CommandBlob> getCommands();

    Stream<Object> getExtraArguments();

    Set<CommandBlob> registerCommands(Class<?>... cls);

    default String prefixAutofillOption(String option) {
        return option;
    }

    boolean handle(String cmd, Object... extraArgs);

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
        default Object handleThrowable(String[] parts, Throwable t) {
            return getUnderlyingCmdr().handleThrowable(parts, t);
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
