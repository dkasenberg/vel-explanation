package velexplanation.vel;

import rabinizer.formulas.*;
import velexplanation.formula.ParamPlaceholder;

public class VELCosafeToSafeConverter {

    public static Formula cosafeToNotSafe(Formula cosafe) throws FormulaConversionException {
        return new Negation(propagateNegationInward(cosafe, true));
    }

    public static Formula propagateNegationInward(Formula f, boolean negateClause) throws FormulaConversionException {
        if ((f instanceof FOperator || f instanceof UOperator) && !negateClause) {
            throw new FormulaConversionException();
        }
        if ((f instanceof GOperator || f instanceof VOperator) && negateClause) {
            throw new FormulaConversionException();
        }

        if (f instanceof FOperator) {
//            negateClause is true
            return new GOperator(propagateNegationInward(((FOperator) f).operand, true));
        } else if (f instanceof UOperator) {
//            negateClause is true - convert to vOperator
            return new VOperator(propagateNegationInward(((UOperator) f).left, true),
                    propagateNegationInward(((UOperator) f).right, true));
        } else if (f instanceof GOperator) {
//            negateClause is false
            return new GOperator(propagateNegationInward(((GOperator) f).operand, false));
        } else if (f instanceof VOperator) {
            return new VOperator(propagateNegationInward(((VOperator) f).left, false),
                    propagateNegationInward(((VOperator) f).right, false));
        } else if (f instanceof XOperator) {
            return new XOperator(propagateNegationInward(((XOperator) f).operand, negateClause));
        } else if (f instanceof Conjunction && negateClause) {
            return new Disjunction(propagateNegationInward(((Conjunction) f).left, true),
                    propagateNegationInward(((Conjunction) f).right, true));
        } else if (f instanceof Conjunction) {
            return new Conjunction(propagateNegationInward(((Conjunction) f).left, false),
                    propagateNegationInward(((Conjunction) f).right, false));
        } else if (f instanceof Disjunction && negateClause) {
            return new Conjunction(propagateNegationInward(((Disjunction) f).left, true),
                    propagateNegationInward(((Disjunction) f).right, true));
        } else if (f instanceof Disjunction) {
            return new Disjunction(propagateNegationInward(((Disjunction) f).left, false),
                    propagateNegationInward(((Disjunction) f).right, false));
        } else if (f instanceof Implication && negateClause) {
            return new Conjunction(propagateNegationInward(((Implication) f).left, false),
                    propagateNegationInward(((Implication) f).right, true));
        } else if (f instanceof Implication) {
            return new Implication(propagateNegationInward(((Implication) f).left, false),
                    propagateNegationInward(((Implication) f).right, false));
        } else if (f instanceof Negation) {
            return propagateNegationInward(((Negation) f).operand, !negateClause);
        } else if (f instanceof BooleanConstant) {
            return new BooleanConstant(((BooleanConstant) f).value ^ negateClause, f.globals);
        } else if (f instanceof Literal || f instanceof ParamPlaceholder) {
            return negateClause ? new Negation(f) : f;
        }
        throw new RuntimeException("Formula type not found");
    }

    public static class FormulaConversionException extends Exception {
    }
}
