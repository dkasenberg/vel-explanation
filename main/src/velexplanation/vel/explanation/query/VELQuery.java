package velexplanation.vel.explanation.query;

import burlap.mdp.singleagent.oo.OOSADomain;
import rabinizer.formulas.Formula;
import rabinizer.formulas.Negation;
import velexplanation.vel.VELObjective;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class VELQuery {

    public VELObjective positiveObjective;
    public VELObjective negativeObjective;
    public boolean isWhyQuery;


    public VELQuery(OOSADomain d, Formula f, List<String> quantifiedVariables, List<Boolean> quantificationTypes,
                    boolean whyQuery) {
        this.isWhyQuery = whyQuery;
        this.positiveObjective = new VELObjective(d, f, new HashSet<>(), quantifiedVariables, quantificationTypes, false);

        List<Boolean> negatedQuantificationTypes = new ArrayList<>();

        for (boolean quantificationType : quantificationTypes) {
            negatedQuantificationTypes.add(!quantificationType);
        }

        this.negativeObjective = new VELObjective(d, new Negation(f), new HashSet<>(),
                quantifiedVariables, negatedQuantificationTypes, false);
    }

    public Map<String, String> freeVarClasses() {
        return positiveObjective.freeVarClassMap;
    }

    @Override
    public String toString() {
        return (isWhyQuery ? "Why " : "") + positiveObjective.toString() + "?";
    }
}
