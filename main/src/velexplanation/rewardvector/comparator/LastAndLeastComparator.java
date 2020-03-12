package velexplanation.rewardvector.comparator;

import org.apache.commons.math3.linear.RealVector;

import java.util.Comparator;

public class LastAndLeastComparator implements Comparator<RealVector> {

    private Comparator<RealVector> baseComparator;

    public LastAndLeastComparator(Comparator<RealVector> base) {
        this.baseComparator = base;
    }

    @Override
    public int compare(RealVector o1, RealVector o2) {
        int baseCompared = baseComparator.compare(o1.getSubVector(0, o1.getDimension() - 1), o2.getSubVector(0, o2.getDimension() - 1));
        return baseCompared == 0 ? Double.compare(o1.getEntry(o1.getDimension() - 1),
                o2.getEntry(o2.getDimension() - 1))
                : baseCompared;
    }
}
