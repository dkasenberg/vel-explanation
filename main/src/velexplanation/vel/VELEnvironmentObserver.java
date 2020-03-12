package velexplanation.vel;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.extensions.EnvironmentObserver;
import velexplanation.actions.RestrictedActionType;

/**
 * Created by dkasenberg on 5/30/18.
 */
public class VELEnvironmentObserver implements EnvironmentObserver {

    private VELPlanner planner;

    public VELEnvironmentObserver(VELPlanner ncr) {
        this.planner = ncr;
    }

    public void reset(State newInitialState) {
        planner.resetEnvironment();
        planner.recomputeBestActions(newInitialState);
        updateActions();
    }

    protected void updateActions() {
        planner.restricted.domain.getActionTypes().stream().map((a) -> (RestrictedActionType) a).forEach((ra) -> {
            ra.setActionRestriction(planner.nextActions);
        });
    }

    @Override
    public void observeEnvironmentInteraction(EnvironmentOutcome eo) {
        State sp = eo.op;
        planner.recomputeBestActions(sp);
        updateActions();
    }

    @Override
    public void observeEnvironmentReset(Environment resetEnvironment) {
        this.reset(resetEnvironment.currentObservation());
    }

    @Override
    public void observeEnvironmentActionInitiation(State o, Action action) {

    }

}
