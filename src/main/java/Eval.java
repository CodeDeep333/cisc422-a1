import ast.*;

import java.util.*;

public class Eval extends BaseEval {
    //-----------------------!! DO NOT MODIFY !!-------------------------
    private int[][] M;
    public Eval(int[][] M) {
        this.M = M;
    }
    //-------------------------------------------------------------------

    @Override
    protected Integer evalNExp(NExp exp, Env env) {
        return 0;
    }

    @Override
    protected Set<Integer> evalSExp(SExp exp, Env env) {
        return null;
    }

    @Override
    protected Boolean evalFormula(Formula formula, Env env) {
        return false;
    }
}
