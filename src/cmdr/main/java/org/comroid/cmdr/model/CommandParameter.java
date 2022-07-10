package org.comroid.cmdr.model;

import org.comroid.api.ValueType;

public class CommandParameter<T> {
    public final ValueType<T> type;
    public final String name;
    public final int ordinal;
    public final boolean required;

    public CommandParameter(ValueType<T> type, String name, int ordinal, boolean required) {
        this.type = type;
        this.name = name;
        this.ordinal = ordinal;
        this.required = required;
    }
}
