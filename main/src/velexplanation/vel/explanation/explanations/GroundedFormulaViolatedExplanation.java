package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import rabinizer.formulas.Formula;
import velexplanation.misc.Pair;
import velexplanation.vel.VELHelper;
import velexplanation.vel.VELObjective;
import velexplanation.vel.VELProductState;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GroundedFormulaViolatedExplanation extends QuantifiedFormulaExplanation {

    private List<Set<Formula>> timestepPropositionMap;
    private Formula violatedFormula;

    private GroundedFormulaViolatedExplanation(List<Set<Formula>> timestepPropositionMap, Formula violatedFormula) {
        this.timestepPropositionMap = timestepPropositionMap;
        this.violatedFormula = violatedFormula;
    }

    public static GroundedFormulaViolatedExplanation construct(VELObjective objective,
                                                               int objectiveIndex,
                                                               Map<String, String> grounding,
                                                               List<VELProductState> productHistory,
                                                               Map<Pair<Integer, Integer>, Set<Set<Formula>>> inconsistentMinterms,
                                                               Episode episode
    ) {
        List<Integer> violationPath = new ArrayList<>();
        for (VELProductState s : productHistory) {
            int objectiveState = s.objectiveStates.get(objectiveIndex).groundingsToFSM.get(grounding);
            violationPath.add(objectiveState);
            if (objective.accStates.contains(objectiveState)) break;

        }

        List<Map<Pair<Integer, Integer>, Set<Set<Formula>>>> inconsistentSets = new ArrayList<>();

//                Forward pass: determine the sets of all possible inconsistencies for each time step

        for (int t = 0; t < violationPath.size() - 1; t++) {
            final int t1 = t;
            Map<Pair<Integer, Integer>, Set<Set<Formula>>> incSets = new HashMap<>();
            for (Pair<Integer, Integer> transition : inconsistentMinterms.keySet()) {
                int i = transition.getLeft();
                int j = transition.getRight();
                Set<Set<Formula>> minterms = inconsistentMinterms.get(transition);


                incSets.put(new Pair<>(i, j),
                        minterms.stream().map(minterm -> minterm.stream()
                                .map(f -> f.applyGrounding(grounding)).collect(Collectors.toSet()))
                                .filter(minterm -> minterm.stream().allMatch(f
                                        -> objective.evaluatesToTrue(f,
                                        episode.stateSequence.get(t1)))).collect(Collectors.toSet()));

            }
            inconsistentSets.add(incSets);
        }

        Map<Set<Integer>, List<Set<Formula>>> bestCritiqueMap;

        Set<Integer> allStates = IntStream.range(0, objective.totalStates).boxed().collect(Collectors.toSet());
        if (objective.cosafe) {
            Set<Integer> endStates = new HashSet<>(allStates);
            endStates.removeAll(objective.accStates);
            bestCritiqueMap = Collections.singletonMap(endStates, Collections.EMPTY_LIST);
        } else {
            bestCritiqueMap = Collections.singletonMap(objective.accStates, Collections.EMPTY_LIST);
        }


        for (int t = violationPath.size() - 2; t >= 0; t--) {
            Map<Set<Integer>, List<Set<Formula>>> newBestCritiqueMap = new HashMap<>();
            for (Set<Integer> curSet : bestCritiqueMap.keySet()) {
                Map<Pair<Integer, Integer>, Set<Set<Formula>>> incSets = inconsistentSets.get(t);
                Set<Formula> allProps = incSets.values().stream().flatMap(inc -> inc.stream()
                        .flatMap(Collection::stream)).collect(Collectors.toSet());

                Set<Set<Formula>> allCritiques = VELHelper.powerSet(allProps);


                for (Set<Formula> critique : allCritiques) {
                    Set<Integer> badSet = incSets.entrySet().stream().filter(e -> !curSet.contains(e.getKey().getRight()))
                            .filter(e -> e.getValue().stream().noneMatch(critique::containsAll))
                            .map(e -> e.getKey().getLeft()).collect(Collectors.toSet());
                    Set<Integer> newSet = new HashSet<>(allStates);
                    newSet.removeAll(badSet);
                    if (!newBestCritiqueMap.containsKey(newSet) || critiqueSize(newBestCritiqueMap.get(newSet)) >
                            critique.size() + critiqueSize(bestCritiqueMap.get(curSet))) {
                        List<Set<Formula>> fullCritique = new ArrayList<>(bestCritiqueMap.get(curSet));
                        fullCritique.add(0, critique);
                        newBestCritiqueMap.put(newSet, fullCritique);
                    }
                }
            }
            bestCritiqueMap = newBestCritiqueMap;
        }
        List<Set<Formula>> bestCritique = bestCritiqueMap.entrySet().stream().filter(entry -> entry.getKey()
                .contains(0)).min(Comparator.comparingInt(entry -> critiqueSize(entry.getValue()))).get().getValue();
        return new GroundedFormulaViolatedExplanation(bestCritique, objective.formula.applyGrounding(grounding));
    }

    private static int critiqueSize(List<Set<Formula>> critique) {
        return critique.stream().collect(Collectors.summingInt(s -> s.size()));
    }

    @Override
    public String toStringWithIndentation(int numIndents) {
        StringBuilder result = new StringBuilder();
        result.append(nTabs(numIndents));
        result.append("The formula '");
        result.append(violatedFormula);
        result.append("' was violated.\n");
        result.append(nTabs(numIndents));
        result.append("Proof:");
        for (int i = 0; i < timestepPropositionMap.size(); i++) {
            if (timestepPropositionMap.get(i).isEmpty()) {
                continue;
            }
            result.append("\n");
            result.append(nTabs(numIndents + 1));
            result.append("At time step ");
            result.append(i);
            result.append(",");
            Set<Formula> props = timestepPropositionMap.get(i);
            for (Formula prop : props) {
                result.append(" ");
                result.append(prop);
                result.append(",");
            }
            result.deleteCharAt(result.length() - 1);
            result.append(".");
        }
        return result.toString();
    }
}