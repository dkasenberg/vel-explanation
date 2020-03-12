package velexplanation.rewardvector;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableStateFactory;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import velexplanation.misc.Pair;
import velexplanation.rewardvector.comparator.DoubleDeckerComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RVDoubleDeckerValueIteration extends RVValueIteration {
    public RVDoubleDeckerValueIteration(SADomain domain, double gamma, HashableStateFactory hashingFactory, double maxDelta, int maxIterations, RewardVectorFunction rvf, int vectorSize, Comparator vectorCompare) {
        super(domain,
                gamma,
                hashingFactory,
                maxDelta,
                maxIterations,
                rvf,
                vectorSize * 2,
                new DoubleDeckerComparator(vectorCompare));
    }

    public RVDoubleDeckerValueIteration(SADomain domain, double gamma, HashableStateFactory hashingFactory, double maxDelta, int maxIterations, RewardVectorFunction rvf, int vectorSize, Comparator vectorCompare, boolean ignoreTF) {
        super(domain,
                gamma,
                hashingFactory,
                maxDelta,
                maxIterations,
                rvf,
                vectorSize * 2,
                new DoubleDeckerComparator(vectorCompare),
                ignoreTF);
    }

    protected RealVector computeQ(State s, Action ga) {

        RealVector q = new ArrayRealVector(size);

        List<Pair<Double, RealVector>> outcomes = new ArrayList();
        List<TransitionProb> tps = ((FullModel) model).transitions(s, ga);

        for (TransitionProb tp : tps) {
            State sp = tp.eo.op;

//            Top isn't discounted, but bottom is
            RealVector valSp = this.value(sp);

            RealVector valSpTop = valSp.getSubVector(0, size / 2);
            RealVector valSpBottom = valSp.getSubVector(size / 2, size / 2);

            RealVector rv = this.rvf.rv(s, ga, sp);

            RealVector qAdd = new ArrayRealVector(size);

            qAdd.setSubVector(0, rv.add(valSpTop));
            qAdd.setSubVector(size / 2, rv.add(valSpBottom.mapMultiply(this.gamma)));

            q = q.add(qAdd.mapMultiply(tp.p));
        }

        return q;
    }
}
