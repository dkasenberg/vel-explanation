package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import rabinizer.formulas.Formula;
import velexplanation.misc.Pair;
import velexplanation.vel.VELObjective;
import velexplanation.vel.VELProductState;
import velexplanation.vel.explanation.ExplanationHelper;

import java.util.*;

public class EFormulaViolatedExplanation extends QuantifiedFormulaExplanation {
    private String variableName;
    private Map<String, Explanation> counterexamples;

    private EFormulaViolatedExplanation(String variableName, Map<String, Explanation> counterexamples) {
        this.counterexamples = counterexamples;
        this.variableName = variableName;
    }

    public static EFormulaViolatedExplanation construct(VELObjective objective,
                                                        int objectiveIndex,
                                                        List<VELProductState> productHistory,
                                                        Map<Pair<Integer, Integer>, Set<Set<Formula>>> inconsistentMinterms,
                                                        Episode episode,
                                                        Map<String, String> groundingSoFar,
                                                        Set<Map<String, String>> groundingsToGo,
                                                        int startIndex) throws Exception {
        String quantVar = objective.quantifiedVariables.get(startIndex);
        Map<Map<String, String>, Set<Map<String, String>>> partition = ExplanationHelper.groundingPartition(groundingsToGo, Collections.singleton(quantVar));
        State s = productHistory.get(productHistory.size() - 1).s;
        //        If existential quantification: partition on first variable.
// If the partition is non-empty and contains one set that satisfies the sublist, return true.
        if (partition.keySet().size() != ((OOState) s).objectsOfClass(objective.freeVarClassMap.get(quantVar)).size()) {
            throw new Exception("Can't produce explanations.");
        }
        Map<String, Explanation> counterexamples = new HashMap<>();
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

            counterexamples.put(objName, QuantifiedFormulaExplanation.construct(objective, objectiveIndex, productHistory, inconsistentMinterms,
                    episode, groundingSoFarCopy, partition.get(partitionKey), startIndex + 1));

        }
        return new EFormulaViolatedExplanation(quantVar, counterexamples);
    }

    @Override
    public String toStringWithIndentation(int numIndents) {
        StringBuilder result = new StringBuilder();
        result.append(nTabs(numIndents));
        result.append(variableName);
        result.append(" could be bound to the following objects:");
        for (String objVar : counterexamples.keySet()) {
            result.append(" ");
            result.append(objVar);
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        result.append(".\n");
        for (String objVar : counterexamples.keySet()) {
            result.append(nTabs(numIndents));
            result.append("For ");
            result.append(variableName);
            result.append("=");
            result.append(objVar);
            result.append(":\n");
            result.append(counterexamples.get(objVar).toStringWithIndentation(numIndents + 1));
            result.append("\n");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
}
