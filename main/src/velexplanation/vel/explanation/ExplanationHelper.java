package velexplanation.vel.explanation;

import org.apache.commons.math3.linear.RealVector;
import rabinizer.formulas.Formula;
import rabinizer.formulas.Negation;
import velexplanation.misc.Pair;
import velexplanation.vel.VELHelper;
import velexplanation.vel.VELObjective;

import java.util.*;

public class ExplanationHelper {

    public static Map<Pair<Integer, Integer>, Set<Set<Formula>>> getInconsistentMinterms(VELObjective objective) {
        Map<Pair<Integer, Integer>, Set<Set<Formula>>> inconsistentMinterms = new HashMap<>();


        for (int i = 0; i < objective.totalStates; i++) {
            for (Pair<Formula, Integer> pair : objective.transitions.get(i)) {
                Formula f = pair.getLeft();
                int j = pair.getRight();
//                    Get the minterms of (neg)(qi->qj).
                Set<Set<Formula>> mintermFormulas = VELHelper.getMintermFormulas(new Negation(f));
                inconsistentMinterms.put(new Pair<>(i, j), mintermFormulas);
            }
        }
        return inconsistentMinterms;
    }

    public static Map<Map<String, String>, Set<Map<String, String>>> groundingPartition(Set<Map<String, String>> allGroundings, Set<String> partitionVars) {

        Map<Map<String, String>, Set<Map<String, String>>> partition = new HashMap<>();
        for (Map<String, String> grounding : allGroundings) {
            Map<String, String> partitionValue = new HashMap<>(grounding);
            Map<String, String> partitionKey = new HashMap<>();
            for (String var : partitionVars) {
                partitionKey.put(var, grounding.get(var));
                partitionValue.remove(var);
            }
            Set<Map<String, String>> partitionValues =
                    partition.containsKey(partitionKey) ? partition.get(partitionKey) : new HashSet<>();
            partitionValues.add(partitionValue);
            partition.put(partitionKey, partitionValues);
        }
        return partition;
    }

    public static List<Integer> getNonZeroEntries(RealVector weightVec) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < weightVec.getDimension(); i++) {
            if (weightVec.getEntry(i) != 0) {
                result.add(i);
            }
        }

        result.sort(Comparator.comparingDouble(weightVec::getEntry).reversed());
        return result;
    }
}
