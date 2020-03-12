package velexplanation.vel;

import rabinizer.formulas.*;
import velexplanation.formula.ParamPlaceholder;

import java.util.*;
import java.util.stream.Collectors;

public class VELHelper {
    public static <T> Set<Set<T>> powerSet(Set<T> origSet) {
        if (origSet.isEmpty()) {
            return Collections.singleton(origSet);
        } else {
            T first = new ArrayList<>(origSet).get(0);
            Set<Set<T>> retVal = new HashSet<>();
            Set<T> withoutFirst = new HashSet<>(origSet);
            withoutFirst.remove(first);
            Set<Set<T>> withoutFirstPowerSet = powerSet(withoutFirst);
            for (Set<T> subset : withoutFirstPowerSet) {
                retVal.add(subset);
                Set<T> subsetCopy = new HashSet<>(subset);
                subsetCopy.add(first);
                retVal.add(subsetCopy);
            }
            return retVal;
        }
    }

    public static Set<String> getAllParams(Formula formula) {
        if (formula instanceof ParamPlaceholder) {
            return new HashSet<>(((ParamPlaceholder) formula).params);
        } else if (formula instanceof FormulaNullary) {
            return Collections.emptySet();
        } else if (formula instanceof FormulaUnary) {
            return getAllParams(((FormulaUnary) formula).operand);
        } else if (formula instanceof FormulaBinary) {
            Set<String> retVal = new HashSet<>(getAllParams(((FormulaBinary) formula).left));
            retVal.addAll(getAllParams(((FormulaBinary) formula).right));
            return retVal;
        }
        throw new RuntimeException("Unknown formula type - cannot get all params");
    }

    public static Map<String, Formula> getAllLiterals(Formula formula) {
        if (formula instanceof Literal) {
            return Collections.singletonMap(((Literal) formula).atom, formula);
        } else if (formula instanceof ParamPlaceholder) {
            return Collections.singletonMap(formula.toString(), formula);
        } else if (formula instanceof FormulaNullary) {
            return Collections.emptyMap();
        } else if (formula instanceof FormulaUnary) {
            return getAllLiterals(((FormulaUnary) formula).operand);
        } else if (formula instanceof FormulaBinary) {
            Map<String, Formula> retVal = new HashMap<>(getAllLiterals(((FormulaBinary) formula).left));
            retVal.putAll(getAllLiterals(((FormulaBinary) formula).right));
            return retVal;
        }
        throw new RuntimeException("Unknown formula type - cannot get all literals");
    }

    private static boolean settingSatisfiesFormula(int literalSetting, List<String> literals, Formula formula) {
        if (formula instanceof BooleanConstant) {
            return ((BooleanConstant) formula).value;
        } else if (formula instanceof Literal) {
            return (literalSetting >> literals.indexOf(((Literal) formula).atom) & 1) != 0;
        } else if (formula instanceof ParamPlaceholder) {
            return (literalSetting >> literals.indexOf(formula.toString()) & 1) != 0;
        } else if (formula instanceof Conjunction) {
            return settingSatisfiesFormula(literalSetting, literals, ((FormulaBinary) formula).left) &&
                    settingSatisfiesFormula(literalSetting, literals, ((FormulaBinary) formula).right);
        } else if (formula instanceof Disjunction) {
            return settingSatisfiesFormula(literalSetting, literals, ((FormulaBinary) formula).left) ||
                    settingSatisfiesFormula(literalSetting, literals, ((FormulaBinary) formula).right);
        } else if (formula instanceof FormalNegation) {
            return !settingSatisfiesFormula(literalSetting, literals, ((FormulaUnary) formula).operand);
        } else if (formula instanceof Implication) {
            return settingSatisfiesFormula(literalSetting, literals, ((FormulaBinary) formula).right)
                    || !settingSatisfiesFormula(literalSetting, literals, ((FormulaBinary) formula).left);
        } else if (formula instanceof Negation) {
            return !settingSatisfiesFormula(literalSetting, literals, ((FormulaUnary) formula).operand);
        }
        throw new RuntimeException("Unknown formula type");
    }

    public static Set<Set<Formula>> getMintermFormulas(Formula formula) {

        Map<String, Formula> literalMap = getAllLiterals(formula);
        List<String> literals = new ArrayList<>(literalMap.keySet());

        Map<Integer, List<Minterm>> minterms = new HashMap<>();
        Set<Minterm> allMinterms = new HashSet<>();

        for (int literalSetting = 0; literalSetting < (1 << literals.size()); literalSetting++) {
            if (settingSatisfiesFormula(literalSetting, literals, formula)) {
                int bitCount = Integer.bitCount(literalSetting);
                Minterm minterm = new Minterm(literalSetting, literals);
                allMinterms.add(minterm);
                if (minterms.containsKey(bitCount)) {
                    minterms.get(bitCount).add(minterm);
                } else {
                    minterms.put(bitCount, new ArrayList<>(Collections.singletonList(
                            minterm
                    )));
                }
            }
        }

        Map<Integer, List<Minterm>> newMinterms = new HashMap<>(minterms);

        while (!newMinterms.isEmpty()) {
            Map<Integer, List<Minterm>> prevMinterms = newMinterms;
            newMinterms = new HashMap<>();
            for (int bitCount = 0; bitCount < literals.size(); bitCount++) {
                if (!prevMinterms.containsKey(bitCount)) continue;
                for (Minterm minterm : prevMinterms.get(bitCount)) {
                    if (!prevMinterms.containsKey(bitCount + 1)) continue;
                    for (Minterm minterm2 : prevMinterms.get(bitCount + 1)) {
                        if (minterm.countDifference(minterm2) != 1) {
                            continue;
                        }
                        Minterm unified = minterm.unify(minterm2);
                        allMinterms.add(unified);
                        int newBitcount = unified.bitCount();
                        if (newMinterms.containsKey(newBitcount)) {
                            newMinterms.get(newBitcount).add(unified);
                        } else {
                            newMinterms.put(newBitcount, new ArrayList<>(Collections.singletonList(
                                    unified
                            )));
                        }
                    }
                }
            }
        }

        List<Minterm> primeMinterms = new ArrayList<>(allMinterms.stream().filter(minterm -> minterm.prime)
                .collect(Collectors.toSet()));
        primeMinterms.sort(Comparator.comparing(Minterm::dashCount));

        Set<Set<Formula>> allMintermFormulas = new HashSet<>();
        for (Minterm m : primeMinterms) {
            Set<Formula> mintermFormulas = new HashSet<>();
            for (int i = 0; i < m.options.length; i++) {
                if (m.options[i] == MintermOptions.DASH) continue;
                else if (m.options[i] == MintermOptions.ONE) {
                    mintermFormulas.add(literalMap.get(literals.get(i)).copy());
                } else {
                    mintermFormulas.add(new Negation(literalMap.get(literals.get(i)).copy()));
                }
            }
            allMintermFormulas.add(mintermFormulas);
        }
//        Can assume that each formula is top-level disjunctions followed
        return allMintermFormulas;

    }

    public enum MintermOptions {
        ONE, ZERO, DASH
    }

    public static class Minterm {
        MintermOptions[] options;
        boolean prime;

        public Minterm(MintermOptions[] options) {
            this.options = options;
            this.prime = true;
        }

        public Minterm(int literalSetting, List<String> literals) {
            this.options = intToMintermOptions(literalSetting, literals);
            this.prime = true;
        }

        private MintermOptions[] intToMintermOptions(int literalSetting, List<String> literals) {
            MintermOptions[] retVal = new MintermOptions[literals.size()];
            for (int bitPosition = 0; bitPosition < literals.size(); bitPosition++) {
                retVal[bitPosition] = ((literalSetting >> bitPosition) & 1) == 1 ? MintermOptions.ONE : MintermOptions.ZERO;
            }
            return retVal;
        }

        public Minterm unify(Minterm other) {
            MintermOptions[] retVal = new MintermOptions[options.length];
            for (int i = 0; i < options.length; i++) {
                if (options[i] != other.options[i]) {
                    retVal[i] = MintermOptions.DASH;
                } else {
                    retVal[i] = options[i];
                }
            }
            this.prime = false;
            other.prime = false;
            return new Minterm(retVal);
        }

        public int countDifference(Minterm other) {
            int mintermDifference = 0;
            for (int i = 0; i < this.options.length; i++) {
                if (this.options[i] != other.options[i]) mintermDifference++;
            }
            return mintermDifference;
        }

        public int dashCount() {
            int count = 0;
            for (int i = 0; i < this.options.length; i++) {
                if (this.options[i] == MintermOptions.DASH) count++;
            }
            return count;
        }

        public int bitCount() {
            int count = 0;
            for (int i = 0; i < this.options.length; i++) {
                if (this.options[i] == MintermOptions.ONE) count++;
            }
            return count;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.options);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Minterm &&
                    Arrays.equals(this.options, ((Minterm) obj).options);
        }
    }
}
