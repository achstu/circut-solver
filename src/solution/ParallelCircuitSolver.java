package cp2024.solution;

import cp2024.circuit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class ParallelCircuitSolver implements CircuitSolver {
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static class NodeEvaluator {
        private final CircuitNode node;
        private final int totalChildren;
        private final int[] trueFalseCounter = new int[2];

        public NodeEvaluator(CircuitNode node, int totalChildren) {
            this.node = node;
            this.totalChildren = totalChildren;
        }

        public void addValue(boolean value) {
            trueFalseCounter[value ? 1 : 0] += 1;
        }

        private int falseCount() {
            return trueFalseCounter[0];
        }

        private int trueCount() {
            return trueFalseCounter[1];
        }

        private int totalEvaluated() {
            return falseCount() + trueCount();
        }

        private boolean isEvaluationComplete() {
            return totalEvaluated() >= totalChildren;
        }

        private int maxRemainingTrueCount() {
            return totalChildren - falseCount();
        }


        private Boolean evaluateSimpleNode(SimpleNode node) {
            return switch (node.getType()) {
                case NOT -> {
                    if (isEvaluationComplete()) yield !(trueCount() == 1);
                    yield null;
                }
                case AND -> {
                    if (isEvaluationComplete()) yield falseCount() == 0;
                    if (falseCount() > 0) yield false;
                    yield null;
                }
                case OR -> {
                    if (isEvaluationComplete()) yield trueCount() > 0;
                    if (trueCount() > 0) yield true;
                    yield null;
                }
                default -> throw new IllegalStateException("Invalid node type: " + node.getType());
            };
        }

        private Boolean evaluateThresholdNode(ThresholdNode node) {
            int threshold = node.getThreshold();
            return switch (node.getType()) {
                case GT -> {
                    if (isEvaluationComplete()) yield trueCount() > threshold;
                    if (trueCount() > threshold) yield true;
                    if (maxRemainingTrueCount() <= threshold) yield false;
                    yield null;
                }
                case LT -> {
                    if (isEvaluationComplete()) yield trueCount() < threshold;
                    if (trueCount() >= threshold) yield false;
                    if (maxRemainingTrueCount() < threshold) yield true;
                    yield null;
                }
                default -> throw new IllegalStateException("Invalid node type: " + node.getType());
            };
        }

        public Optional<Boolean> evaluate() throws InterruptedException {
           return Optional.ofNullable(switch (node) {
               case LeafNode leafNode -> leafNode.getValue();
               case SimpleNode simpleNode -> evaluateSimpleNode(simpleNode);
               case ThresholdNode thresholdNode -> evaluateThresholdNode(thresholdNode);
           });
        }
    }

    private static class EvaluationTask implements Callable<Boolean> {
        private final CircuitNode node;

        private final ExecutorService solverThreadPool;
        private final ExecutorCompletionService<Boolean> childTasks;

        public EvaluationTask(CircuitNode node, ExecutorService threadPool) {
            this.node = node;
            this.solverThreadPool = threadPool;
            this.childTasks = new ExecutorCompletionService<>(threadPool);
        }

        @Override
        public Boolean call() throws InterruptedException {
            List<Future<Boolean>> futures = new ArrayList<>();
            try {
                // enforce that getArgs() method on node is called exactly once
                final CircuitNode[] nodeArgs = node.getArgs();

                for (CircuitNode child : nodeArgs) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    futures.add(childTasks.submit(new EvaluationTask(child, solverThreadPool)));
                }

                // separate logic for IF nodes
                if (node.getType() == NodeType.IF) {
                    boolean condition = futures.get(0).get();
                    int activeBranch = condition ? 1 : 2;

                    // TODO if 'then' and 'else' branches evaluates the same
                    // 'condition' should be canceled instead

                    return futures.get(activeBranch).get();
                }

                // then logic for LeafNode, SimpleNode and ThresholdNode
                NodeEvaluator evaluator = new NodeEvaluator(node, nodeArgs.length);
                Optional<Boolean> evaluation;
                while ((evaluation = evaluator.evaluate()).isEmpty()) {
                    Future<Boolean> nextChildValue = childTasks.take();
                    boolean value = nextChildValue.get();
                    evaluator.addValue(value);
                }
                return evaluation.get();

            } catch (InterruptedException | ExecutionException e) {
                // propagate exception
                throw new InterruptedException(e.toString());
            } finally {
                // cancel all added tasks
                futures.forEach(f -> f.cancel(true));
            }
        }
    }


    @Override
    public CircuitValue solve(Circuit c) {
        if (threadPool.isShutdown()) return new BrokenCircuitValue();
        return new ParallelCircuitValue(
                threadPool.submit(new EvaluationTask(c.getRoot(), threadPool))
        );
    }

    @Override
    public void stop() {
        threadPool.shutdownNow();
    }
}
