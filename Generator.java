package plc.project;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        // this one given, do not change
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        // this one given, do not change
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    // added this helper function to get the data type
    public static String getJavaType(String type) {
        return Environment.getType(type).getJvmName();
    }

    @Override
    public Void visit(Ast.Source ast) {
        print("public class Main {");
        indent++;
        newline(0);

        if (!ast.getFields().isEmpty()) {
            int i = 0;
            while (i < ast.getFields().size()) {
                newline(indent);
                print(ast.getFields().get(i));
                i++;
            }
            // not required newline here it causes problem for single field
            // newline(0);
        }

        // main method
        newline(indent);
        print("public static void main(String[] args) {");
        indent++;
        newline(indent);
        print("System.exit(new Main().main());");
        indent--;
        newline(indent);
        print("}");
        newline(0);

        int i = 0;
        int numOfMethod = ast.getMethods().size();
        while (i < ast.getMethods().size()) {
            newline(indent);
            print(ast.getMethods().get(i));
            i++;

            // added for multiple methods
            if (numOfMethod > 1 && i != numOfMethod)
                newline(0);
        }

        newline(0);

        indent--;
        newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        // Java typename

        /*switch (ast.getTypeName()) {
            case "Integer":
                print("int");
                break;
            case "Decimal":
                print("double");
                break;
            case "Boolean":
                print("boolean");
                break;
            case "Character":
                print("char");
                break;
            case "String":
                print("String");
                break;
        }

         */

        // print data type
        print(getJavaType(ast.getTypeName()));

        print(" ");
        print(ast.getName());
        if (ast.getValue().isPresent()) {
            print(" = ");
            print(ast.getValue().get());
        }
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        if (ast.getReturnTypeName().isPresent()) {
            print(getJavaType(ast.getReturnTypeName().get()));
        }

        print(" ");
        print(ast.getName());
        print("(");

        int i = 0;
        while (i < ast.getParameters().size()) {
            // added to get the right data type
            print(getJavaType(ast.getParameterTypeNames().get(i)), " ", ast.getParameters().get(i));
            if (i != ast.getParameters().size() - 1)
                print(", ");
            i++;
        }
        print(") {");

        if (!ast.getStatements().isEmpty()) {
            indent++;
            for (i = 0; i < ast.getStatements().size(); i++) {
                newline(indent);
                print(ast.getStatements().get(i));
            }

            indent--;
            newline(indent);
        }

        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        print(ast.getExpression());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        print(ast.getVariable().getType().getJvmName());
        print(" ");
        print(ast.getVariable().getJvmName());
        if (ast.getValue().isPresent()) {
            print(" = ");
            print(ast.getValue().get());
        }
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        print(ast.getReceiver());
        print(" = ");
        print(ast.getValue());
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        print("if (");
        print(ast.getCondition());
        print(") {");
        indent++;

        int i = 0;
        while (i < ast.getThenStatements().size()) {
            newline(indent);
            print(ast.getThenStatements().get(i));
            i++;
        }

        indent--;
        newline(indent);
        print("}");

        if (!ast.getElseStatements().isEmpty()) {
            print(" else {");
            indent++;
            for (i = 0; i < ast.getElseStatements().size(); i++) {
                newline(indent);
                print(ast.getElseStatements().get(i));
            }

            indent--;
            newline(indent);
            print("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        print("for (");
        print("int ");
        print(ast.getName());
        print(" : ");
        print(ast.getValue());
        print(") {");
        indent++;

        int i = 0;
        while (i < ast.getStatements().size()) {
            newline(indent);
            print(ast.getStatements().get(i));
            i++;
        }

        indent--;
        // added to check if for statement is empty
        if (!ast.getStatements().isEmpty())
            newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        print("while (");
        print(ast.getCondition());
        print(") {");

        if (!ast.getStatements().isEmpty()) {
            indent++;
            for (int i = 0; i < ast.getStatements().size(); i++) {
                newline(indent + 1);
                print(ast.getStatements().get(i));
            }
            indent--;
            newline(indent);
            print("}");
        } else {
            print("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        print("return ");
        print(ast.getValue());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        if (ast.getType() == Environment.Type.INTEGER) {
            BigInteger temp = (BigInteger) ast.getLiteral();
            print(temp.intValue());
        }
        else if (ast.getType() == Environment.Type.DECIMAL) {
            BigDecimal temp = (BigDecimal) ast.getLiteral();
            print(temp.doubleValue());
        }
        else if (ast.getType() == Environment.Type.CHARACTER) {
            print("'");
            print(ast.getLiteral());
            print("'");
        }
        else if (ast.getType() == Environment.Type.STRING) {
            print("\"");
            print(ast.getLiteral());
            print("\"");
        }
        else {
            print(ast.getLiteral());
        }
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        print("(");
        print(ast.getExpression());
        print(")");
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        // Left
        print(ast.getLeft());
        print(" ");
        // Operator
        switch (ast.getOperator()) {
            case "OR":
                print("||");
                break;
            case "AND":
                print("&&");
                break;
            default:
                print(ast.getOperator());
                break;
        }
        print(" ");
        // Right
        print(ast.getRight());
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        if (ast.getReceiver().isPresent()) {
            print(ast.getReceiver().get());
            print(".");
        }
        print(ast.getVariable().getJvmName());
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        if (ast.getReceiver().isPresent()) {
            print(ast.getReceiver().get());
            print(".");
        }

        print(ast.getFunction().getJvmName());
        print("(");

        if (!ast.getArguments().isEmpty()) {
            for (int i = 0; i < ast.getArguments().size(); i++) {
                print(ast.getArguments().get(i));
                if (i != ast.getArguments().size() - 1) {
                    print(", ");
                }
            }
        }

        print(")");
        return null;
    }

}
