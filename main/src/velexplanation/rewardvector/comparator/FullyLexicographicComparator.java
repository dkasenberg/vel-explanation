/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velexplanation.rewardvector.comparator;

import org.apache.commons.math3.linear.RealVector;

import java.util.Comparator;

/**
 * @author dkasenberg
 */
public class FullyLexicographicComparator implements Comparator<RealVector> {

    @Override
    public int compare(RealVector r1, RealVector r2) {

        for (int i = r1.getDimension() - 1; i >= 0; i--) {
            if (r1.getEntry(i) > r2.getEntry(i)) return 1;
            else if (r1.getEntry(i) < r2.getEntry(i)) return -1;
        }
        return 0;
    }

}
