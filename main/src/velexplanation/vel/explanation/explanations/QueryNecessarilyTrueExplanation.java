package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.state.State;
import velexplanation.mdp.MDPContainer;
import velexplanation.vel.VELObjective;
import velexplanation.vel.VELProductMDP;
import velexplanation.vel.VELProductState;
import velexplanation.vel.explanation.query.VELQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryNecessarilyTrueExplanation extends Explanation {

    private VELQuery query;

    private QueryNecessarilyTrueExplanation(VELQuery query) {
        this.query = query;
    }

    public static QueryNecessarilyTrueExplanation construct(VELQuery query, Episode counterfactualEpisode,
                                                            MDPContainer origMDP) throws Exception {
        if (queryNecessary(query, counterfactualEpisode, origMDP)) {
            return new QueryNecessarilyTrueExplanation(query);
        }
        throw new Exception("Query not impossible");

    }

    private static boolean queryNecessary(VELQuery query, Episode counterfactualEpisode, MDPContainer origMDP) {
//        Construct query objective history

        VELObjective queryObjective = query.negativeObjective;
        VELProductMDP productMDP = new VELProductMDP(origMDP, Collections.singletonList(queryObjective));
        List<VELProductState> productHistory = new ArrayList<>();

        VELProductState curState = (VELProductState) productMDP.initialState;
        productHistory.add(curState);

        for (State s : counterfactualEpisode.stateSequence) {
            curState = productMDP.model.getProductTransition(curState, s);
            productHistory.add(curState);
        }

        VELProductState finalState = productHistory.get(productHistory.size() - 1);

        return queryObjective.getQuantifiedSubgroundings(finalState.objectiveStates.get(0), finalState.s).isEmpty()
                ^ !queryObjective.cosafe;
    }

    @Override
    public String toStringWithIndentation(int numIndents) {
        StringBuilder result = new StringBuilder();
        result.append(nTabs(numIndents));
        result.append("It is not possible for '");
        result.append(query.positiveObjective);
        result.append("' to be false in the current environment.");
        return result.toString();
    }
}
