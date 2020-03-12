package velexplanation.vel;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import rabinizer.bdd.Globals;
import rabinizer.formulas.*;
import velexplanation.NonOOPropFunction;
import velexplanation.formula.ParamPlaceholder;
import velexplanation.misc.Pair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dkasenberg on 4/17/18.
 */
public class VELObjective {


    private static List<String> unaryOperators = Arrays.asList("!");
    private static List<String> binaryOperators = Arrays.asList("&", "|");
    //    TODO refactor explanation, etc. so that these things don't have to be public
    public OOSADomain domain;
    public int totalStates;
    public Formula formula;
    public Map<Integer, Set<Pair<Formula, Integer>>> transitions;
    public Set<Integer> accStates;
    public Set<String> costlyVariables;
    public List<String> quantifiedVariables;
    public List<Boolean> quantificationTypes;
    public Log log = LogFactory.getLog(VELObjective.class);
    public boolean cosafe;
    public Map<String, String> freeVarClassMap;
    boolean durative;
    Globals globals;
    private Set<Pair<Integer, Formula>> costlyTransitions;
    private Map<String, Formula> propositionAliases;
    private Map<String, String> propAliasesReversed;

    public VELObjective(OOSADomain domain, Formula formula, Set<String> costlyVariables, List<String> quantifiedVariables,
                        List<Boolean> quantificationTypes, boolean durative, boolean init) {
//        TODO(dkasenberg) constrain this to not allow any unquantified variables.
        this.domain = domain;
        this.durative = durative;
        costlyTransitions = new HashSet<>();
        propositionAliases = new HashMap<>();
        propAliasesReversed = new HashMap<>();
        accStates = new HashSet<>();
        transitions = new HashMap<>();

        this.costlyVariables = new HashSet<>(costlyVariables);
        this.quantificationTypes = new ArrayList<>(quantificationTypes);
        this.quantifiedVariables = new ArrayList<>(quantifiedVariables);

        this.formula = formula;
        this.cosafe = false;
        if (init) {
            this.initialize();
        }
        freeVarClassMap = freeVarClasses(this.formula);
    }

    public VELObjective(OOSADomain domain, Formula formula, Set<String> costlyVariables, List<String> quantifiedVariables,
                        List<Boolean> quantificationTypes, boolean durative) {
        this(domain, formula, costlyVariables, quantifiedVariables, quantificationTypes, durative, true);

    }

    public VELObjective copy() {
        VELObjective objectiveCopy = new VELObjective(domain, formula, costlyVariables, quantifiedVariables, quantificationTypes, durative,
                false);
        objectiveCopy.totalStates = this.totalStates;
        objectiveCopy.transitions = this.transitions;
        objectiveCopy.accStates = this.accStates;
        objectiveCopy.costlyTransitions = costlyTransitions;
        objectiveCopy.durative = durative;
        objectiveCopy.propositionAliases = propositionAliases;
        objectiveCopy.propAliasesReversed = propAliasesReversed;
        objectiveCopy.cosafe = cosafe;
        return objectiveCopy;
    }

    //    Equality of objectives is equality of the formula and quantifications (and costly vars), up to a change of the
//    variable names (but not their classes).  There are various more complex semantic checks we could do (e.g.
//    reordering a set of consecutive quantifications of the same type doesn't change the semantics, changing the order
//    of ands doesn't change the semantics, etc) but it just seems like overkill to go that far with it.
//    TODO(dkasenberg) test this more thoroughly
    @Override
    public boolean equals(Object obj) {

        return obj instanceof VELObjective
                && formula.equals(((VELObjective) obj).formula)
                && costlyVariables.size() == ((VELObjective) obj).costlyVariables.size()
                && quantifiedVariables.size() == ((VELObjective) obj).quantifiedVariables.size()
                && quantificationTypes.equals(((VELObjective) obj).quantificationTypes)
                && costlyVariables.stream().map(var -> quantifiedVariablePaths(formula, var)).collect(Collectors.toSet())
                .equals(((VELObjective) obj).costlyVariables.stream().map(var -> quantifiedVariablePaths(((VELObjective) obj).formula,
                        var)).collect(Collectors.toSet()))
                && quantifiedVariables.stream().map(var -> quantifiedVariablePaths(formula, var)).collect(Collectors.toList())
                .equals(((VELObjective) obj).quantifiedVariables.stream().map(var -> quantifiedVariablePaths(((VELObjective) obj).formula,
                        var)).collect(Collectors.toList()))
                && durative == ((VELObjective) obj).durative;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{
                formula,
                costlyVariables.size(),
                quantifiedVariables.size(),
                quantificationTypes,
                costlyVariables.stream().map(var -> quantifiedVariablePaths(formula, var)).collect(Collectors.toSet()),
                quantifiedVariables.stream().map(var -> quantifiedVariablePaths(formula, var)).collect(Collectors.toList()),
                durative
        });
    }

    //    This method essentially takes a free variable in a formula and outputs all the formula-tree traversals that lead
//    to an occurrence of that variable.  The idea is that it'll produce a renaming-independent representation of a
    private Set<List<Integer>> quantifiedVariablePaths(Formula formula, String varName) {
        if (formula instanceof ParamPlaceholder) {
            ParamPlaceholder pp = (ParamPlaceholder) formula;
            if (pp.applied.containsKey(varName)) return Collections.emptySet();
            Set<List<Integer>> indices = new HashSet<>();
            for (int i = 0; i < pp.params.size(); i++) {
                if (pp.params.get(i).equals(varName)) indices.add(Collections.singletonList(i));
            }
            return indices;
        } else if (formula instanceof FormulaNullary) {
            return Collections.emptySet();
        } else if (formula instanceof FormulaUnary) {
            Set<List<Integer>> operandVariablePaths = quantifiedVariablePaths(((FormulaUnary) formula).operand, varName);
            return operandVariablePaths.stream().map(list -> {
                List<Integer> newList = new ArrayList<>(list);
                newList.add(0, 0);
                return newList;
            }).collect(Collectors.toSet());
        } else if (formula instanceof FormulaBinary) {
            Set<List<Integer>> leftVariablePaths = quantifiedVariablePaths(((FormulaBinary) formula).left, varName);
            Set<List<Integer>> rightVariablePaths = quantifiedVariablePaths(((FormulaBinary) formula).right, varName);
            Set<List<Integer>> toReturn = new HashSet<>(leftVariablePaths.stream().map(list -> {
                List<Integer> newList = new ArrayList<>(list);
                newList.add(0, 0);
                return newList;
            }).collect(Collectors.toSet()));
            toReturn.addAll(rightVariablePaths.stream().map(list -> {
                List<Integer> newList = new ArrayList<>(list);
                newList.add(0, 1);
                return newList;
            }).collect(Collectors.toSet()));
            return toReturn;
        }
        throw new RuntimeException("This kind of formula not supported.");
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (!costlyVariables.isEmpty()) {
            result.append("<");
            for (String varName : costlyVariables) {
                result.append(varName);
                result.append(",");
            }
            result.deleteCharAt(result.length() - 1);
            result.append("> ");
        }
        if (!quantifiedVariables.isEmpty()) {
            for (int i = 0; i < quantifiedVariables.size(); i++) {
                String varName = quantifiedVariables.get(i);
                boolean universal = quantificationTypes.get(i);

                result.append(universal ? "A" : "E");
                result.append(varName);
                result.append(" ");
            }
            result.deleteCharAt(result.length() - 1);
            result.append(": ");
        }
        result.append(formula.toString());
        return result.toString();
    }

    protected void initialize() {

//        Implicitly add X operators to action-based PFs; took down because explicit is probably better.
//        Formula formula = transformNullaries(this.formula, formula1 -> {
//            log.warn(formula1);
//            if(formula1 instanceof ParamPlaceholder &&
//                    domain.propFunction(((ParamPlaceholder) formula1).propName)
//                            instanceof RecordedActionMDP.RecordedActionPF) {
//                log.warn("This should be working1");
//                return new XOperator(formula1);
//            } else if(formula1 instanceof Literal &&
//                    domain.propFunction(((Literal) formula1).atom) instanceof RecordedActionMDP.RecordedActionPF) {
//                log.warn("This should be working2");
//                return new XOperator(formula1);
//            }
//            return formula1;
//        });
        buildTransitions(formula);
        if (durative) {
            makeDurative();
        }
    }

    protected String getSCheckObjectiveText(Formula formula) {
        String s = formula.toReversePolishString();
        for (String toReplace : propAliasesReversed.keySet()) {
            s = s.replaceAll(toReplace, propAliasesReversed.get(toReplace));
        }
        s = s.replaceAll("->", "i");
        return s;
    }

    protected void createAliasMap(Formula formula) {
        propositionAliases.clear();
        propAliasesReversed.clear();

        createAliasMap(formula, 0);
    }

    protected int createAliasMap(Formula formula, int propIndex) {
        if (formula instanceof FormulaUnary) {
            return createAliasMap(((FormulaUnary) formula).operand, propIndex);
        } else if (formula instanceof FormulaBinary) {
            int leftIndex = createAliasMap(((FormulaBinary) formula).left, propIndex);
            return createAliasMap(((FormulaBinary) formula).right, leftIndex);
        } else if (formula instanceof Literal || formula instanceof ParamPlaceholder) {
            if (propAliasesReversed.containsKey(formula.toString())) {
                return propIndex;
            }
            propositionAliases.put("p" + propIndex, formula);
            propAliasesReversed.put(formula.toString(), "p" + propIndex);
            return propIndex + 1;
        } else {
            return propIndex;
        }
    }

    protected String[] getSCheckAutomatonTokens(Formula safeFormula) throws SCheckAutomatonException {
        String text = getSCheckObjectiveText(safeFormula);

        StringBuffer output = new StringBuffer();
        Process p;
        try {

//            Use scheck2 to create the FSMs from the VEL formulas.
            String[] cmd = {
                    "/bin/sh",
                    "-c",
                    "echo \"" + text + "\" | scheck/scheck2 -d -s"
            };
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String automatonString = output.toString();

        String[] tokens = automatonString.split("\\s+");
        if (tokens.length <= 1) {
            throw new SCheckAutomatonException();
        }

        return tokens;
    }

    protected void buildTransitions(Formula formula) {
        createAliasMap(formula);

        String[] tokens;
        try {
            Formula safeNegation = VELCosafeToSafeConverter.cosafeToNotSafe(formula);

            tokens = getSCheckAutomatonTokens(safeNegation);
            this.cosafe = true;
        } catch (VELCosafeToSafeConverter.FormulaConversionException | SCheckAutomatonException e) {

            try {
                tokens = getSCheckAutomatonTokens(new Negation(formula));
            } catch (SCheckAutomatonException e1) {
                throw new RuntimeException("Formula is neither syntactically safe nor co-safe");
            }
        }

        totalStates = Integer.parseInt(tokens[0]);

        int tokenIndex = 2;
        int stateIndex = 0;

        Map<String, Integer> stateMap = new HashMap<>();

        Map<Integer, Set<Pair<Formula, String>>> tempTransitions = new HashMap<>();

        int statesConsidered = 0;
        while (statesConsidered++ < totalStates) {
            String tokenStateIndex = tokens[tokenIndex++];
            int curState = Integer.parseInt(tokens[tokenIndex++]) == 1 ? 0 : ++stateIndex;
            stateMap.put(tokenStateIndex, curState);
            if (Integer.parseInt(tokens[tokenIndex]) != -1) {
                accStates.add(curState);
                tokenIndex++;
            }
            tokenIndex++;
            Set<Pair<Formula, String>> outgoingTransitions = new HashSet<>();
//            This is where we add the transitions.
            while (Integer.parseInt(tokens[tokenIndex]) != -1) {
                String toState = tokens[tokenIndex++];
                Pair<BinaryTreeNode<String>, Integer> treeAndOffset = getBinaryTree(tokens, tokenIndex);
                tokenIndex = treeAndOffset.getRight();
                outgoingTransitions.add(new Pair<>(formulaFromBinaryTree(treeAndOffset.getLeft()), toState));
            }
            tempTransitions.put(curState, outgoingTransitions);
            tokenIndex++;
        }

        for (Map.Entry<Integer, Set<Pair<Formula, String>>> entry : tempTransitions.entrySet()) {
            int inState = entry.getKey();
            Set<Pair<Formula, Integer>> actualTransition = new HashSet<>();
            for (Pair<Formula, String> transition : entry.getValue()) {
                actualTransition.add(new Pair<>(transition.getLeft(), stateMap.get(transition.getRight())));
            }
            transitions.put(inState, actualTransition);
        }

        boolean addedSinkState = false;
        for (int incomingState = 0; incomingState < totalStates; incomingState++) {
            Formula disjn = new BooleanConstant(false, this.globals);
            for (Pair<Formula, Integer> transition : transitions.get(incomingState)) {
                disjn = new Disjunction(disjn, transition.getLeft());
            }
            Set<Set<Formula>> mintermFormulas = VELHelper.getMintermFormulas(new Negation(disjn));
            if (!mintermFormulas.isEmpty()) {
                Formula coverMissing = null;
                for (Set<Formula> formulas : mintermFormulas) {
                    if (formulas.isEmpty()) {
                        coverMissing = new BooleanConstant(true, this.globals);
                        break;
                    }
                    Formula runningConjunction = null;
                    for (Formula conjunctionComponent : formulas) {
                        runningConjunction = runningConjunction == null ? conjunctionComponent
                                : new Conjunction(runningConjunction, conjunctionComponent);
                    }
                    coverMissing = coverMissing == null ? runningConjunction
                            : new Disjunction(coverMissing, runningConjunction);
                }
                transitions.get(incomingState).add(new Pair<>(coverMissing, totalStates));
                addedSinkState = true;
            }
        }
        if (addedSinkState) {
            transitions.put(totalStates, Collections.singleton(new Pair<>(new BooleanConstant(true, this.globals),
                    totalStates)));
            totalStates++;
        }
    }

    public Formula formulaFromBinaryTree(BinaryTreeNode<String> tree) {
        if (tree.content.equals("!")) {
            return new Negation(formulaFromBinaryTree(tree.right));
        } else if (tree.content.equals("&")) {
            return new Conjunction(formulaFromBinaryTree(tree.left), formulaFromBinaryTree(tree.right));
        } else if (tree.content.equals("|")) {
            return new Disjunction(formulaFromBinaryTree(tree.left), formulaFromBinaryTree(tree.right));
        } else if (tree.content.equals("t")) {
            return new BooleanConstant(true, globals);
        } else if (tree.content.equals("f")) {
            return new BooleanConstant(false, globals);
        } else {
            return propositionAliases.get(tree.content).copy();
        }
    }

    public Pair<BinaryTreeNode<String>, Integer> getBinaryTree(String[] tokens, int offset) {
        String rootString = tokens[offset];
        BinaryTreeNode<String> toReturn = new BinaryTreeNode(rootString);
        if (binaryOperators.contains(rootString)) {
            Pair<BinaryTreeNode<String>, Integer> leftSubtreeAndOffset = getBinaryTree(tokens, offset + 1);
            toReturn.left = leftSubtreeAndOffset.getLeft();
            Pair<BinaryTreeNode<String>, Integer> rightSubtreeAndOffset =
                    getBinaryTree(tokens, leftSubtreeAndOffset.getRight());
            toReturn.right = rightSubtreeAndOffset.getLeft();
            return new Pair<>(toReturn, rightSubtreeAndOffset.getRight());
        } else if (unaryOperators.contains(rootString)) {
            Pair<BinaryTreeNode<String>, Integer> rightSubtreeAndOffset = getBinaryTree(tokens, offset + 1);
            toReturn.right = rightSubtreeAndOffset.getLeft();
            return new Pair<>(toReturn, rightSubtreeAndOffset.getRight());
        } else {
            return new Pair<>(toReturn, offset + 1);
        }
    }

    public VELObjectiveState nextState(VELObjectiveState origState, State mdpState) {
        VELObjectiveState toReturn = new VELObjectiveState(origState.velObjective);

        for (Map<String, String> grounding : origState.groundingsToFSM.keySet()) {

            int origStateForGrounding = origState.groundingsToFSM.get(grounding);

            for (Pair<Formula, Integer> transition : transitions.get(origStateForGrounding)) {
                Formula groundedFormula = transition.getLeft().applyGrounding(grounding);
                if (evaluatesToTrue(groundedFormula, mdpState)) {
                    toReturn.groundingsToFSM.put(grounding, transition.getRight());
                    break;
                }
            }
        }
        return toReturn;
    }

    public Set<Map<String, String>> getDurativeCostlyGroundings(VELObjectiveState origState, OOState mdpState) {
        Set<Map<String, String>> groundings = new HashSet<>();
        for (Map<String, String> grounding : origState.groundingsToFSM.keySet()) {

            int origStateForGrounding = origState.groundingsToFSM.get(grounding);

            for (Pair<Formula, Integer> transition : transitions.get(origStateForGrounding)) {
                Formula formula = transition.getLeft();
                if (costlyTransitions.contains(new Pair<>(origStateForGrounding, formula))) {
                    Formula groundedFormula = formula.applyGrounding(grounding);
                    if (evaluatesToTrue(groundedFormula, mdpState)) {
                        groundings.add(grounding);
                    }
                }
            }
        }
        return groundings;
    }

    public void makeDurative() {

        for (Map.Entry<Integer, Set<Pair<Formula, Integer>>> transitionEntry : transitions.entrySet()) {
            Set<Pair<Formula, Integer>> newTransitions = new HashSet<>();
            for (Pair<Formula, Integer> p : transitionEntry.getValue()) {
                if (accStates.contains(p.getRight())) {
                    newTransitions.add(new Pair<>(p.getLeft(), transitionEntry.getKey()));
                    costlyTransitions.add(new Pair<>(transitionEntry.getKey(), p.getLeft()));
                } else {
                    newTransitions.add(p);
                }
            }
            transitions.put(transitionEntry.getKey(), newTransitions);
        }
    }

    public Set<Map<String, String>> filterCostly(Set<Map<String, String>> origMappings, Set<String> costly) {
        Set<Map<String, String>> filteredSet = new HashSet<>();
        for (Map<String, String> mapping : origMappings) {
            Map<String, String> newMapping = new HashMap<>(mapping);
            newMapping.keySet().retainAll(costly);
            filteredSet.add(newMapping);
        }
        return filteredSet;
    }

    public double groundingRelatedCost(VELProductState productState) {
        return groundingRelatedCost(productState.objectiveStates.stream().filter(ns -> ns.velObjective == this).findAny().get(),
                productState.s);
    }

    public double groundingRelatedCost(VELObjectiveState objectiveState, State s) {
        return getQuantifiedSubgroundings(objectiveState, s).size();
    }

    public Set<Map<String, String>> getQuantifiedSubgroundings(VELObjectiveState objectiveState, State s) {
        Set<Map<String, String>> accGroundings = getAcceptingGroundings(objectiveState);

        Map<Map<String, String>, Set<Map<String, String>>> partition = groundingPartition(accGroundings, costlyVariables);
        return partition.entrySet().stream()
                .filter(e -> satisfiesQuantifiers(e.getValue(), s, 0))
                .map(e -> e.getKey()).collect(Collectors.toSet());
    }

    public boolean satisfiesQuantifiers(Set<Map<String, String>> groundings, State s, int startIndex) {
        if (startIndex == quantifiedVariables.size()) {
//            TODO correct this to return the correct value.
            return true;
        }
        if (groundings.isEmpty()) {
            return false;
        }
        String quantVar = quantifiedVariables.get(startIndex);
        Map<Map<String, String>, Set<Map<String, String>>> partition = groundingPartition(groundings, Collections.singleton(quantVar));
        if (quantificationTypes.get(startIndex) ^ !this.cosafe) {
// If universal quantification: partition on first variable.
//            Must have all possible values of first variable, and each partition must satisfy the sublist.
            return partition.keySet().size() == ((OOState) s).objectsOfClass(freeVarClassMap.get(quantVar)).size() &&
                    partition.values().stream().allMatch(partitionValue -> satisfiesQuantifiers(partitionValue,
                            s, startIndex + 1));
        } else {
            //        If existential quantification: partition on first variable.
// If the partition is non-empty and contains one set that satisfies the sublist, return true.
            return partition.values().stream().anyMatch(
                    partitionValue -> satisfiesQuantifiers(partitionValue, s, startIndex + 1));
        }
    }

    public Map<Map<String, String>, Set<Map<String, String>>> groundingPartition(Set<Map<String, String>> allGroundings, Set<String> partitionVars) {

        Map<Map<String, String>, Set<Map<String, String>>> partition = new HashMap<>();
        for (Map<String, String> grounding : allGroundings) {
            Map<String, String> partitionValue = new HashMap<>(grounding);
            Map<String, String> partitionKey = new HashMap<>();
            for (String var : partitionVars) {
                partitionKey.put(var, grounding.get(var));
                partitionValue.remove(var);
            }
            Set<Map<String, String>> partitionValues =
                    partition.containsKey(partitionKey) ? partition.get(partitionKey) : new HashSet<>();
            partitionValues.add(partitionValue);
            partition.put(partitionKey, partitionValues);
        }
        return partition;
    }

    public double durativeCost(VELObjectiveState origState, State mdpState) {
        return filterCostly(getDurativeCostlyGroundings(origState, (OOState) mdpState), costlyVariables).size();
    }

    // TODO replace this with bdd stuff, and put it INTO the formula classes.  This is bad for encapsulation.
    public boolean evaluatesToTrue(Formula formula, State mdpState) {
        if (formula instanceof ParamPlaceholder) {
            throw new RuntimeException("Formula is not grounded.");
        } else if (formula instanceof UOperator
                || formula instanceof XOperator
                || formula instanceof GOperator
                || formula instanceof FOperator) {
            throw new RuntimeException("Formula must not contain temporal operators.");
        } else if (formula instanceof BooleanConstant) {
            return ((BooleanConstant) formula).value;
        } else if (formula instanceof Conjunction) {
            return evaluatesToTrue(((FormulaBinary) formula).left, mdpState) &&
                    evaluatesToTrue(((FormulaBinary) formula).right, mdpState);
        } else if (formula instanceof Disjunction) {
            return evaluatesToTrue(((FormulaBinary) formula).left, mdpState) ||
                    evaluatesToTrue(((FormulaBinary) formula).right, mdpState);
        } else if (formula instanceof FormalNegation) {
            return !evaluatesToTrue(((FormulaUnary) formula).operand, mdpState);
        } else if (formula instanceof Implication) {
            return evaluatesToTrue(((FormulaBinary) formula).right, mdpState)
                    || !evaluatesToTrue(((FormulaBinary) formula).left, mdpState);
        } else if (formula instanceof Negation) {
            return !evaluatesToTrue(((FormulaUnary) formula).operand, mdpState);
        } else if (formula instanceof Literal) {
            return singlePropFunValuation(new FormulaVar(((Literal) formula).atom), mdpState);
        }
        throw new RuntimeException("Formula cannot be evaluated.");
    }

    public Set<Map<String, String>> getAcceptingGroundings(VELObjectiveState objectiveState) {
        return objectiveState.groundingsToFSM.entrySet().stream().filter(e -> accStates.contains(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Set<Map<String, String>> getNonAcceptingGroundings(VELObjectiveState objectiveState) {
        return objectiveState.groundingsToFSM.entrySet().stream().filter(e -> !accStates.contains(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Map<String, String> freeVarClasses(Formula formula) {
        if (formula instanceof FormulaBinary) {
            Map<String, String> leftVarClasses = new HashMap<>(freeVarClasses(((FormulaBinary) formula).left));
            Map<String, String> rightVarClasses = freeVarClasses(((FormulaBinary) formula).right);
            leftVarClasses.putAll(rightVarClasses);
            return leftVarClasses;
        } else if (formula instanceof FormulaUnary) {
            return freeVarClasses(((FormulaUnary) formula).operand);
        } else if (formula instanceof ParamPlaceholder) {

            String[] paramClasses = domain.propFunction(((ParamPlaceholder) formula).propName).getParameterClasses();
//            TODO Right now we're not watching for conflicts.  We probably should.
            Map<String, String> toReturn = new HashMap<>();
            for (int i = 0; i < paramClasses.length; i++) {
                if (((ParamPlaceholder) formula).applied.containsKey(((ParamPlaceholder) formula).params.get(i))) {
                    continue;
                }
                toReturn.put(((ParamPlaceholder) formula).params.get(i), paramClasses[i]);
            }
            return toReturn;
        }
        return Collections.emptyMap();
    }

    //    Of course, this sucker runs in exponential time (makes sense, since it is computing a power set.
    public <S, T> Set<Map<S, T>> separateMap(Map<S, Set<T>> origMap) {
        if (origMap.isEmpty()) {
            return Collections.singleton(Collections.emptyMap());
        }
        S key = origMap.keySet().stream().findAny().get();
        Set<T> values = origMap.get(key);


        Map<S, Set<T>> origCopy = new HashMap<>(origMap);
        origCopy.remove(key);

        Set<Map<S, T>> inductive = separateMap(origCopy);

        Set<Map<S, T>> toReturn = new HashSet<>();
        for (T value : values) {
            if (inductive.isEmpty()) {
                Map<S, T> toAdd = Collections.singletonMap(key, value);
                toReturn.add(toAdd);
            } else {
                for (Map<S, T> submap : inductive) {
                    Map<S, T> submapCopy = new HashMap<>(submap);

                    submapCopy.put(key, value);
                    toReturn.add(submapCopy);
                }
            }
        }
        return toReturn;
    }

    //    Will probably change this when we create the actual language itself with its own formulas that convert.
    public Set<Map<String, String>> getAllGroundings(State s0) {
        if (!(s0 instanceof OOState)) {
            return Collections.singleton(Collections.emptyMap());
        }
        OOState s = (OOState) s0;
        Map<String, String> allFreeVarClasses = new HashMap<>();
        for (Map.Entry<Integer, Set<Pair<Formula, Integer>>> transitionEntry : transitions.entrySet()) {
            for (Pair<Formula, Integer> p : transitionEntry.getValue()) {
                allFreeVarClasses.putAll(freeVarClasses(p.getLeft()));
            }
        }
        Map<String, Set<String>> allApplicableBindings = new HashMap<>();
        for (Map.Entry<String, String> varClass : allFreeVarClasses.entrySet()) {
//            Creating a map that maps each variable to the names of all objects in the relevant class.
            allApplicableBindings.put(varClass.getKey(),
                    s.objectsOfClass(varClass.getValue()).stream().map(oi -> oi.name()).collect(Collectors.toSet()));
        }
        return separateMap(allApplicableBindings);
    }

    public boolean singlePropFunValuation(FormulaVar var, State s) {
        if (domain.propFunction(var.name) != null) {
            try {
                if (!(s instanceof OOState)) {
                    return ((NonOOPropFunction) domain.propFunction(var.name)).isTrue(s);
                }
                return domain.propFunction(var.name).isTrue((OOState) s, var.params);
            } catch (Exception e) {
                return false;
            }
        }
        throw new RuntimeException("Propositional function '" + var.name + "' not found.");
    }

    public static class SCheckAutomatonException extends Exception {
    }

    public static class FormulaVar {
        public String name;
        public String[] params;

        public FormulaVar(String origName) {
            String[] v = origName.split("[:,]");
            name = v[0];
            params = Arrays.copyOfRange(v, 1, v.length);
        }

        @Override
        public String toString() {
            return name + "(" + String.join(",", params) + ")";
        }

    }

    public class BinaryTreeNode<T> {
        T content;
        BinaryTreeNode<T> left;
        BinaryTreeNode<T> right;

        public BinaryTreeNode(T content) {
            this.content = content;
            this.left = null;
            this.right = null;
        }

    }

}
