package velexplanation.rewardvector.comparator;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import velexplanation.misc.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by dkasenberg on 6/1/18.
 */
public class MixedLexicographicWeightedComparator implements Comparator<RealVector> {

    public double threshold;
    private RealMatrix priorities;
    private List<Integer> prioritiesList;
    private RealVector weights;

    public MixedLexicographicWeightedComparator(List<Integer> priorityList, List<Double> weights, double threshold) {
        this(priorityList, new ArrayRealVector(weights.stream().mapToDouble(d -> d).toArray()), threshold);
    }

    public MixedLexicographicWeightedComparator(List<Integer> priorityList, RealVector weights, double threshold) {

        this.prioritiesList = priorityList;
        this.weights = weights;

        List<Integer> np = normalizedPriorities(priorityList);

        this.priorities = new OpenMapRealMatrix(
                np.stream().max(Comparator.comparing(j -> j)).orElse(-1) + 1,
                weights.getDimension());
        for (int i = 0; i < weights.getDimension(); i++) {
            priorities.setEntry(np.get(i), i, weights.getEntry(i));
        }

        this.threshold = Math.abs(threshold);
    }

    public RealMatrix getPrioritiesMatrix() {
        return priorities.copy();
    }

    public MixedLexicographicWeightedComparator add(int priority, double weight) {
        List<Integer> newPriorities = new ArrayList<>(this.prioritiesList);
        RealVector newWeights = weights.copy().append(weight);
        newPriorities.add(priority);

        return new MixedLexicographicWeightedComparator(newPriorities, newWeights, threshold);
    }

    public int getMaxPriority() {
        return prioritiesList.stream().mapToInt(j -> j).max().getAsInt();
    }

    public MixedLexicographicWeightedComparator removeAtIndex(int index) {
        List<Integer> newPriorities = new ArrayList<>(this.prioritiesList);
        RealVector newWeights = new ArrayRealVector(IntStream.range(0,
                weights.getDimension()).filter(i -> i != index)
                .mapToDouble(i -> weights.getEntry(i)).toArray());
        return new MixedLexicographicWeightedComparator(newPriorities, newWeights, threshold);
    }

    public MixedLexicographicWeightedComparator addAtMaxPriority() {
        List<Integer> newPrioritiesList = new ArrayList<>(this.prioritiesList);
        newPrioritiesList.add(getMaxPriority() + 1);

        return new MixedLexicographicWeightedComparator(newPrioritiesList, weights.append(1.), threshold);
    }


    //     TODO fix this : it's not ordering the objectives right (should put them back in order)
    private List<Integer> normalizedPriorities(List<Integer> priorityList) {

        List<Pair<Integer, Integer>> indicesAndValues = new ArrayList<>();
        Integer[] toReturn = new Integer[priorityList.size()];

        for (int i = 0; i < priorityList.size(); i++) {
            indicesAndValues.add(new Pair<>(i, priorityList.get(i)));
        }
        indicesAndValues.sort(Comparator.comparing(p -> p.getRight()));
        int prevValue = Integer.MIN_VALUE;
        int normalized = -1;

        for (int i = 0; i < indicesAndValues.size(); i++) {
            if (indicesAndValues.get(i).getRight() > prevValue) {
                normalized++;
                prevValue = indicesAndValues.get(i).getRight();
            }
            toReturn[indicesAndValues.get(i).getLeft()] = normalized;
        }
        return Arrays.asList(toReturn);
    }

    @Override
    public int compare(RealVector o1, RealVector o2) {
        RealVector groupedDifference = priorities.operate(o1.subtract(o2));

        for (int i = groupedDifference.getDimension() - 1; i >= 0; i--) {
            if (groupedDifference.getEntry(i) > threshold) return 1;
            else if (groupedDifference.getEntry(i) < -threshold) return -1;
        }
        return 0;
    }

    public WeightedSumComparator asWeightedSum(List<Integer> numPossibleBindings) {
        RealVector newWeights = priorities.getRowVector(0);
        for (int i = 1; i < priorities.getRowDimension(); i++) {
            double multiplier = 0.;
            double minEntry = Double.POSITIVE_INFINITY;
            for (int j = 0; j < priorities.getColumnDimension(); j++) {
                multiplier += newWeights.getEntry(j) * ((double) numPossibleBindings.get(i));
                if (priorities.getEntry(i, j) > 0) {
                    minEntry = Math.min(minEntry, priorities.getEntry(i, j));
                }
            }
            multiplier = multiplier / minEntry + 1;
            newWeights = newWeights.add(priorities.getRowVector(i).mapMultiply(multiplier));
        }
        return new WeightedSumComparator(newWeights, threshold);
    }
}
