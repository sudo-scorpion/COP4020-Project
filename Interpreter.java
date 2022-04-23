package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);

    public Interpreter(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {
        ast.getFields().forEach(this::visit);
        ast.getMethods().forEach(this::visit);
        List<Environment.PlcObject> args = new ArrayList<Environment.PlcObject>();
        return scope.lookupFunction("main", 0).invoke(args);
    }

    @Override
    public Environment.PlcObject visit(Ast.Field ast) {
        if (ast.getValue().isPresent())
            scope.defineVariable(ast.getName(), visit(ast.getValue().get()));
        else
            scope.defineVariable(ast.getName(), Environment.NIL);
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Method ast) {
        scope.defineFunction(ast.getName(), ast.getParameters().size(), args -> {
            try {
                scope = new Scope(scope);
                ast.getParameters().forEach(param -> {args.forEach(arg -> {scope.defineVariable(param, arg);});});
                ast.getStatements().forEach(this::visit);
            }
            catch (Return r) {
                return r.value;
            }
            finally {
                scope = scope.getParent();
            }
            return Environment.NIL;
        });
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Expression ast) {
        visit(ast.getExpression());
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Declaration ast) {
        if (ast.getValue().isPresent())
            scope.defineVariable(ast.getName(), visit(ast.getValue().get()));
        else
            scope.defineVariable(ast.getName(), Environment.NIL);
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Assignment ast) {
        if (ast.getReceiver().getClass() == Ast.Expr.Access.class) {
            try {
                scope = new Scope(scope);
                Ast.Expr.Access temp = Ast.Expr.Access.class.cast(ast.getReceiver());
                if (!(temp.getReceiver().isPresent())){
                    scope.lookupVariable(temp.getName()).setValue(visit(ast.getValue()));
                }
                else{
                    Environment.PlcObject rec = visit(temp.getReceiver().get());
                    rec.setField(temp.getName(), visit(ast.getValue()));
                }
            } finally {
                scope = scope.getParent();
            }
        }
        else
            throw new RuntimeException("Not Access Type");
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.If ast) {
        if (!requireType(Boolean.class, visit(ast.getCondition()))) { // todo debug comments
            try {
                scope = new Scope(scope);
                for (Ast.Stmt stmt : ast.getElseStatements()) {
                    visit(stmt);
                }
            }
            catch(Exception e){/* System.out.println("115");*/}
            finally {
                scope = scope.getParent();
            }
        }
        else{
            try {
                scope = new Scope(scope);
                for (Ast.Stmt stmt : ast.getThenStatements()) {
                    visit(stmt);
                }
            }
            catch (Exception e){/* System.out.println("129");*/}
            finally {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.For ast) {
        Iterable it = requireType(Iterable.class, visit(ast.getValue()));
        it.forEach( e -> {
            try {
                scope = new Scope(scope);
                scope.defineVariable(ast.getName(), (Environment.PlcObject) e);
                ast.getStatements().forEach( stmt -> {visit(stmt);});
            } finally {
                scope = scope.getParent();
            }
        });
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.While ast) {
        while (requireType(Boolean.class, visit(ast.getCondition()))) {
            try {
                scope = new Scope(scope);
                for (Ast.Stmt stmt : ast.getStatements()) {
                    visit(stmt);
                }
            } finally {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Return ast) {
        throw new Return(visit(ast.getValue()));
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Literal ast) {
        if (ast.getLiteral() == null)
            return Environment.NIL;
        else
            return Environment.create(ast.getLiteral());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Group ast) {
        return visit(ast.getExpression());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Binary ast) {

        String operator = ast.getOperator();
        // AND OR
        if (operator.equals("AND")){
            if (requireType(Boolean.class, visit(ast.getLeft())) == requireType(Boolean.class, visit(ast.getRight())))
                return Environment.create(ast.getLeft());
            else
                return Environment.create(Boolean.FALSE);
        }
        else if (operator.equals("OR")){
            if (requireType(Boolean.class, visit(ast.getLeft())) == Boolean.TRUE)
                return visit(ast.getLeft());
            else if (requireType(Boolean.class, visit(ast.getRight())) == Boolean.TRUE)
                return visit(ast.getRight());
            else
                return Environment.create(Boolean.FALSE);
        }
        // COMPARISON
        else if (operator.equals("<")){
            if (visit(ast.getLeft()).getValue() instanceof Comparable && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {
                int i;

                Comparable<Object> left = (Comparable<Object>)visit(ast.getLeft()).getValue();
                Comparable<Object> right = (Comparable<Object>)visit(ast.getRight()).getValue();
                i = left.compareTo(right);

                if (i < 0)
                    return Environment.create(Boolean.TRUE);
                else
                    return Environment.create(Boolean.FALSE);
            }
        }
        else if (operator.equals(">")){
            if (visit(ast.getLeft()).getValue() instanceof Comparable && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {
                int i;

                Comparable<Object> left = (Comparable<Object>)visit(ast.getLeft()).getValue();
                Comparable<Object> right = (Comparable<Object>)visit(ast.getRight()).getValue();
                i = left.compareTo(right);

                if (i > 0)
                    return Environment.create(Boolean.TRUE);
                else
                    return Environment.create(Boolean.FALSE);
            }
        }
        else if (operator.equals("<=")){
            if (visit(ast.getLeft()).getValue() instanceof Comparable && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {
                int i;

                Comparable<Object> left = (Comparable<Object>)visit(ast.getLeft()).getValue();
                Comparable<Object> right = (Comparable<Object>)visit(ast.getRight()).getValue();
                i = left.compareTo(right);

                if (i <= 0)
                    return Environment.create(Boolean.TRUE);
                else
                    return Environment.create(Boolean.FALSE);
            }
        }
        else if (operator.equals(">=")){
            if (visit(ast.getLeft()).getValue() instanceof Comparable && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {
                int i;

                Comparable<Object> left = (Comparable<Object>)visit(ast.getLeft()).getValue();
                Comparable<Object> right = (Comparable<Object>)visit(ast.getRight()).getValue();
                i = left.compareTo(right);

                if (i >= 0)
                    return Environment.create(Boolean.TRUE);
                else
                    return Environment.create(Boolean.FALSE);
            }
        }
        // EQUALITY
        else if (operator.equals("==")){
            if (visit(ast.getLeft()).getValue().equals(visit(ast.getRight()).getValue()))
                return Environment.create(Boolean.TRUE);
            else
                return Environment.create(Boolean.FALSE);
        }
        else if (operator.equals("!=")){
            if (visit(ast.getLeft()).getValue().equals(visit(ast.getRight()).getValue()))
                return Environment.create(Boolean.FALSE);
            else
                return Environment.create(Boolean.TRUE);
        }
        // ADDITION SUBTRACTION
        else if (operator.equals("+")) {
            if (visit(ast.getLeft()).getValue().getClass() == BigInteger.class && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass())
                return Environment.create(BigInteger.class.cast(visit(ast.getLeft()).getValue()).add(BigInteger.class.cast(visit(ast.getRight()).getValue())));
            else if (visit(ast.getLeft()).getValue().getClass() == BigDecimal.class && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass())
                return Environment.create(BigDecimal.class.cast(visit(ast.getLeft()).getValue()).add(BigDecimal.class.cast(visit(ast.getRight()).getValue())));
            // Must also handle string concatenation?
            else if (visit(ast.getLeft()).getValue().getClass() == String.class || visit(ast.getRight()).getValue().getClass() == String.class)
                return Environment.create(visit(ast.getLeft()).getValue().toString() + visit(ast.getRight()).getValue().toString());
            else
                throw new RuntimeException("Addition Unsuccessful, incompatible types");
        }
        else if (operator.equals("-")) {
            if ((visit(ast.getLeft()).getValue().getClass() == BigDecimal.class || visit(ast.getLeft()).getValue().getClass() == BigInteger.class) && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {
                // Integer
                if (visit(ast.getLeft()).getValue().getClass() == BigInteger.class)
                        return Environment.create(BigInteger.class.cast(visit(ast.getLeft()).getValue()).subtract(BigInteger.class.cast(visit(ast.getRight()).getValue())));
                // Decimal
                else if (visit(ast.getLeft()).getValue().getClass() == BigDecimal.class && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass())
                        return Environment.create(BigDecimal.class.cast(visit(ast.getLeft()).getValue()).subtract(BigDecimal.class.cast(visit(ast.getRight()).getValue())));
            }
            else
                // Do not need to handle strings, subtraction makes no sense
                throw new RuntimeException("Subtraction Unsuccessful, incompatible types");
        }
        // MULTIPLICATION DIVISION
        else if (operator.equals("*")) {
            if ((visit(ast.getLeft()).getValue().getClass() == BigDecimal.class || visit(ast.getLeft()).getValue().getClass() == BigInteger.class) && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {
                // Integer
                if (visit(ast.getLeft()).getValue().getClass() == BigInteger.class)
                    return Environment.create(BigInteger.class.cast(visit(ast.getLeft()).getValue()).multiply(BigInteger.class.cast(visit(ast.getRight()).getValue())));
                // Decimal
                else if (visit(ast.getLeft()).getValue().getClass() == BigDecimal.class && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass())
                    return Environment.create(BigDecimal.class.cast(visit(ast.getLeft()).getValue()).multiply(BigDecimal.class.cast(visit(ast.getRight()).getValue())));
            }
            else
                throw new RuntimeException("Multiplication Unsuccessful, incompatible types");
        }
        else if (operator.equals("/")) {
            if ((visit(ast.getLeft()).getValue().getClass() == BigDecimal.class || visit(ast.getLeft()).getValue().getClass() == BigInteger.class) && visit(ast.getLeft()).getValue().getClass() == visit(ast.getRight()).getValue().getClass()) {

                // Error handling - throw exception if division by 0
                if ((visit(ast.getRight()).getValue().getClass() == BigInteger.class && visit(ast.getRight()).getValue().equals(BigInteger.ZERO)))
                    throw new RuntimeException("Division by zero error");

                // Otherwise, continue
                if (visit(ast.getLeft()).getValue().getClass() == BigInteger.class)
                    return Environment.create(BigInteger.class.cast(visit(ast.getLeft()).getValue()).divide(BigInteger.class.cast(visit(ast.getRight()).getValue())));
                else if (visit(ast.getLeft()).getValue().getClass() == BigDecimal.class)
                    return Environment.create(BigDecimal.class.cast(visit(ast.getLeft()).getValue()).divide(BigDecimal.class.cast(visit(ast.getRight()).getValue()), RoundingMode.HALF_EVEN));
            }
            else
                throw new RuntimeException("Division Unsuccessful, incompatible types");
        }

        // Otherwise, throw an error
        throw new RuntimeException("Unexpected error, given types are incompatible with binary operands.");
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Access ast) {
        if (ast.getReceiver().isPresent()) {
            Environment.PlcObject rec = visit(ast.getReceiver().get());
            return rec.getField(ast.getName()).getValue();
        }
        return scope.lookupVariable(ast.getName()).getValue();
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Function ast) {
        try {
            scope = new Scope(scope);
            List<Environment.PlcObject> args = new ArrayList<Environment.PlcObject>();
            for (int i = 0; i < ast.getArguments().size(); i++) {
                args.add(visit(ast.getArguments().get(i)));
            }

            Boolean receiver = ast.getReceiver().isPresent();
            if (receiver) {
                Environment.PlcObject rec = visit(ast.getReceiver().get());
                return rec.callMethod(ast.getName(), args);
            }
            return scope.lookupFunction(ast.getName(), args.size()).invoke(args);
        } finally {
            scope = scope.getParent();
        }
    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject value;

        private Return(Environment.PlcObject value) {
            this.value = value;
        }

    }

}
