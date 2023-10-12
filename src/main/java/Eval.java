/*
Name: Khushroo Kerman
Student ID: 20222653
Net ID: 19kkk4
Breathing is hard
 */

// Imports
import ast.*;

import java.util.*;

// Initialize Class
public class Eval extends BaseEval {
    //-----------------------!! DO NOT MODIFY !!-------------------------
    private int[][] M;
    public Eval(int[][] M) {
        this.M = M;
    }
    //-------------------------------------------------------------------

    // The 3 methods for the class
    // Method to Evaluate Numerical Expressions
    // Takes in a Numerical Expression and an Environment as parameters
    @Override
    protected Integer evalNExp(NExp exp, Env env) {
        // Returns the numerical value of the expression
        if (exp instanceof Nat){
            return ((Nat) exp).value;
        // Calculates the total number of sales and returns it
        } else if (exp instanceof  SalesForM) {
            int sum = 0;
            for (int i = 0; i < M.length; i++) {
                for (int j = 0; j < M[0].length; j++) {
                    sum += M[i][j];
                }
            }
            return sum;
        // Calculates the total sales of a day/column
        } else if (exp instanceof SalesForD) {
            // Subtract 1 to change from natural language
            int day = evalNExp(((SalesForD) exp).day, env) - 1;
            // Make sure the day is in the set
            if (day > M.length) {
                throw new SalesArrayIndexOutOfBoundsException();
            }
            int sum = 0;
            for (int i = 0; i < M.length; i++) {
                sum += M[i][day];
            }
            return sum;
        // Calculates the total sales of a product
        } else if (exp instanceof SalesForP) {
            // Subtract 1 from the product to switch from natural language
            int pro = evalNExp(((SalesForP) exp).product, env) - 1;
            // Make sure that the product is in the set
            if (pro > M[0].length) {
                throw new SalesArrayIndexOutOfBoundsException();
            }
            int sum = 0;
            for (int i = 0; i < M.length; i++) {
                sum += M[pro][i];
            }
            return sum;
        // Find the sales of a product on a certain day
        } else if (exp instanceof SalesAt) {
            int pro = evalNExp(((SalesAt) exp).product, env);
            int day = evalNExp(((SalesAt) exp).day, env);
            // Subtract the product and day by 1 to change from natural language
            return M[pro - 1][day - 1];
        // Check how many of a type there are
        } else if (exp instanceof Size) {
            Set<Integer> set = evalSExp(((Size) exp).sExp, env);
            return set.size();
        // Do arithmetic between sides of a formula
        } else if (exp instanceof BinaryNExp) {
            switch (((BinaryNExp) exp).op.kind) {
                case ADD: return evalNExp(((BinaryNExp) exp).lhs, env) + evalNExp(((BinaryNExp) exp).rhs, env);
                case DIFF: return evalNExp(((BinaryNExp) exp).lhs, env) - evalNExp(((BinaryNExp) exp).rhs, env);
                case MULT: return evalNExp(((BinaryNExp) exp).lhs, env) * evalNExp(((BinaryNExp) exp).rhs, env);
                // Throw an exception if it tries to divide by 0
                case DIV:
                    try {
                        return evalNExp(((BinaryNExp) exp).lhs, env) / evalNExp(((BinaryNExp) exp).rhs, env);
                    } catch (Exception e) {
                        throw new DivisionByZeroException();
                    }

            }
        // If the expression is a variable, assign it's value using it's name and the enviroment
        // I tried to pop the stack after the variable was taken but it caused too many errors and
        // even if I put it in other places it causes many issues which I was not able to solve
        } else if (exp instanceof Var) {
            int val = env.lookup(((Var) exp).name);
            return val;
        // Throw an exception if it does not fit any of the categories
        } else {
            throw new BaseEval.UnboundVariableException();
        }
        return 0;
    }

    // Method to evaluate set expressions
    // Takes in a set expression and the environment as parameters
    @Override
    protected Set<Integer> evalSExp(SExp exp, Env env) {
        // Checks what type the set expression is and add all of the type to a set
        if (exp instanceof Type) {
            Set<Integer> set = new HashSet<>();
            switch (((Type) exp).kind) {
                case DAY:
                    for (int i = 1; i <= M[0].length; i++) {
                        set.add(i);
                    }
                    break;
                case PRODUCT:
                    for (int i = 1; i <= M.length; i++) {
                        set.add(i);
                    }
                    break;
                case SALE:
                    for (int i = 0; i < M.length; i++) {
                        for (int j = 0; j < M[0].length; j++) {
                            set.add(M[i][j]);
                        }
                    }
                    break;
            }
            return set;
        // Collect all of a type into a set then narrow it down to only those
        // that fit according to the formula
        } else if (exp instanceof SetCompr) {
            Set<Integer> set = evalSExp(((SetCompr) exp).type, env);
            Set<Integer> set2 = new HashSet<>();
            for (int i: set) {
                // Add the variable value to the environment using it's name
                env.push(((SetCompr) exp).var.name, i);
                // Add the value to the set if it fits the formula
                if (evalFormula(((SetCompr) exp).formula, env)) {
                    set2.add(i);
                }

            }
            return set2;

        // Take two sets and an operation then return a new set depending on what
        // operation was used
        } else if (exp instanceof BinarySExp) {
            Set<Integer> set1 = evalSExp(((BinarySExp) exp).lhs, env);
            Set<Integer> set2 = evalSExp(((BinarySExp) exp).rhs, env);
            switch (((BinarySExp) exp).op.kind) {
                case UNION:
                    set1.addAll(set2);
                    return set1;
                case INTER:
                    set1.retainAll(set2);
                    return set1;
                case DIFF:
                    set1.removeAll(set2);
                    return set1;
            }
        // Throw an exception if the expression does not fit any of these categories
        } else {
            throw new BaseEval.UnboundVariableException();
        }
        return null;
    }

    // Method to evaluate formulas
    // Takes in a formula and the environment as parameters
    @Override
    protected Boolean evalFormula(Formula formula, Env env) {
        // Takes a formula and it's operations, then compares the two numerical values
        // to figure out if the formula is true or not
        if (formula instanceof AtomicN) {
            return switch (((AtomicN) formula).relNOp.kind) {
                case EQ -> evalNExp(((AtomicN) formula).lhs, env).equals(evalNExp(((AtomicN) formula).rhs, env));
                case GT -> evalNExp(((AtomicN) formula).lhs, env) > evalNExp(((AtomicN) formula).rhs, env);
                case LT -> evalNExp(((AtomicN) formula).lhs, env) < evalNExp(((AtomicN) formula).rhs, env);
                case GTE -> evalNExp(((AtomicN) formula).lhs, env) >= evalNExp(((AtomicN) formula).rhs, env);
                case LTE -> evalNExp(((AtomicN) formula).lhs, env) <= evalNExp(((AtomicN) formula).rhs, env);
                case NEQ -> !evalNExp(((AtomicN) formula).lhs, env).equals(evalNExp(((AtomicN) formula).rhs, env));
            };
        // Checks to see if two set expressions are equal
        } else if (formula instanceof AtomicS) {
            if (((AtomicS) formula).relSOp.kind == RelSOp.Kind.EQ) {
                return evalSExp(((AtomicS) formula).lhs, env).equals(evalSExp(((AtomicS) formula).rhs, env));
            }
        // Takes in a formula and a negation to return the opposite result of the formula
        } else if (formula instanceof Unary) {
            if (((Unary) formula).unConn.kind == UnaryConn.Kind.NOT){
                return !evalFormula(((Unary) formula).formula, env);
            }
        // Compares two formula together and returns a boolean value depending on the operation and the formula
        } else if (formula instanceof Binary) {
            switch (((Binary) formula).binConn.kind) {
                case AND:
                    return evalFormula(((Binary) formula).lhs, env) && evalFormula(((Binary) formula).rhs, env);
                case OR:
                    return evalFormula(((Binary) formula).lhs, env) || evalFormula(((Binary) formula).rhs, env);
                case EQUIV:
                    return evalFormula(((Binary) formula).lhs, env) == evalFormula(((Binary) formula).rhs, env);
                case IMPLY:
                    if (!evalFormula(((Binary) formula).lhs, env)) {
                        return true;
                    } else if (evalFormula(((Binary) formula).rhs, env)) {
                        return true;
                    } else {
                        return false;
                    }
            }
        // Takes a formula and tests the values of the variables to check if the formula
        // is true for all or at least one of the variables
        } else if (formula instanceof Quantified) {
            Set<Integer> set = evalSExp(((Quantified) formula).type, env);
            switch (((Quantified) formula).quantifier.kind) {
                // Checks if the formula holds true at least once with the given variables
                case EXISTS:
                    for (int i: set) {
                        env.push(((Quantified) formula).var.name, i);
                        if (evalFormula(((Quantified) formula).formula, env)) {
                            return true;
                        }

                    }
                    return false;
                // Checks if the formula holds for all of the variables given
                case FORALL:
                    for (int i: set) {
                        env.push(((Quantified) formula).var.name, i);
                        if (!evalFormula(((Quantified) formula).formula, env)) {
                            return false;
                        }

                    }
                    return true;
            }
        // Throw an exception if it doesn't match any of the categories
        } else {
            throw new BaseEval.UnboundVariableException();
        }
        return false;
    }
}
