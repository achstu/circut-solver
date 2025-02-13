package test;

import circuit.*;
import solution.*;
import parser.*;

import java.time.Instant;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;

public class Main {
    private static final CircuitSolver solver = new ParallelCircuitSolver();

    private static void testCircuit(Circuit circuit) {
        try {
            Instant start = Instant.now();

            CircuitValue result = solver.solve(circuit);
            boolean value = result.getValue();

            Instant end = Instant.now();
            Duration elapsed = Duration.between(start, end);
            System.out.println("Elapsed: " + elapsed.toMillis() / 1000. + "s");
            System.out.println("Value:   " + value);
        } catch (InterruptedException e) {
            System.out.println("Task interrupted...");
        }
    }

    private static void printCoreInfo() {
        int nCores = Runtime.getRuntime().availableProcessors();
        System.out.println("number of cores " + nCores);
    }

    private static void readFromStdin() {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            String expr = sc.nextLine();
            if (expr.equals("stop")) {
                solver.stop();
            } else {
                Circuit circuit = Parser.parse(expr);
                testCircuit(circuit);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        printCoreInfo();
        readFromStdin();
    }
}