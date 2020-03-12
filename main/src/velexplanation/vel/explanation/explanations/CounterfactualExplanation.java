package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import velexplanation.misc.Pair;
import velexplanation.vel.VELObjective;
import velexplanation.vel.explanation.query.VELQuery;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class CounterfactualExplanation extends Explanation {
    protected Episode realEpisode;
    protected Episode counterfactualEpisode;
    protected VELQuery query;
    protected Set<ObjectiveViolatedExplanation> objectivesViolatedByRealEpisode;
    protected Set<ObjectiveViolatedExplanation> objectivesViolatedByAltEpisode;

    protected CounterfactualExplanation(Episode realEpisode, Episode counterfactualEpisode, VELQuery query,
                                        Set<ObjectiveViolatedExplanation> objectivesViolatedByRealEpisode,
                                        Set<ObjectiveViolatedExplanation> objectivesViolatedByAltEpisode) {
        this.counterfactualEpisode = counterfactualEpisode;
        this.realEpisode = realEpisode;
        this.query = query;
        this.objectivesViolatedByRealEpisode = objectivesViolatedByRealEpisode;
        this.objectivesViolatedByAltEpisode = objectivesViolatedByAltEpisode;
    }

    public List<Pair<VELObjective, Set<Map<String, String>>>> getObjectiveViolations(boolean counterfactual) {
        Set<ObjectiveViolatedExplanation> relevantViolationList = counterfactual ?
                objectivesViolatedByAltEpisode : objectivesViolatedByRealEpisode;

        return relevantViolationList.stream().map(objExpl ->
                new Pair<>(objExpl.objective, objExpl.getViolations())).collect(Collectors.toList());
    }

    public Episode getCounterfactualEpisode() {
        return counterfactualEpisode;
    }

    public Episode getRealEpisode() {
        return realEpisode;
    }
}
