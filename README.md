### Overview

This project focuses on evaluating Boolean expressions concurrently.

### Boolean Expresions

| Syntax                 | Name                     | Semantics                                                          |
|------------------------|--------------------------|--------------------------------------------------------------------|
| `TRUE`, `FALSE`        | Boolean constants        | Represents the logical values `TRUE` and `FALSE`.                  |
| `NOT(e)`               | Negation                 | `TRUE` iff `e` is `FALSE`.                                         |
| `AND(e1, e2, ...)`     | Conjunction              | `TRUE` iff all expressions evaluate to `TRUE`.                     |
| `OR(e1, e2, ...)`      | Disjunction              | `TRUE` iff at least one expression evaluates to `TRUE`             |
| `IF(e1, e2, e3)`       | Conditional expression   | Evaluates to `e2` if `e1` is `TRUE`, otherwise evaluates to  `e3`. |
| `GTx(e1, e2, ..., en)` | Threshold (greater than) | `TRUE` iff more than `x` expressions evaluate to `TRUE`            |
| `LTx(e1, e2, ..., en)` | Threshold (less than)    | `TRUE` iff less than `x` expressions evaluate to `TRUE`.           |

### Lazy Evaluation and Concurrency

Boolean expressions are usually evaluated from left to right. Lazy evaluation allows skipping the computation of some subexpressions if the already computed values determine the result of the entire expression. Notably, if expressions do not generate side effects, the order of evaluating subexpressions should not affect the final value. Therefore, subexpressions can be evaluated concurrently.