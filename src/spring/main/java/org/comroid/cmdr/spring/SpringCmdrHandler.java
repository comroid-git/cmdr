package org.comroid.cmdr.spring;

import com.google.common.flogger.FluentLogger;
import org.comroid.cmdr.model.CommandBlob;

import java.util.Arrays;

public class SpringCmdrHandler {
    protected final FluentLogger log = FluentLogger.forEnclosingClass();

    protected void handleResponse(Object user, Object o) {
        log.atSevere().log("Unhandled response for user %s: %s", user, o);
    }

    protected String handleThrowable(Throwable t) {
        log.atSevere().log("Unhandled exception: %s", t);
        return "An internal error occurred";
    }

    protected String handleInvalidArguments(CommandBlob cmd, String[] args) {
        log.atSevere().log("Invalid arguments for command %s: %s", cmd.name(), args);
        return "Invalid arguments";
    }
}
