package rabinizer.formulas;

import net.sf.javabdd.BDD;

/**
 * Represents a until formula.
 *
 * @author Andreas & Ruslan
 */
public class UOperator extends FormulaBinary {

    public UOperator(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    public String operator() {
        return "U";
    }

    public Formula applyParam(String k, String v) {
        return new UOperator(left.applyParam(k, v), right.applyParam(k, v));
    }

    @Override
    public Formula ThisTypeBinary(Formula left, Formula right) {
        return new UOperator(left, right);
    }

    public Formula copy() {
        return new UOperator(left.copy(), right.copy());
    }

    public BDD bdd() {
        if (cachedBdd == null) {
            Formula booleanAtom = new UOperator(
                    left.representative(),
                    right.representative()
            );
            int bddVar = globals.bddForFormulae.bijectionBooleanAtomBddVar.id(booleanAtom);
            if (globals.bddForFormulae.bddFactory.varNum() <= bddVar) {
                globals.bddForFormulae.bddFactory.extVarNum(1);
            }
            cachedBdd = globals.bddForFormulae.bddFactory.ithVar(bddVar);
            globals.bddForFormulae.representativeOfBdd(cachedBdd, this);
        }
        return cachedBdd;
    }

    @Override
    public Formula unfold() {
        // unfold(a U b) = unfold(b) v (unfold(a) ^ X (a U b))
        return new Disjunction(right.unfold(), new Conjunction(left.unfold(), /*new XOperator*/ (this)));
    }

    @Override
    public Formula unfoldNoG() {
        // unfold(a U b) = unfold(b) v (unfold(a) ^ X (a U b))
        return new Disjunction(right.unfoldNoG(), new Conjunction(left.unfoldNoG(), /*new XOperator*/ (this)));
    }

    public Formula toNNF() {
        return new UOperator(left.toNNF(), right.toNNF());
    }

    public Formula negationToNNF() {
        return new Disjunction(new GOperator(right.negationToNNF()),
                new UOperator(right.negationToNNF(), new Conjunction(left.negationToNNF(), right.negationToNNF())));
    }

}
