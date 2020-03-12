package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import velexplanation.mdp.MDPContainer;
import velexplanation.misc.Pair;
import velexplanation.rewardvector.comparator.MixedLexicographicWeightedComparator;
import velexplanation.vel.VELObjective;
import velexplanation.vel.explanation.ExplanationHelper;
import velexplanation.vel.explanation.query.VELQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EpisodeComparisonExplanation extends CounterfactualExplanation {

    public Set<Pair<Double, ObjectiveViolatedExplanation>> minimalExplanation;
    private int priorityLevel;
    private int maxPriorityLevel;
    private RealVector weightsAtPriority;

    private EpisodeComparisonExplanation(Episode realEpisode,
                                         Episode counterfactualEpisode,
                                         int priorityLevel,
                                         int maxPriorityLevel,
                                         Set<Pair<Double, ObjectiveViolatedExplanation>> minimalExplanation,
                                         Set<ObjectiveViolatedExplanation> objectivesViolatedByRealEpisode,
                                         Set<ObjectiveViolatedExplanation> objectivesViolatedByAltEpisode,
                                         RealVector weightsAtPriority,
                                         VELQuery query) {
        super(realEpisode, counterfactualEpisode, query, objectivesViolatedByRealEpisode,
                objectivesViolatedByAltEpisode);
        this.minimalExplanation = minimalExplanation;
        this.priorityLevel = priorityLevel;
        this.maxPriorityLevel = maxPriorityLevel;
        this.weightsAtPriority = weightsAtPriority;
    }

    public static CounterfactualExplanation construct(Episode realEpisode,
                                                      Episode counterfactualEpisode,
                                                      List<VELObjective> objectives,
                                                      MixedLexicographicWeightedComparator comparator,
                                                      MDPContainer origMDP,
                                                      VELQuery query) throws Exception {


        ObjectivesViolatedExplanation expReal = ObjectivesViolatedExplanation.construct(objectives, realEpisode, origMDP);

        ObjectivesViolatedExplanation expCounterfactual = ObjectivesViolatedExplanation.construct(objectives,
                counterfactualEpisode, origMDP);


        Set<ObjectiveViolatedExplanation> objectivesViolatedByAltEpisode = expCounterfactual.objectiveExplanations
                .stream().filter(exp -> exp.getViolationCost() > 0).collect(Collectors.toSet());

        Set<ObjectiveViolatedExplanation> objectivesViolatedByRealEpisode = expReal.objectiveExplanations
                .stream().filter(exp -> exp.getViolationCost() > 0).collect(Collectors.toSet());

        RealVector vcReal = expReal.getViolationCost();
        RealVector vcCounterfactual = expCounterfactual.getViolationCost();

        if (comparator.compare(vcCounterfactual, vcReal) > 0) {
            RealMatrix priorities = comparator.getPrioritiesMatrix();

            for (int priorityLevel = priorities.getRowDimension() - 1; priorityLevel >= 0; priorityLevel--) {
                RealVector weightsForPriority = priorities.getRowVector(priorityLevel);
                double priorityWeightReal = weightsForPriority.dotProduct(vcReal);
                double priorityWeightCounterfactual = weightsForPriority.dotProduct(vcCounterfactual);
                double priorityWeightDiff = priorityWeightCounterfactual - priorityWeightReal;
                if (priorityWeightDiff < -comparator.threshold) {
                    throw new Exception("Counterfactual trajectory is better!");
                } else if (priorityWeightDiff > comparator.threshold) {
                    Set<Pair<Double, ObjectiveViolatedExplanation>> altEpisodeViolatedExplanations = new HashSet<>();
                    List<Integer> objectivesForPriority = ExplanationHelper.getNonZeroEntries(weightsForPriority);
                    for (int objIndex : objectivesForPriority) {
                        double weight = weightsForPriority.getEntry(objIndex);
                        ObjectiveViolatedExplanation objExpl = expCounterfactual.objectiveExplanations.get(objIndex)
                                .cutToNViolations((int) Math.floor(priorityWeightReal / weight) + 1);
                        altEpisodeViolatedExplanations.add(new Pair<>(weight, objExpl));
                        priorityWeightReal -= weight * objExpl.getViolationCost();
                        if (priorityWeightReal < 0) break;
                    }
                    return new EpisodeComparisonExplanation(
                            realEpisode,
                            counterfactualEpisode,
                            priorityLevel,
                            priorities.getRowDimension() - 1,
                            altEpisodeViolatedExplanations,
                            objectivesViolatedByRealEpisode,
                            objectivesViolatedByAltEpisode,
                            weightsForPriority,
                            query
                    );
                }
            }
        }

        return new EpisodeComparisonShrugExplanation(realEpisode, counterfactualEpisode, query,
                objectivesViolatedByRealEpisode, objectivesViolatedByAltEpisode);
    }

    @Override
    public String toStringWithIndentation(int numIndents) {
        StringBuilder result = new StringBuilder();
        List<Pair<Double, ObjectiveViolatedExplanation>> orderedExplanation = new ArrayList<>(minimalExplanation);
        ObjectivesViolatedExplanation expsZipped = new ObjectivesViolatedExplanation(orderedExplanation
                .stream().map(p -> p.getRight()).collect(Collectors.toList()));
        List<Double> weights = orderedExplanation.stream().map(p -> p.getLeft()).collect(Collectors.toList());
        result.append("The counterfactual trajectory violated at least the following objectives:\n");
        result.append(expsZipped.toStringWithIndentation(numIndents + 1));
        result.append(nTabs(numIndents));
        result.append("\nThe total weight of these violations is");
        RealVector vcExplanation = expsZipped.getViolationCost();
        double vcScalar = 0.;
        boolean addPlus = false;
        for (int i = 0; i < weights.size(); i++) {

            if (weights.get(i) > 0 && vcExplanation.getEntry(i) > 0) {
                result.append(addPlus ? " + " : " ");
                addPlus = true;
                result.append(weights.get(i));
                result.append("*");
                vcScalar += weights.get(i) * vcExplanation.getEntry(i);
                result.append((int) vcExplanation.getEntry(i));
            }
        }
        result.append(" = ");
        result.append(vcScalar);
        result.append(".\n");
        result.append(nTabs(numIndents));

        if (priorityLevel < maxPriorityLevel) {
            result.append(nTabs(numIndents));
            result.append("The two episodes are comparable with respect to any objectives more important than these.\n");
        }

        result.append(nTabs(numIndents));
        result.append("Thus, the observed episode is preferable to the counterfactual episode.");

        return result.toString();
    }
}
