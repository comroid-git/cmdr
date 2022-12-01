package org.comroid.cmdr.spring;

import org.comroid.cmdr.CommandManager;
import org.comroid.cmdr.model.CommandBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpringCmdr {
    private final CommandManager cmdr;
    private final AnnotationConfigApplicationContext ctx;
    @Autowired
    private SpringCmdrHandler handler;
    @Autowired
    private CmdrConfig config;

    public SpringCmdr() {
        this.ctx = new AnnotationConfigApplicationContext();
        this.cmdr = new CommandManager() {
            @Override
            public Object handleThrowable(Throwable t) {
                return handler.handleThrowable(t);
            }

            @Override
            public Object handleInvalidArguments(CommandBlob cmd, String[] args) {
                return handler.handleInvalidArguments(cmd, args);
            }

            @Override
            public void handleResponse(Object o, Object[] extraArgs) {
                handler.handleResponse(extraArgs[0], o);
            }
        };
    }

    @PostConstruct
    public void init() {
        final String scan = config.getScan();

        // scan for cmdr related components
        ctx.scan(scan);
        ctx.refresh();

        // register command containers
        ctx.getBeansOfType(Object.class)
                .values()
                .stream()
                .filter(bean -> bean.getClass().getPackageName().equals(scan))
                .forEach(cmdr::registerCommands);
    }

    public List<CommandBlob> getCommands() {
        return cmdr.getCommands()
                .values()
                .stream()
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }

    public void handleCommand(Object user, String rawCommand) {
        cmdr.executeCommand(cmdr, rawCommand.split(" "), new Object[]{user});
    }
}
