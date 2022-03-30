package plc.project;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Optional;
import java.util.stream.IntStream;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */

public final class Analyzer implements Ast.Visitor<Void> {
    public Scope scope;
    private Ast.Method method;

    public Analyzer(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Void visit(Ast.Source ast) {
        try {
            if (!ast.getFields().isEmpty()) {
                for (int i = 0; i < ast.getFields().size(); i++)
                    visit(ast.getFields().get(i));
            }

            boolean main = false;

            if (!ast.getMethods().isEmpty()) {
                for (int i = 0; i < ast.getMethods().size(); i++){
                    visit(ast.getMethods().get(i));

                    if (ast.getMethods().get(i).getName().equals("main")) {
                        main = true;
                    }
                }
            }

            if (!main) {
                throw new RuntimeException("Unsuccessful - No main method");
            }
        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        try {
            if (!ast.getValue().isPresent()) {
                scope.defineVariable(ast.getName(), ast.getName(), Environment.getType(ast.getTypeName()), Environment.NIL);
            }
            else {
                visit(ast.getValue().get());
                requireAssignable(Environment.getType(ast.getTypeName()), ast.getValue().get().getType());
                scope.defineVariable(ast.getName(), ast.getName(), ast.getValue().get().getType(), Environment.NIL);
            }
            ast.setVariable(scope.lookupVariable(ast.getName()));

        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        try {
            Environment.Type returnType = Environment.Type.NIL;

            if (ast.getReturnTypeName().isPresent())
                returnType = Environment.getType(ast.getReturnTypeName().get());

            scope.defineVariable("returnType", "returnType", returnType, Environment.NIL);

            List<String> p = ast.getParameterTypeNames();
            Environment.Type[] paramTypes = new Environment.Type[p.size()];

            if (!p.isEmpty()) {
                int i = 0;
                while (i < p.size()) {
                    paramTypes[i] = Environment.getType(p.get(i));
                    i++;
                }
            }

            scope.defineFunction(ast.getName(), ast.getName(), Arrays.asList(paramTypes), returnType, args -> Environment.NIL);

            List<Ast.Stmt> a = ast.getStatements();

            if (!a.isEmpty()) {
                int i = 0;
                while (i < ast.getStatements().size()) {
                    try {
                        scope = new Scope(scope);
                        visit(ast.getStatements().get(i));
                    } finally {
                        scope = scope.getParent();
                    }
                    i++;
                }
            }

            ast.setFunction(scope.lookupFunction(ast.getName(), ast.getParameters().size()));
        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        visit(ast.getExpression());
        try {
            if (ast.getExpression().getClass() != Ast.Expr.Function.class) {
                throw new RuntimeException("Unsuccessful, not a function type");
            }
        }
        catch (RuntimeException r) {
            throw new RuntimeException(r);
        }
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        try {
            if (ast.getValue().isPresent()) {
                visit(ast.getValue().get());
                scope.defineVariable(ast.getName(), ast.getName(), ast.getValue().get().getType(), Environment.NIL);
            }
            else {
                scope.defineVariable(ast.getName(), ast.getName(), Environment.getType(ast.getTypeName().get()), Environment.NIL);
            }

            ast.setVariable(scope.lookupVariable(ast.getName()));

        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        try {
            if (ast.getReceiver().getClass() != Ast.Expr.Access.class) {
                throw new RuntimeException("Unsuccessful, not an access type");
            }
            visit(ast.getValue());
            visit(ast.getReceiver());
            requireAssignable(ast.getReceiver().getType(), ast.getValue().getType());
        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        try {
            if (ast.getThenStatements().isEmpty()) {
                throw new RuntimeException("Unsuccessful -- No then statement");
            }

            visit(ast.getCondition());
            requireAssignable(Environment.Type.BOOLEAN, ast.getCondition().getType());

            for (int i = 0; i < ast.getElseStatements().size(); i++) {
                try {
                       scope = new Scope(scope);
                       visit(ast.getElseStatements().get(i));
                } finally {
                       scope = scope.getParent();
                }
            }

            for (int i = 0; i < ast.getThenStatements().size(); i++) {
                try {
                    scope = new Scope(scope);
                    visit(ast.getThenStatements().get(i));
                } finally {
                    scope = scope.getParent();
                }
            }

        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        try {
            if (ast.getStatements().isEmpty())
                throw new RuntimeException("Unsuccessful - No statements in for loop");

            visit(ast.getValue());
            requireAssignable(Environment.Type.INTEGER_ITERABLE, ast.getValue().getType());

            for (Ast.Stmt elem : ast.getStatements()) {
                try {
                    scope = new Scope(scope);
                    scope.defineVariable(ast.getName(), ast.getName(), Environment.Type.INTEGER, Environment.NIL);
                } finally {
                    scope = scope.getParent();
                }
            }
        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        // this one good, from lecture - do not change
        try {
            visit(ast.getCondition());
            requireAssignable(Environment.Type.BOOLEAN, ast.getCondition().getType());
            try {
                scope = new Scope(scope);
                for (Ast.Stmt stmt : ast.getStatements()) {
                    visit(stmt);
                }
            } finally {
                scope = scope.getParent();
            }
        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        try {
            visit(ast.getValue());

            Environment.Variable ret = scope.lookupVariable("returnType");
            requireAssignable(ret.getType(), ast.getValue().getType());
        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        try {
            if (ast.getLiteral() instanceof String)
                ast.setType(Environment.Type.STRING);
            else if (ast.getLiteral() instanceof Character)
                ast.setType(Environment.Type.CHARACTER);
            else if (ast.getLiteral() == Environment.NIL)
                ast.setType(Environment.Type.NIL);
            else if (ast.getLiteral() instanceof Boolean)
                ast.setType(Environment.Type.BOOLEAN);
            else if (ast.getLiteral() instanceof BigInteger) {
                BigInteger temp = (BigInteger) ast.getLiteral();

                if (temp.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0 && // added to make sure the value within integer range
                        temp.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0)
                    ast.setType(Environment.Type.INTEGER);
                else
                    throw new RuntimeException("Integer value is not within range");
            }
            else if (ast.getLiteral() instanceof BigDecimal) {
                    BigDecimal temp = (BigDecimal) ast.getLiteral();

                if (temp.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) <= 0 && // added to make sure the value within decimal range
                        temp.compareTo(BigDecimal.valueOf(Double.MIN_VALUE)) >= 0)
                    ast.setType(Environment.Type.DECIMAL);
                else
                    throw new RuntimeException("Decimal value is not within range");
            }
            else {
                throw new RuntimeException("Unsuccessful -- no such type");
            }
        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        try {
            visit(ast.getExpression());

            if (ast.getExpression().getClass() != Ast.Expr.Binary.class)
                throw new RuntimeException("Unsuccessful, not a binary type");

        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        try {
            visit(ast.getLeft());
            visit(ast.getRight());

            switch (ast.getOperator()) {
                case "OR":
                case "AND":
                    if (ast.getLeft().getType() == Environment.Type.BOOLEAN && ast.getRight().getType() == Environment.Type.BOOLEAN) {
                        ast.setType(ast.getLeft().getType());
                        break;
                    }
                    throw new RuntimeException("Unsuccessful, not a boolean type");
                case "<":
                case "<=":
                case ">":
                case ">=":
                case "==":
                case "!=":
                    requireAssignable(Environment.Type.COMPARABLE, ast.getLeft().getType());
                    requireAssignable(Environment.Type.COMPARABLE, ast.getRight().getType());
                    ast.setType(Environment.Type.BOOLEAN);
                    break;
                case "+":
                    if (ast.getLeft().getType() == Environment.Type.STRING || ast.getRight().getType() == Environment.Type.STRING) {
                        ast.setType(Environment.Type.STRING);
                    } else if (ast.getLeft().getType() == Environment.Type.INTEGER || ast.getLeft().getType() == Environment.Type.DECIMAL) {
                        if (ast.getLeft().getType() != ast.getRight().getType()) {
                            throw new RuntimeException("Addition Unsuccessful, incompatible types");
                        }
                        ast.setType(ast.getLeft().getType());
                    } else {
                        throw new RuntimeException("Addition Unsuccessful, incompatible types");
                    }
                    break;
                case "-":
                case "*":
                case "/":
                    if (ast.getLeft().getType() == Environment.Type.INTEGER || ast.getLeft().getType() == Environment.Type.DECIMAL) {
                        if (ast.getLeft().getType() == ast.getRight().getType()) {
                            ast.setType(ast.getLeft().getType());
                            break;
                        }
                    }
                    throw new RuntimeException("Unsuccessful, not Integer or Decimal type");
                default:
                    break;
            }
        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        try {
            if (!ast.getReceiver().isPresent()) {
                ast.setVariable(scope.lookupVariable(ast.getName()));
            }
            else {
                Ast.Expr.Access access = (Ast.Expr.Access) ast.getReceiver().get();
                access.setVariable(scope.lookupVariable(access.getName()));
                try {
                    scope = scope.lookupVariable(access.getName()).getType().getScope();
                    ast.setVariable(scope.lookupVariable(ast.getName()));
                }
                finally {
                    scope = scope.getParent();
                }
            }
        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        try {
            if (!ast.getReceiver().isPresent()) {
                List<Environment.Type> params = scope.lookupFunction(ast.getName(), ast.getArguments().size()).getParameterTypes();

                int i = 0;
                while (i < ast.getArguments().size()) {
                    visit(ast.getArguments().get(i));
                    requireAssignable(params.get(i), ast.getArguments().get(i).getType());
                    i++;
                }

                ast.setFunction(scope.lookupFunction(ast.getName(), ast.getArguments().size()));
            }
            else {
                visit(ast.getReceiver().get());
                Ast.Expr.Access temp = (Ast.Expr.Access) ast.getReceiver().get();
                List<Environment.Type> params = scope.lookupVariable(temp.getName()).getType().getMethod(ast.getName(), ast.getArguments().size()).getParameterTypes();

                int i = 0;
                while (i < ast.getArguments().size()) {
                    visit(ast.getArguments().get(i));
                    requireAssignable(params.get(i + 1), ast.getArguments().get(i).getType());
                    i++;
                }

                ast.setFunction(scope.lookupVariable(temp.getName()).getType().getMethod(ast.getName(), ast.getArguments().size()));
            }

        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }

        return null;
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type) {
        try {
            if (target != type && target != Environment.Type.ANY && target != Environment.Type.COMPARABLE) {
                throw new RuntimeException("requireAssignable -- Types Do Not Match");
            }
        } catch (RuntimeException r) {
            throw new RuntimeException(r);
        }
    }
}
