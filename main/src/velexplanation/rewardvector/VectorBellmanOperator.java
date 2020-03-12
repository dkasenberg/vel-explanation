package velexplanation.rewardvector;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dan on 5/16/17.
 */
public class VectorBellmanOperator implements VectorDPOperator {

    @Override
    public RealVector apply(RealVector[] qs, Comparator<RealVector> comparator) {

        RealVector mx = qs[0];
        List<RealVector> maxQ = new ArrayList<>();
        maxQ.add(mx);

        for (int i = 1; i < qs.length; i++) {
            RealVector qi = qs[i];
            if (comparator.compare(qi, mx) > 0) {
                mx = qi;
                maxQ.clear();
                maxQ.add(mx);
            } else if (comparator.compare(qi, mx) == 0) {
                maxQ.add(qi);
            }
        }
        return maxQ.stream().reduce(new ArrayRealVector(mx.getDimension()),
                RealVector::add).mapDivide(maxQ.size());

//        return mx;
    }
}