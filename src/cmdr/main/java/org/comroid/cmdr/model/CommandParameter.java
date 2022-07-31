package org.comroid.cmdr.model;

import org.comroid.api.Invocable;
import org.comroid.api.ValueType;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class CommandParameter<T> {
    public final ValueType<T> type;
    public final String name;
    public final int ordinal;
    public final boolean required;
    public final String[] autoCompleteOptions;
    public final @Nullable Invocable<Stream<String>> autoCompleteDelegate;

    public CommandParameter(ValueType<T> type, String name, int ordinal, boolean required, String[] autoCompleteOptions, @Nullable Invocable<Stream<String>> autoCompleteDelegate) {
        this.type = type;
        this.name = name;
        this.ordinal = ordinal;
        this.required = required;
        this.autoCompleteOptions = autoCompleteOptions;
        this.autoCompleteDelegate = autoCompleteDelegate;
    }
}
