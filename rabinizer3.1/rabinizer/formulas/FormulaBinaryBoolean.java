/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.formulas;

import rabinizer.bdd.GSet;
import rabinizer.bdd.Valuation;

/**
 * @author jkretinsky
 */
public abstract class FormulaBinaryBoolean extends FormulaBinary {

    public FormulaBinaryBoolean(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    public Formula unfold() {
        return ThisTypeBinary(left.unfold(), right.unfold());
    }

    @Override
    public Formula unfoldNoG() {
        return ThisTypeBinary(left.unfoldNoG(), right.unfoldNoG());
    }

    @Override
    public Formula evaluateValuation(Valuation valuation) {
        return ThisTypeBinary(left.evaluateValuation(valuation), right.evaluateValuation(valuation));
    }

    @Override
    public Formula evaluateLiteral(Literal literal) {
        return ThisTypeBinary(left.evaluateLiteral(literal), right.evaluateLiteral(literal));
    }

    @Override
    public abstract Formula removeConstants();

    @Override
    public Formula removeX() {
        return ThisTypeBinary(left.removeX(), right.removeX());
    }

    @Override
    public Literal getAnUnguardedLiteral() {
        if (left.getAnUnguardedLiteral() != null) {
            return left.getAnUnguardedLiteral();
        } else {
            return right.getAnUnguardedLiteral();
        }
    }

    @Override
    public Formula substituteGsToFalse(GSet gSet) {
        return ThisTypeBinary(left.substituteGsToFalse(gSet), right.substituteGsToFalse(gSet));
    }

    @Override
    public boolean isVeryDifferentFrom(Formula f) {
        return left.isVeryDifferentFrom(f) || right.isVeryDifferentFrom(f);
    }

    @Override
    public abstract boolean ignoresG(Formula f);

}
