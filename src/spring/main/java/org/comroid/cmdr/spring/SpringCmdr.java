package org.comroid.cmdr.spring;

import org.comroid.cmdr.CommandManager;
import org.comroid.cmdr.model.CommandBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SpringCmdr extends CommandManager {
    private final AnnotationConfigApplicationContext ctx;
    private CmdrHandler handler;
    @Autowired
    private CmdrConfig config;

    public SpringCmdr() {
        this.ctx = new AnnotationConfigApplicationContext() ;
    }

    @PostConstruct
    public void init() {
        final String scan = config.getScan();

        // scan for cmdr related components
        ctx.scan(scan);
        ctx.refresh();

        // register custom handler
        this.handler = ctx.getBeansOfType(CmdrHandler.class)
                .values()
                .stream()
                .findAny()
                .orElseGet(CmdrHandler::new);
        // register command containers
        ctx.getBeansOfType(Object.class)
                .values()
                .stream()
                .filter(bean -> bean.getClass().getPackageName().equals(scan))
                .forEach(this::registerCommands);
    }

    public void handleCommand(String rawCommand) {
        executeCommand(this, rawCommand.split(" "), new Object[0]);
    }

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
        handler.handleResponse(o);
    }
}
