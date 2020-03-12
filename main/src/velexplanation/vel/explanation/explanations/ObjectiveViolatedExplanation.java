package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import rabinizer.formulas.Formula;
import velexplanation.misc.Pair;
import velexplanation.vel.VELObjective;
import velexplanation.vel.VELProductState;
import velexplanation.vel.explanation.ExplanationHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectiveViolatedExplanation extends Explanation {
    public VELObjective objective;
    private Map<Map<String, String>, Explanation> violationExplanations;

    private ObjectiveViolatedExplanation(Map<Map<String, String>, Explanation> violationExplanations, VELObjective objective) {
        this.objective = objective;
        this.violationExplanations = violationExplanations;
    }

    public static ObjectiveViolatedExplanation construct(VELObjective objective,
                                                         int objectiveIndex,
                                                         List<VELProductState> productHistory,
                                                         Map<Pair<Integer, Integer>, Set<Set<Formula>>> inconsistentMinterms,
                                                         Episode episode) {

        VELProductState finalState = productHistory.get(productHistory.size() - 1);

        Set<Map<String, String>> violatingGroundings;
        if (objective.cosafe) {
            violatingGroundings = objective.getNonAcceptingGroundings(finalState.objectiveStates.get(objectiveIndex));
        } else {
            violatingGroundings = objective.getAcceptingGroundings(finalState.objectiveStates.get(objectiveIndex));
        }

        Map<Map<String, String>, Set<Map<String, String>>> partition = ExplanationHelper.groundingPartition(violatingGroundings, objective.costlyVariables);
        Map<Map<String, String>, Explanation> violationCritiques = new HashMap<>();
        for (Map<String, String> costlyGrounding : partition.keySet()) {
            try {
                violationCritiques.put(costlyGrounding, QuantifiedFormulaExplanation.construct(objective,
                        objectiveIndex, productHistory, inconsistentMinterms, episode, costlyGrounding,
                        partition.get(costlyGrounding), 0));
            } catch (Exception ignored) {
            }
        }

        return new ObjectiveViolatedExplanation(violationCritiques, objective);
    }

    public Set<Map<String, String>> getViolations() {
        return violationExplanations.keySet();
    }

    public ObjectiveViolatedExplanation cutToNViolations(int n) {
        if (n > violationExplanations.size()) return this;
        Map<Map<String, String>, Explanation> cutViolationExplanations = violationExplanations.entrySet().stream()
                .limit(n)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ObjectiveViolatedExplanation(cutViolationExplanations, objective);
    }

    @Override
    public String toStringWithIndentation(int numIndents) {
        StringBuilder result = new StringBuilder();
        int numViolations = violationExplanations.size();
        result.append(nTabs(numIndents));
        result.append("The objective '");
        result.append(objective.toString());
        result.append("' was violated ");
        result.append(numViolations);
        result.append(numViolations != 1 ? " times.\n" : " time.\n");
        for (Map<String, String> costlyVarGrounding : violationExplanations.keySet()) {
            result.append(nTabs(numIndents + 1));
            result.append("For");
            for (String varName : costlyVarGrounding.keySet()) {
                result.append(" ");
                result.append(varName);
                result.append("=");
                result.append(costlyVarGrounding.get(varName));
                result.append(",");
            }
            result.deleteCharAt(result.length() - 1);
            result.append(":\n");
            result.append(violationExplanations.get(costlyVarGrounding).toStringWithIndentation(numIndents + 2));
        }

        return result.toString();
    }

    public double getViolationCost() {
        return (double) violationExplanations.size();
    }
}