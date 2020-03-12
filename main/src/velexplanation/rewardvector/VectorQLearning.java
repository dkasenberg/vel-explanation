package velexplanation.rewardvector;

/**
 * Created by dkasenberg on 6/4/18.
 */

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.options.EnvironmentOptionOutcome;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.Planner;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.yaml.snakeyaml.Yaml;

import javax.management.RuntimeErrorException;
import java.io.*;
import java.util.*;

public class VectorQLearning extends MDPSolver implements QVectorProvider, LearningAgent, Planner {
    protected Map<HashableState, VectorQLearningStateNode> qFunction;
    protected Comparator<RealVector> comparator;
    protected QVectorFunction qInitFunction;
    protected LearningRate learningRate;
    protected Policy learningPolicy;
    protected int maxEpisodeSize;
    protected int eStepCounter;
    protected int numEpisodesForPlanning;
    protected double maxQChangeForPlanningTermination;
    protected double maxQChangeInLastEpisode = 1.0D / 0.0;
    protected boolean shouldDecomposeOptions = true;
    protected int totalNumberOfSteps = 0;
    protected int size;
    protected RewardVectorFunction rvf;

    public VectorQLearning(SADomain domain, double gamma, HashableStateFactory hashingFactory, double qInit, int size, double learningRate, Comparator<RealVector> comparator, RewardVectorFunction rvf) {
        this.QLInit(domain, gamma, hashingFactory, new ConstantVectorValueFunction(qInit, size), size, learningRate, new VectorEpsilonGreedy(this, 0.1D), 2147483647, comparator, rvf);
    }

    public VectorQLearning(SADomain domain, double gamma, HashableStateFactory hashingFactory, double qInit, int size, double learningRate, int maxEpisodeSize, Comparator<RealVector> comparator, RewardVectorFunction rvf) {
        this.QLInit(domain, gamma, hashingFactory, new ConstantVectorValueFunction(qInit, size), size, learningRate, new VectorEpsilonGreedy(this, 0.1D), maxEpisodeSize, comparator, rvf);
    }

    public VectorQLearning(SADomain domain, double gamma, HashableStateFactory hashingFactory, double qInit, int size, double learningRate, Policy learningPolicy, int maxEpisodeSize, Comparator<RealVector> comparator, RewardVectorFunction rvf) {
        this.QLInit(domain, gamma, hashingFactory, new ConstantVectorValueFunction(qInit, size), size, learningRate, learningPolicy, maxEpisodeSize, comparator, rvf);
    }

    public VectorQLearning(SADomain domain, double gamma, HashableStateFactory hashingFactory, QVectorFunction qInit, int size, double learningRate, Policy learningPolicy, int maxEpisodeSize, Comparator<RealVector> comparator, RewardVectorFunction rvf) {
        this.QLInit(domain, gamma, hashingFactory, qInit, size, learningRate, learningPolicy, maxEpisodeSize, comparator, rvf);
    }

    protected void QLInit(SADomain domain, double gamma, HashableStateFactory hashingFactory, QVectorFunction qInitFunction, int size, double learningRate, Policy learningPolicy, int maxEpisodeSize, Comparator<RealVector> comparator, RewardVectorFunction rvf) {
        this.solverInit(domain, gamma, hashingFactory);
        this.qFunction = new HashMap();
        this.learningRate = new ConstantLR(Double.valueOf(learningRate));
        this.learningPolicy = learningPolicy;
        this.maxEpisodeSize = maxEpisodeSize;
        this.qInitFunction = qInitFunction;
        this.numEpisodesForPlanning = 1;
        this.maxQChangeForPlanningTermination = 0.0D;
        this.comparator = comparator;
        this.size = size;
        this.rvf = rvf;
    }

    public void initializeForPlanning(int numEpisodesForPlanning) {
        this.numEpisodesForPlanning = numEpisodesForPlanning;
    }

    public void setLearningRateFunction(LearningRate lr) {
        this.learningRate = lr;
    }

    public void setQInitFunction(QVectorFunction qInit) {
        this.qInitFunction = qInit;
    }

    public void setLearningPolicy(Policy p) {
        this.learningPolicy = p;
    }

    public void setMaximumEpisodesForPlanning(int n) {
        if (n > 0) {
            this.numEpisodesForPlanning = n;
        } else {
            this.numEpisodesForPlanning = 1;
        }

    }

    public void setMaxQChangeForPlanningTerminaiton(double m) {
        if (m > 0.0D) {
            this.maxQChangeForPlanningTermination = m;
        } else {
            this.maxQChangeForPlanningTermination = 0.0D;
        }

    }

    public int getLastNumSteps() {
        return this.eStepCounter;
    }

    public void toggleShouldDecomposeOption(boolean toggle) {
        this.shouldDecomposeOptions = toggle;
    }

    @Override
    public Comparator getComparator() {
        return comparator;
    }

    public List<QValueVector> qValues(State s) {
        return this.getQs(this.stateHash(s));
    }

    @Override
    public int size() {
        return size;
    }

    public RealVector qValue(State s, Action a) {
        return this.getQ(this.stateHash(s), a).q;
    }

    protected List<QValueVector> getQs(HashableState s) {
        VectorQLearningStateNode node = this.getStateNode(s);
        return node.qEntry;
    }

    protected QValueVector getQ(HashableState s, Action a) {
        VectorQLearningStateNode node = this.getStateNode(s);
        Iterator var4 = node.qEntry.iterator();

        QValueVector qv;
        do {
            if (!var4.hasNext()) {
                return null;
            }

            qv = (QValueVector) var4.next();
        } while (!qv.a.equals(a));

        return qv;
    }

    public RealVector value(State s) {
        return Helper.maxQ(this, s);
    }

    protected VectorQLearningStateNode getStateNode(HashableState s) {
        VectorQLearningStateNode node = (VectorQLearningStateNode) this.qFunction.get(s);
        if (node == null) {
            node = new VectorQLearningStateNode(s);
            List<Action> gas = this.applicableActions(s.s());
            if (gas.isEmpty()) {
                this.applicableActions(s.s());
                throw new RuntimeErrorException(new Error("No possible actions in this state, cannot continue Q-learning"));
            }

            Iterator var4 = gas.iterator();

            while (var4.hasNext()) {
                Action ga = (Action) var4.next();
                node.addQValue(ga, this.qInitFunction.qValue(s.s(), ga));
            }

            this.qFunction.put(s, node);
        }

        return node;
    }

    protected RealVector getMaxQ(HashableState s) {
        List<QValueVector> qs = this.getQs(s);
        RealVector max = new ArrayRealVector(size, Double.NEGATIVE_INFINITY);
        Iterator var5 = qs.iterator();

        while (var5.hasNext()) {
            QValueVector q = (QValueVector) var5.next();
            if (comparator.compare(q.q, max) > 0) {
                max = q.q;
            }
        }

        return max;
    }

    public GreedyQVectorPolicy planFromState(State initialState) {
        if (this.model == null) {
            throw new RuntimeException("QLearning (and its subclasses) cannot execute planFromState because a model is not specified.");
        } else {
            SimulatedEnvironment env = new SimulatedEnvironment(this.domain, initialState);
            int eCount = 0;

            do {
                this.runLearningEpisode(env, this.maxEpisodeSize);
                ++eCount;
            }
            while (eCount < this.numEpisodesForPlanning && this.maxQChangeInLastEpisode > this.maxQChangeForPlanningTermination);
            return new GreedyQVectorPolicy(this);
        }
    }

    public Episode runLearningEpisode(Environment env) {
        return this.runLearningEpisode(env, -1);
    }

    public Episode runLearningEpisode(Environment env, int maxSteps) {
        State initialState = env.currentObservation();
        Episode ea = new Episode(initialState);
        HashableState curState = this.stateHash(initialState);
        this.eStepCounter = 0;

        for (this.maxQChangeInLastEpisode = 0.0D; !env.isInTerminalState() && (this.eStepCounter < maxSteps || maxSteps == -1); ++this.totalNumberOfSteps) {
            Action action = this.learningPolicy.action(curState.s());
            QValueVector curQ = this.getQ(curState, action);
            Object eo;
            if (!(action instanceof Option)) {
                eo = env.executeAction(action);
            } else {
                eo = ((Option) action).control(env, this.gamma);
            }

            HashableState nextState = this.stateHash(((EnvironmentOutcome) eo).op);
            RealVector maxQ = new OpenMapRealVector(size);
            if (!((EnvironmentOutcome) eo).terminated) {
                maxQ = this.getMaxQ(nextState);
            }


            RealVector r = rvf.rv(((EnvironmentOutcome) eo).o, action, ((EnvironmentOutcome) eo).op);
            double discount = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome) eo).discount : this.gamma;
            int stepInc = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome) eo).numSteps() : 1;
            this.eStepCounter += stepInc;
            if (action instanceof Option && this.shouldDecomposeOptions) {
                ea.appendAndMergeEpisodeAnalysis(((EnvironmentOptionOutcome) eo).episode);
            } else {
                ea.transition(action, nextState.s(), 0.);
            }

            RealVector oldQ = curQ.q;

            curQ.q = curQ.q.add(r.add(maxQ.mapMultiply(discount)).subtract(curQ.q)
                    .mapMultiply(this.learningRate.pollLearningRate(this.totalNumberOfSteps, curState.s(), action)));
            double deltaQ = oldQ.subtract(curQ.q).getNorm();
            if (deltaQ > this.maxQChangeInLastEpisode) {
                this.maxQChangeInLastEpisode = deltaQ;
            }

            curState = this.stateHash(env.currentObservation());
        }

        return ea;
    }

    public void resetSolver() {
        this.qFunction.clear();
        this.eStepCounter = 0;
        this.maxQChangeInLastEpisode = 1.0D / 0.0;
    }

    public void writeQTable(String path) {
        Yaml yaml = new Yaml();

        try {
            yaml.dump(this.qFunction, new BufferedWriter(new FileWriter(path)));
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    public void loadQTable(String path) {
        Yaml yaml = new Yaml();

        try {
            this.qFunction = (Map) yaml.load(new FileInputStream(path));
        } catch (FileNotFoundException var4) {
            var4.printStackTrace();
        }

    }
}
