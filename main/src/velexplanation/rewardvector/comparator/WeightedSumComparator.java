/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velexplanation.rewardvector.comparator;

import org.apache.commons.math3.linear.RealVector;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author dkasenberg
 */
public class WeightedSumComparator implements Comparator<RealVector> {
    public RealVector weights;
    public double threshold;

    public WeightedSumComparator(RealVector weights, double threshold) {
        this.weights = weights;
        this.threshold = threshold;
    }

    public double getDifference(RealVector r1, RealVector r2) {
        return weights.dotProduct(r1.subtract(r2));
    }

    @Override
    public int compare(RealVector t, RealVector t1) {
        double weightedsumdiff = getDifference(t, t1);
        if (Math.abs(weightedsumdiff) < threshold) return 0;
//        if(weightedsumdiff == 0) return 0;
        if (weightedsumdiff > 0) return 1;
        return -1;
    }

    public double getOverridingWeight(List<Integer> numPossibleBindings) {
        return IntStream.range(0, numPossibleBindings.size())
                .mapToDouble(i -> ((double) numPossibleBindings.get(i)) * weights.getEntry(i))
                .sum() + 1;
    }
}
