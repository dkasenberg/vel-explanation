package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.state.State;
import velexplanation.mdp.MDPContainer;
import velexplanation.vel.VELProductMDP;
import velexplanation.vel.VELProductState;
import velexplanation.vel.explanation.query.VELQuery;

import java.util.*;

import static velexplanation.vel.explanation.ExplanationHelper.getInconsistentMinterms;

public class QueryFalseExplanation extends Explanation {
    private QuantifiedFormulaExplanation formulaExplanation;
    private VELQuery query;

    public QueryFalseExplanation(VELQuery query, QuantifiedFormulaExplanation formulaExplanation) {
        this.formulaExplanation = formulaExplanation;
        this.query = query;
    }

    public static QueryFalseExplanation construct(VELQuery query,
                                                  Episode episode,
                                                  MDPContainer origMDP) throws Exception {


        VELProductMDP productMDP = new VELProductMDP(origMDP, Collections.singletonList(query.positiveObjective));
        List<VELProductState> productHistory = new ArrayList<>();

        VELProductState curState = (VELProductState) productMDP.initialState;
        productHistory.add(curState);

        for (State s : episode.stateSequence) {
            curState = productMDP.model.getProductTransition(curState, s);
            productHistory.add(curState);
        }

        VELProductState finalState = productHistory.get(productHistory.size() - 1);

        Set<Map<String, String>> violatingGroundings;
        if (query.positiveObjective.cosafe) {
            violatingGroundings = query.positiveObjective.getNonAcceptingGroundings(finalState.objectiveStates.get(0));
        } else {
            violatingGroundings = query.positiveObjective.getAcceptingGroundings(finalState.objectiveStates.get(0));
        }

        return new QueryFalseExplanation(query, QuantifiedFormulaExplanation.construct(
                query.positiveObjective,
                0,
                productHistory,
                getInconsistentMinterms(query.positiveObjective),
                episode,
                new HashMap<>(),
                violatingGroundings,
                0
        ));
    }

    public QuantifiedFormulaExplanation getFormulaExplanation() {
        return formulaExplanation;
    }

    @Override
    public String toStringWithIndentation(int numIndents) {
        StringBuilder result = new StringBuilder();
        result.append(nTabs(numIndents));
        result.append("The query '");
        result.append(query.positiveObjective);
        result.append("' is false.\n");
        result.append(formulaExplanation.toStringWithIndentation(numIndents + 1));
        return result.toString();
    }
}
