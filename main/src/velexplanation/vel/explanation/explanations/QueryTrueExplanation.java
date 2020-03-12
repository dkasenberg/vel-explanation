package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.state.State;
import velexplanation.mdp.MDPContainer;
import velexplanation.vel.VELProductMDP;
import velexplanation.vel.VELProductState;
import velexplanation.vel.explanation.query.VELQuery;

import java.util.*;

import static velexplanation.vel.explanation.ExplanationHelper.getInconsistentMinterms;

public class QueryTrueExplanation extends Explanation {
    private QuantifiedFormulaExplanation formulaExplanation;
    private VELQuery query;

    public QueryTrueExplanation(VELQuery query, QuantifiedFormulaExplanation formulaExplanation) {
        this.formulaExplanation = formulaExplanation;
        this.query = query;
    }

    public static QueryTrueExplanation construct(VELQuery query,
                                                 Episode episode,
                                                 MDPContainer origMDP) throws Exception {

        VELProductMDP productMDP = new VELProductMDP(origMDP, Collections.singletonList(query.negativeObjective));
        List<VELProductState> productHistory = new ArrayList<>();

        VELProductState curState = (VELProductState) productMDP.initialState;
        productHistory.add(curState);

        for (State s : episode.stateSequence) {
            curState = productMDP.model.getProductTransition(curState, s);
            productHistory.add(curState);
        }

        VELProductState finalState = productHistory.get(productHistory.size() - 1);

        Set<Map<String, String>> violatingGroundings;
        if (query.negativeObjective.cosafe) {
            violatingGroundings = query.negativeObjective.getNonAcceptingGroundings(finalState.objectiveStates.get(0));
        } else {
            violatingGroundings = query.negativeObjective.getAcceptingGroundings(finalState.objectiveStates.get(0));
        }

        return new QueryTrueExplanation(query, QuantifiedFormulaExplanation.construct(
                query.negativeObjective,
                0,
                productHistory,
                getInconsistentMinterms(query.negativeObjective),
                episode,
                new HashMap<>(),
                violatingGroundings,
                0
        ));
    }

    @Override
    public String toStringWithIndentation(int numIndents) {
        StringBuilder result = new StringBuilder();
        result.append(nTabs(numIndents));
        result.append("The query '");
        result.append(query.positiveObjective);
        result.append("' is true.\n");
        result.append(formulaExplanation.toStringWithIndentation(numIndents + 1));
        return result.toString();
    }
}
