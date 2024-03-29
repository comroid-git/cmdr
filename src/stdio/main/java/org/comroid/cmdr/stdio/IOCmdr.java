package org.comroid.cmdr.stdio;

import org.comroid.cmdr.CommandManager;
import org.comroid.cmdr.model.Cmdr;
import org.comroid.cmdr.model.Command;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class IOCmdr extends CommandManager implements Cmdr, Closeable {
    private final InputStream input;
    private final OutputStream output;
    private final PrintWriter print;

    public IOCmdr(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
        this.print = new PrintWriter(output instanceof PrintStream ? output : new PrintStream(output));
    }

    @Command
    public static String hello_world() {
        return "Hello, world!";
    }

    public static void main(String... extraClassNames) throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();
        for (String clsName : extraClassNames)
            classes.add(Class.forName(clsName));
        run(classes.toArray(new Class[0]));
    }

    public static void run(Class<?>... classes) throws IOException {
        try (IOCmdr cmdr = new IOCmdr(System.in, System.out)) {
            for (Class<?> extraClass : classes)
                cmdr.registerCommands(extraClass);
            cmdr.run();
        }
    }

    public void run() throws IOException {
        long time;
        try (
                InputStreamReader isr = new InputStreamReader(input);
                BufferedReader in = new BufferedReader(isr);
        ) {
            while (true) {
                print.write("cmdr> ");
                String input = in.readLine();
                if ("exit".equals(input)) break;
                String[] command = input.split(" ");
                time = System.nanoTime();
                Object response = executeCommand(this, command, getExtraArguments().toArray());
                time = System.nanoTime() - time;
                print.write(response.toString());
                print.write(" [" + TimeUnit.NANOSECONDS.toMillis(time) + "ms]\n");
            }
        }
    }

    @Override
    public void handleResponse(Object o, Object[] extraArgs) {
        print.write(String.valueOf(o));
    }

    @Override
    public void close() throws IOException {
        input.close();
        print.close();
        output.close();
    }
}
