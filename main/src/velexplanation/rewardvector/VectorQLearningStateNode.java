package velexplanation.rewardvector;

import burlap.mdp.core.action.Action;
import burlap.statehashing.HashableState;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkasenberg on 6/4/18.
 */
public class VectorQLearningStateNode {

    public HashableState s;
    public List<QValueVector> qEntry;

    public VectorQLearningStateNode() {
    }

    public VectorQLearningStateNode(HashableState s) {
        this.s = s;
        this.qEntry = new ArrayList();
    }

    public void addQValue(Action a, RealVector q) {
        QValueVector qv = new QValueVector(this.s.s(), a, q);
        this.qEntry.add(qv);
    }
}