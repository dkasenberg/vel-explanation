package velexplanation.rewardvector;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableStateFactory;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import velexplanation.misc.TerminalRewardFunction;
import velexplanation.rewardvector.comparator.DoubleDeckerComparator;
import velexplanation.rewardvector.comparator.LastAndLeastComparator;

import java.util.Comparator;
import java.util.List;

public class ParsimoniousRVDoubleDeckerValueIteration extends RVValueIteration {

    private TerminalRewardFunction trf;

    public ParsimoniousRVDoubleDeckerValueIteration(SADomain domain, double gamma, HashableStateFactory hashingFactory,
                                                    double maxDelta, int maxIterations, RewardVectorFunction rvf, int vectorSize, Comparator vectorCompare) {
        super(domain, gamma, hashingFactory, maxDelta, maxIterations, rvf, vectorSize * 2 + 1,
                new LastAndLeastComparator(new DoubleDeckerComparator(vectorCompare)));

        this.trf = new TerminalRewardFunction(this.model);
    }

    public ParsimoniousRVDoubleDeckerValueIteration(SADomain domain, double gamma, HashableStateFactory hashingFactory,
                                                    double maxDelta, int maxIterations, RewardVectorFunction rvf, int vectorSize, Comparator vectorCompare, boolean ignoreTF) {
        super(domain, gamma, hashingFactory, maxDelta, maxIterations, rvf, vectorSize * 2 + 1,
                new LastAndLeastComparator(new DoubleDeckerComparator(vectorCompare)), ignoreTF);
        this.trf = new TerminalRewardFunction(this.model);
    }

    @Override
    protected RealVector computeQ(State s, Action ga) {

        RealVector q = new ArrayRealVector(size);

        List<TransitionProb> tps = ((FullModel) model).transitions(s, ga);

        int rawSize = (size - 1) / 2;

        for (TransitionProb tp : tps) {
            State sp = tp.eo.op;

//            Top isn't discounted, but bottom is
            RealVector valSp = this.value(sp);

            RealVector valSpTop = valSp.getSubVector(0, rawSize);
            RealVector valSpBottom = valSp.getSubVector(rawSize, rawSize);

            RealVector rv = this.rvf.rv(s, ga, sp);

            RealVector qAdd = new ArrayRealVector(size);

            qAdd.setSubVector(0, rv.add(valSpTop));
            qAdd.setSubVector(rawSize, rv.add(valSpBottom.mapMultiply(this.gamma)));
            qAdd.setEntry(size - 1, trf.reward(s, ga, sp) + this.gamma * valSp.getEntry(size - 1));

            q = q.add(qAdd.mapMultiply(tp.p));
        }

        return q;
    }
}
