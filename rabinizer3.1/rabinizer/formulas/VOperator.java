package rabinizer.formulas;

import net.sf.javabdd.BDD;

/**
 * Represents a until formula.
 *
 * @author Andreas & Ruslan
 */
public class VOperator extends FormulaBinary {

    public VOperator(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    public String operator() {
        return "V";
    }

    public Formula applyParam(String k, String v) {
        return new VOperator(left.applyParam(k, v), right.applyParam(k, v));
    }

    @Override
    public Formula ThisTypeBinary(Formula left, Formula right) {
        return new VOperator(left, right);
    }

    public Formula copy() {
        return new VOperator(left.copy(), right.copy());
    }

    public BDD bdd() {
        if (cachedBdd == null) {
            Formula booleanAtom = new VOperator(
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
        // unfold(a V b) = X (a V b)

        // unfold(a U b) = unfold(b) v (unfold(a) ^ X (a U b))
        return new Conjunction(right.unfold(), new Disjunction(left.unfold(), /*new XOperator*/ (this)));
    }

    @Override
    public Formula unfoldNoG() {
        // unfold(a U b) = unfold(b) v (unfold(a) ^ X (a U b))
        return new Conjunction(right.unfoldNoG(), new Disjunction(left.unfoldNoG(), /*new XOperator*/ (this)));
    }

    public Formula toNNF() {
        return new VOperator(left.toNNF(), right.toNNF());
    }

    public Formula negationToNNF() {
        return new Disjunction(new GOperator(right.negationToNNF()),
                new VOperator(right.negationToNNF(), new Conjunction(left.negationToNNF(), right.negationToNNF())));
    }

}
