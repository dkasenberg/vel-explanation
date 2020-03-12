package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import velexplanation.vel.explanation.query.VELQuery;

import java.util.Set;

public class EpisodeComparisonShrugExplanation extends CounterfactualExplanation {

    EpisodeComparisonShrugExplanation(Episode real, Episode counterfactual, VELQuery query,
                                      Set<ObjectiveViolatedExplanation> objectivesViolatedByRealEpisode,
                                      Set<ObjectiveViolatedExplanation> objectivesViolatedByAltEpisode) {
        super(real, counterfactual, query, objectivesViolatedByRealEpisode, objectivesViolatedByAltEpisode);
    }

    public static EpisodeComparisonShrugExplanation construct(Episode real, Episode counterfactual, VELQuery query,
                                                              Set<ObjectiveViolatedExplanation> objectivesViolatedByRealEpisode,
                                                              Set<ObjectiveViolatedExplanation> objectivesViolatedByAltEpisode) {
        return new EpisodeComparisonShrugExplanation(real, counterfactual, query, objectivesViolatedByRealEpisode,
                objectivesViolatedByAltEpisode);
    }


    @Override
    public String toStringWithIndentation(int numIndents) {
        StringBuilder result = new StringBuilder();
        result.append(nTabs(numIndents));
        result.append("The real trajectory satisfies the statement '");
        result.append(query.positiveObjective);
        result.append("', but there exists a counterfactual trajectory not satisfying this statement ");
        result.append(" that is equivalent.\n");
        result.append("The choice between these two trajectories was arbitrary.");
        return result.toString();
    }
}
