package velexplanation.vel;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.NullState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.model.statemodel.SampleStateModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static velexplanation.vel.VELProductMDP.SeeFirstStateActionType.ACTION_SEE_FIRST_STATE;

/**
 * Created by dkasenberg on 5/21/18.
 */
public class VELProductModel implements FullStateModel {
    public FullStateModel model;
    public List<VELObjective> objectives;
    public VELProductMDP mdp;

    public VELProductModel(SampleStateModel model, List<VELObjective> objectives, VELProductMDP mdp) {
        if (!(model instanceof FullStateModel)) throw new RuntimeException("Must be a FullStateModel");
        this.model = (FullStateModel) model;
        this.mdp = mdp;
        this.objectives = objectives;
    }


    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        VELProductState ps = (VELProductState) s;

        if (a.actionName().equals(ACTION_SEE_FIRST_STATE) || ps.s instanceof NullState) {
            return Arrays.asList(new StateTransitionProb(getProductTransition(ps, mdp.mdp.initialState), 1.0));
        }

        List<StateTransitionProb> probs = model.stateTransitions(ps.s, a);
        return probs.stream().map(stp -> new StateTransitionProb(getProductTransition(ps, stp.s), stp.p))
                .collect(Collectors.toList());
    }

    public VELProductState getProductTransition(VELProductState s, State sp) {

        List<VELObjectiveState> newObjectiveStates = new ArrayList<>();
        for (int i = 0; i < objectives.size(); i++) {
            VELObjective curObjective = objectives.get(i);
            VELObjectiveState origState = s.objectiveStates.get(i);
            newObjectiveStates.add(curObjective.nextState(origState, sp));
        }
        return new VELProductState(sp, newObjectiveStates);
    }

    public State sample(State s, Action a) {
        return Helper.sampleByEnumeration(this, s, a);
    }
}
