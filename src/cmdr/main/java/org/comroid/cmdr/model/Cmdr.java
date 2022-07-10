package org.comroid.cmdr.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Cmdr {
    Set<CommandBlob> register(Class<?> cls);

    Map<String, CommandBlob> getCommands();
}
