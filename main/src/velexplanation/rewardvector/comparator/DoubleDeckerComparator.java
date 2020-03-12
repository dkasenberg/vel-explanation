package velexplanation.rewardvector.comparator;

import org.apache.commons.math3.linear.RealVector;

import java.util.Comparator;

public class DoubleDeckerComparator implements Comparator<RealVector> {

    private Comparator<RealVector> baseComparator;

    public DoubleDeckerComparator(Comparator<RealVector> baseComparator) {
        this.baseComparator = baseComparator;
    }


    public Comparator<RealVector> getBaseComparator() {
        return baseComparator;
    }

    @Override
    public int compare(RealVector o1, RealVector o2) {
        if (o1.getDimension() % 2 == 1 || o2.getDimension() % 2 == 1) {
            throw new RuntimeException("Received vector with odd dimensionality; can't apply double-decker " +
                    "comparator.");
        }
        int halfDim = o1.getDimension() / 2;
        int topComparison = baseComparator.compare(o1.getSubVector(0, halfDim),
                o2.getSubVector(0, halfDim));
        if (topComparison != 0) {
            return topComparison;
        }
        return baseComparator.compare(o1.getSubVector(halfDim, halfDim),
                o2.getSubVector(halfDim, halfDim));
    }
}
