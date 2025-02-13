package parser;

import circuit.Circuit;
import circuit.CircuitNode;
import circuit.NodeType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

public class Parser {
    private static final Pattern TERMINAL_PATTERN = Pattern.compile("^(true|false)(\\d*)$");
    private static final Pattern NON_TERMINAL_PATTERN = Pattern.compile("^(not|or|and|if|gt|lt)(\\d*)\\((.*)\\)$");

    private static CircuitNode parseNode(String expr) {
        Matcher terminalMatcher = TERMINAL_PATTERN.matcher(expr);
        if (terminalMatcher.matches()) {
            boolean value = Boolean.parseBoolean(terminalMatcher.group(1));
            int delay = Integer.parseInt("0" + terminalMatcher.group(2));
            return CircuitNode.mk(value, Duration.ofSeconds(delay));
        }

        Matcher nonTerminalMatcher = NON_TERMINAL_PATTERN.matcher(expr);
        if (nonTerminalMatcher.matches()) {
            String op = nonTerminalMatcher.group(1);
            int threshold = Integer.parseInt("0" + nonTerminalMatcher.group(2));
            String args = nonTerminalMatcher.group(3);

            CircuitNode[] nodeArgs = splitArgs(args)
                    .stream()
                    .map(Parser::parseNode)
                    .toList()
                    .toArray(new CircuitNode[0]);

            return switch (op) {
                case "not" -> CircuitNode.mk(NodeType.NOT, nodeArgs);
                case "or" -> CircuitNode.mk(NodeType.OR, nodeArgs);
                case "and" -> CircuitNode.mk(NodeType.AND, nodeArgs);
                case "if" -> CircuitNode.mk(NodeType.IF, nodeArgs);
                case "gt" -> CircuitNode.mk(NodeType.GT, threshold, nodeArgs);
                case "lt" -> CircuitNode.mk(NodeType.LT, threshold, nodeArgs);
                default -> throw new IllegalArgumentException("Invalid operand: " + op);
            };
        }

        throw new IllegalArgumentException("Invalid expression: " + expr);
    }

    private static List<String> splitArgs(String args) {
        List<String> result = new ArrayList<>();
        int depth = 0;

        StringBuilder arg = new StringBuilder();
        for (char c : args.toCharArray()) {
            if (depth == 0 && c == ',') {
                result.add(arg.toString());
                arg = new StringBuilder();
            } else {
                if (c == '(') depth++;
                else if (c == ')') depth--;
                arg.append(c);
            }
        }
        result.add(arg.toString());
        return result;
    }

    public static Circuit parse(String expr) {
        return new Circuit(Parser.parseNode(expr));
    }
}
