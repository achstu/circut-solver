package demo;

import circuit.*;
import parser.Parser;
// import solution.ParallelCircuitSolver;
import java.io.File;
import java.time.Duration;
import java.util.Scanner;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        // CircuitSolver solver = new ParallelCircuitSolver();
        CircuitSolver sequentialSolver = new SequentialSolver();

        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            String expr = sc.next();
            Circuit circuit = Parser.parse(expr);

            CircuitValue result = sequentialSolver.solve(circuit);
            System.out.println("EXPR = " + expr);
            System.out.println("VAL  = " + result.getValue());

        }
    }
}
