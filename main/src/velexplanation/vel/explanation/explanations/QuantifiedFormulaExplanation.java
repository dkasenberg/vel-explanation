package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import rabinizer.formulas.Formula;
import velexplanation.misc.Pair;
import velexplanation.vel.VELObjective;
import velexplanation.vel.VELProductState;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class QuantifiedFormulaExplanation extends Explanation {

    public static QuantifiedFormulaExplanation construct(VELObjective velObjective,
                                                         int objectiveIndex,
                                                         List<VELProductState> productHistory,
                                                         Map<Pair<Integer, Integer>, Set<Set<Formula>>> inconsistentMinterms,
                                                         Episode episode,
                                                         Map<String, String> groundingSoFar,
                                                         Set<Map<String, String>> groundingsToGo,
                                                         int startIndex) throws Exception {
        if (startIndex == velObjective.quantifiedVariables.size()) {
//            TODO correct this to return the correct value.
            return GroundedFormulaViolatedExplanation.construct(velObjective, objectiveIndex, groundingSoFar, productHistory, inconsistentMinterms, episode);
        }
        if (groundingsToGo.isEmpty()) {
            throw new Exception("Can't produce explanations.");
        }

        if (velObjective.quantificationTypes.get(startIndex)) {
            return AFormulaViolatedExplanation.construct(velObjective,
                    objectiveIndex,
                    productHistory,
                    inconsistentMinterms,
                    episode,
                    groundingSoFar,
                    groundingsToGo,
                    startIndex);

// If universal quantification: partition on first variable.
//            Must have all possible values of first variable, and each partition must satisfy the sublist.
        } else {
            return EFormulaViolatedExplanation.construct(velObjective,
                    objectiveIndex,
                    productHistory,
                    inconsistentMinterms,
                    episode,
                    groundingSoFar,
                    groundingsToGo,
                    startIndex);
        }
    }
}
