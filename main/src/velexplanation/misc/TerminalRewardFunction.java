package velexplanation.misc;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.SampleModel;

public class TerminalRewardFunction implements RewardFunction {

    private SampleModel tf;

    public TerminalRewardFunction(SampleModel tf) {
        this.tf = tf;
    }

    @Override
    public double reward(State state, Action action, State state1) {
        return tf.terminal(state1) && !tf.terminal(state) ? 1. : 0;
    }
}
