package velexplanation.vel.explanation.explanations;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.state.State;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import velexplanation.mdp.MDPContainer;
import velexplanation.vel.VELObjective;
import velexplanation.vel.VELProductMDP;
import velexplanation.vel.VELProductState;

import java.util.ArrayList;
import java.util.List;

import static velexplanation.vel.explanation.ExplanationHelper.getInconsistentMinterms;

public class ObjectivesViolatedExplanation extends Explanation {

    public List<ObjectiveViolatedExplanation> objectiveExplanations;

    ObjectivesViolatedExplanation(List<ObjectiveViolatedExplanation> objectiveExplanations) {
        this.objectiveExplanations = objectiveExplanations;
    }

    public static ObjectivesViolatedExplanation construct(List<VELObjective> objectives, Episode episode, MDPContainer origMDP) {
        VELProductMDP productMDP = new VELProductMDP(origMDP, objectives);
        List<VELProductState> productHistory = new ArrayList<>();

        VELProductState curState = (VELProductState) productMDP.initialState;
        productHistory.add(curState);

//        Get history in product MDP.
        for (State s : episode.stateSequence) {
            curState = productMDP.model.getProductTransition(curState, s);
            productHistory.add(curState);
        }

        List<ObjectiveViolatedExplanation> objectiveExplanations = new ArrayList<>();

        for (int objIndex = 0; objIndex < objectives.size(); objIndex++) {
            VELObjective objective = objectives.get(objIndex);

            objectiveExplanations.add(ObjectiveViolatedExplanation.construct(objective, objIndex, productHistory,
                    getInconsistentMinterms(objective), episode));

        }

        return new ObjectivesViolatedExplanation(objectiveExplanations);
    }

    public RealVector getViolationCost() {
        return new ArrayRealVector(objectiveExplanations.stream()
                .mapToDouble(ObjectiveViolatedExplanation::getViolationCost).toArray());
    }

    @Override
    public String toStringWithIndentation(int numIndents) {
        StringBuilder result = new StringBuilder();
        for (ObjectiveViolatedExplanation exp : objectiveExplanations) {
            result.append(exp.toStringWithIndentation(numIndents));
            result.append("\n");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
}
