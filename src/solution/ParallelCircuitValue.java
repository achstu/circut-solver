package solution;

import circuit.CircuitValue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ParallelCircuitValue implements CircuitValue {
    private final Future<Boolean> futureValue;

    public ParallelCircuitValue(Future<Boolean> futureValue) {
        this.futureValue = futureValue;
    }

    @Override
    public boolean getValue() throws InterruptedException {
        try {
            return this.futureValue.get();
        } catch (ExecutionException e) {
            throw new InterruptedException();
        }
    }
}
