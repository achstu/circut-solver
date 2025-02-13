### Lazy Evaluation and Concurrency

Boolean computations are usually evaluated from left to right. Lazy evaluation allows skipping the computation of some subexpressions if the already computed values determine the result of the entire expression. Notably, if expressions do not generate side effects, the order of evaluating subexpressions should not affect the final value. Therefore, subexpressions can be evaluated concurrently.
