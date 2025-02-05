import random

MAX_DEPTH = 10
MAX_WIDTH = 10

terminals = {"true1", "false1"}
non_terminals = {"or", "and", "gt", "lt", "if"}


def randexpr(depth=MAX_DEPTH):

    def form(symbol, n=None, x=None):
        return (
            symbol
            + (str(x) if x is not None else "")
            + "("
            + ",".join(randexpr(depth - 1) for _ in range(n))
            + ")"
        )

    if depth == 0:
        return random.choice(list(terminals))

    symbol = random.choice(list(terminals | non_terminals))
    match symbol:
        case "true1" | "false1":
            return symbol
        case "or" | "and":
            n = random.randint(2, MAX_WIDTH)
            return form(symbol, n)
        case "if":
            return form(symbol, 3)
        case "lt" | "gt":
            n = random.randint(2, MAX_WIDTH)
            x = random.randint(0, MAX_WIDTH)
            return form(symbol, n, x)


print(randexpr())
