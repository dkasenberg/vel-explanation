package velexplanation.vel;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import velexplanation.rewardvector.RewardVectorFunction;

/**
 * Created by dkasenberg on 5/22/18.
 */
public class VELObjectiveRVF implements RewardVectorFunction {

    public VELProductMDP product;

    public VELObjectiveRVF(VELProductMDP product) {
        this.product = product;
    }

    @Override
    public RealVector rv(State s, Action ga, State sp) {
        RealVector reward = new ArrayRealVector(product.objectives.size());
        if (sp instanceof VELProductState && s instanceof VELProductState) {
            for (int i = 0; i < product.objectives.size(); i++) {
                VELObjective velObjective = product.objectives.get(i);
                int multiplier = velObjective.cosafe ? -1 : 1;
                if (velObjective.durative) {
                    reward.setEntry(i, -multiplier * velObjective.durativeCost(((VELProductState) s).objectiveStates.get(i), sp));
                } else {
                    reward.setEntry(i, multiplier * (velObjective.groundingRelatedCost(((VELProductState) s).objectiveStates.get(i), ((VELProductState) s).s)
                            - velObjective.groundingRelatedCost(((VELProductState) sp).objectiveStates.get(i), ((VELProductState) sp).s)));
                }
            }
        }
        return reward;
    }

}
