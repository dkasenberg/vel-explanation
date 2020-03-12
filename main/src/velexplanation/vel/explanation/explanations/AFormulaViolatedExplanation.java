package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import rabinizer.formulas.Formula;
import velexplanation.misc.Pair;
import velexplanation.vel.VELObjective;
import velexplanation.vel.VELProductState;
import velexplanation.vel.explanation.ExplanationHelper;

import java.util.*;

public class AFormulaViolatedExplanation extends QuantifiedFormulaExplanation {
    private String variableName;
    private Pair<String, Explanation> counterExample;

    private AFormulaViolatedExplanation(String variableName, Pair<String, Explanation> counterExample) {
        this.variableName = variableName;
        this.counterExample = counterExample;
    }

    public static AFormulaViolatedExplanation construct(VELObjective objective,
                                                        int objectiveIndex,
                                                        List<VELProductState> productHistory,
                                                        Map<Pair<Integer, Integer>, Set<Set<Formula>>> inconsistentMinterms,
                                                        Episode episode,
                                                        Map<String, String> groundingSoFar,
                                                        Set<Map<String, String>> groundingsToGo,
                                                        int startIndex) throws Exception {
        String quantVar = objective.quantifiedVariables.get(startIndex);
        Map<Map<String, String>, Set<Map<String, String>>> partition = ExplanationHelper.groundingPartition(groundingsToGo, Collections.singleton(quantVar));
        for (Map<String, String> partitionKey : partition.keySet()) {
//                partitionKey should only have a single key and value.
            Map<String, String> groundingSoFarCopy = new HashMap<>(groundingSoFar);
            String varName = null;
            String objName = null;
            for (Map.Entry<String, String> e : partitionKey.entrySet()) {
                varName = e.getKey();
                objName = e.getValue();
                break;
            }
            groundingSoFarCopy.put(varName, objName);

            try {
                return new AFormulaViolatedExplanation(varName,
                        new Pair<>(objName, QuantifiedFormulaExplanation.construct(objective, objectiveIndex, productHistory, inconsistentMinterms,
                                episode, groundingSoFarCopy, partition.get(partitionKey), startIndex + 1))
                );
            } catch (Exception ignored) {
            }
        }

        throw new Exception("Can't produce explanations.");
    }

    public String counterexample() {
        return counterExample.getLeft();
    }

    @Override
    public String toStringWithIndentation(int numIndents) {
        StringBuilder result = new StringBuilder();
        result.append(nTabs(numIndents));
        result.append("For ");
        result.append(variableName);
        result.append("=");
        result.append(counterExample.getLeft());
        result.append(":\n");
        result.append(counterExample.getRight().toStringWithIndentation(numIndents + 1));
        return result.toString();
    }
}
