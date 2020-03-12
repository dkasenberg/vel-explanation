/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velexplanation.formula;

import net.sf.javabdd.BDD;
import org.apache.commons.lang3.StringUtils;
import rabinizer.bdd.Globals;
import rabinizer.formulas.Formula;
import rabinizer.formulas.FormulaNullary;
import rabinizer.formulas.Literal;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dkasenberg
 */
public class ParamPlaceholder extends FormulaNullary {

    public String propName;
    public List<String> params;
    public Map<String, String> applied;

    public ParamPlaceholder(String propName, List<String> params, Globals globals) {
        super(globals);
        this.propName = propName;
        this.params = params;
        this.applied = new HashMap<>();
    }

    public Formula copy() {
        ParamPlaceholder theCopy = new ParamPlaceholder(propName, new ArrayList<>(params), globals);
        theCopy.applied = new HashMap<>(applied);
        return theCopy;
    }

    public Formula applyParam(String param, String value) {

        ParamPlaceholder theCopy = (ParamPlaceholder) this.copy();
        theCopy.applied.put(param, value);
        // Check if all variables have been bound
        if (theCopy.applied.keySet().containsAll(params)) {
            return theCopy.applyAllParams();
        }

        return theCopy;
    }

    protected Formula applyAllParams() {
        // Order must be retained here!
        List<String> actualsAsArray = params.stream().map(param -> applied.get(param)).collect(Collectors.toList());
        String atom = propName + ":" + StringUtils.join(actualsAsArray, ",");
        int id = globals.bddForVariables.bijectionIdAtom.id(atom);
        return new Literal(atom, id, false, globals);
    }

    @Override
    public String operator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BDD bdd() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private int appliedBitmask() {
        int bitmask = 0;
        for (int i = 0; i < params.size(); i++) {
            bitmask |= (applied.containsKey(params.get(i)) ? 1 : 0 << i);
        }
        return bitmask;
    }

    @Override
    public int hashCode() {
        int strHash = (propName + ":" + applied.values()).hashCode();
        int sizehash = params.size();
        int bitmask = appliedBitmask();
        return strHash * 1369 + bitmask * 37 + sizehash;
    }

    //    Equality in ParamPlaceholder ensures that the prop is the same and that any applied params are the same, but
//    allows the unapplied params to have different names.
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParamPlaceholder)) return false;
        ParamPlaceholder pp = (ParamPlaceholder) o;

        if (!propName.equals((pp.propName))) return false;
        if (params.size() != pp.params.size()) return false;
        for (int i = 0; i < params.size(); i++) {
            boolean containsKey = applied.containsKey(params.get(i));
            boolean otherContainsKey = pp.applied.containsKey(pp.params.get(i));
            if (containsKey != otherContainsKey) return false;
            if (containsKey && otherContainsKey && !applied.get(params.get(i)).equals(pp.applied.get(pp.params.get(i))))
                return false;
        }
        return true;

    }

    @Override
    public String toString() {
        return propName + ":" + StringUtils.join(params, ",");
    }

    @Override
    public String toReversePolishString() {
        return propName + ":" + StringUtils.join(params, ",");
    }

    @Override
    public Formula toNNF() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Formula negationToNNF() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsG() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasSubformula(Formula f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Formula> gSubformulas() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Formula> topmostGs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Formula unfold() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Formula unfoldNoG() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
