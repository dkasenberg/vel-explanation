/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rabinizer.automata;

import rabinizer.exec.Tuple;

import java.util.Map;

/**
 * @author jkretinsky
 */
public class ProductDegenState extends Tuple<ProductState, Map<Integer, Integer>> {

    public ProductDegenState(ProductState ps, Map<Integer, Integer> awaitedIndices) {
        super(ps, awaitedIndices);
    }

    @Override
    public String toString() {
        return left + " " + right;
    }

}
