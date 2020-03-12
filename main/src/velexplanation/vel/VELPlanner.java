package velexplanation.vel;

import burlap.debugtools.DPrint;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.environment.extensions.EnvironmentServer;
import burlap.statehashing.HashableStateFactory;
import org.apache.commons.math3.linear.RealVector;
import velexplanation.mdp.MDPContainer;
import velexplanation.mdp.RestrictedMDP;
import velexplanation.misc.Pair;
import velexplanation.rewardvector.ParsimoniousRVDoubleDeckerValueIteration;
import velexplanation.rewardvector.QValueVector;
import velexplanation.rewardvector.RVDoubleDeckerValueIteration;
import velexplanation.rewardvector.RVValueIteration;
import velexplanation.vel.parsing.VELParser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by dkasenberg on 5/30/18.
 */
public class VELPlanner {

    public List<VELObjective> objectives;
    public Comparator<RealVector> comp;
    public RVValueIteration values;
    public VELProductMDP product;
    public RestrictedMDP restricted;
    public Set<Action> nextActions;
    private SADomain domain;
    private State initialState;
    private VELProductState curState;
    private HashableStateFactory hf;
    private double discount;
    private VELEnvironmentObserver observer;
    private MDPContainer origMDP;
    private VELObjectiveRVF rvf;

    private boolean ignoreTF;

    public VELPlanner(String objectiveText, SADomain d, State s, HashableStateFactory hf, double discount) {
        this(objectiveText, d, s, hf, discount, true, true, false);
    }

    public VELPlanner(String objectiveText, SADomain d, State s, HashableStateFactory hf, double discount,
                      boolean init) {
        this(objectiveText, d, s, hf, discount, init, true, false);
    }

    public VELPlanner(String objectiveText, SADomain d, State s, HashableStateFactory hf, double discount,
                      boolean init, boolean doubleDecker, boolean shorten) {
        this(objectiveText, d, s, hf, discount, init, doubleDecker, shorten, false);
    }

    public VELPlanner(String objectiveText, SADomain d, State s, HashableStateFactory hf, double discount,
                      boolean init, boolean doubleDecker, boolean shorten, boolean ignoreTF) {
        this.domain = d;
        this.initialState = s;
        this.hf = hf;
        this.discount = discount;
        this.observer = new VELEnvironmentObserver(this);
        this.ignoreTF = ignoreTF;

        origMDP = new MDPContainer(d, s, hf);

//        origMDP = new RecordedActionMDP(origMDP);

        try {
            Pair<List<VELObjective>, Comparator<RealVector>> parsed = VELParser.parseFromText(objectiveText, d, s);
            this.objectives = new ArrayList<>(parsed.getLeft());
            this.comp = parsed.getRight();

            this.restricted = new RestrictedMDP(origMDP);
            if (init) {
                this.computeOptimalActions(doubleDecker, shorten);
            }

            observer.reset(origMDP.initialState);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VELPlanner(List<VELObjective> objectives, Comparator<RealVector> comp, SADomain d, State s, HashableStateFactory hf, double discount,
                      boolean init, boolean doubleDecker, boolean shorten) {
        this(objectives, comp, d, s, hf, discount, init, doubleDecker, shorten, false);
    }

    public VELPlanner(List<VELObjective> objectives, Comparator<RealVector> comp, SADomain d, State s, HashableStateFactory hf, double discount,
                      boolean init, boolean doubleDecker, boolean shorten, boolean ignoreTF) {
        this.domain = d;
        this.initialState = s;
        this.hf = hf;
        this.discount = discount;
        this.observer = new VELEnvironmentObserver(this);
        this.ignoreTF = ignoreTF;

        origMDP = new MDPContainer(d, s, hf);

//        origMDP = new RecordedActionMDP(origMDP);

        this.objectives = objectives;
        this.comp = comp;

        this.restricted = new RestrictedMDP(origMDP);
        if (init) {
            this.computeOptimalActions(doubleDecker, shorten);
        }

        observer.reset(origMDP.initialState);
    }

    public HashableStateFactory getHashingFactory() {
        return this.hf;
    }

    protected void resetEnvironment() {
        this.curState = (VELProductState) product.initialState;
    }

    protected void computeOptimalActions(boolean doubleDecker, boolean shorten) {
        this.product = new VELProductMDP(new MDPContainer(this.domain,
                this.initialState,
                hf), this.objectives);

        this.curState = (VELProductState) product.initialState;

        this.rvf = new VELObjectiveRVF(product);


//        this.values = new RVValueIteration(product.domain, this.discount,hf,0.01,1000,
//                rvf,objectives.size(), this.comp);


//        TODO(dkasenberg) double decker value iteration is broken with durative objectives.  All of what we're now
//        doing assumes that objectives are non-durative.

        if (shorten && doubleDecker) {
            this.values = new ParsimoniousRVDoubleDeckerValueIteration(product.domain, this.discount, hf, 1E-5, 1500, rvf,
                    objectives.size(), this.comp, ignoreTF);
        } else if (doubleDecker) {
            this.values = new RVDoubleDeckerValueIteration(product.domain, this.discount, hf, 1E-5, 1500,
                    rvf, objectives.size(), this.comp, ignoreTF);
        } else {
            this.values = new RVValueIteration(product.domain, this.discount, hf, 1E-5, 1500, rvf,
                    objectives.size(), this.comp, ignoreTF);
        }
        this.comp = this.values.getComparator();

        DPrint.toggleCode(values.getDebugCode(), true);
        values.planFromState(product.initialState);

    }

    public Environment getEnvironment() {
        return new EnvironmentServer(new SimulatedEnvironment(this.restricted.domain,
                this.restricted.initialState), this.observer);
    }

    public void recomputeBestActions(State newState) {
        VELProductState s = product.model.getProductTransition(this.curState, newState);
        List<QValueVector> qs = values.qValues(s);

        RealVector maxQ = qs.stream().map(q -> q.q).max(this.comp).get();
        this.nextActions = qs.stream().filter(q -> comp.compare(q.q, maxQ) == 0)
                .map(q -> q.a)
                .collect(Collectors.toSet());

        this.curState = s;
    }

    public SADomain getCurrentDomain() {
        return restricted.domain;
    }
}
