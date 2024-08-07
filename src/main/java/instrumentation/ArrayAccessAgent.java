package instrumentation;

import java.lang.instrument.Instrumentation;

public class ArrayAccessAgent {
    private static boolean verbose;

    public static void premain(String agentArgs, Instrumentation inst) {
        if (agentArgs != null && agentArgs.contains("-v")) {
            verbose = true;
        }
        log("ArrayAccessAgent running");
        inst.addTransformer(new ArrayAccessTransformer());
    }

    public static void log(String message) {
        if (verbose) {
            System.out.println(message);
        }
    }
}