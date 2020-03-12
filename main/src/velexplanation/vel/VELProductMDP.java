/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velexplanation.vel;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.NullState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import rabinizer.bdd.Globals;
import velexplanation.NonOOPropFunction;
import velexplanation.actions.WrapperActionType;
import velexplanation.mdp.MDPContainer;
import velexplanation.mdp.WrappedMDPContainer;
import velexplanation.misc.FormulaVar;

import java.util.*;

import static velexplanation.mdp.state.RecordedActionState.LAST_ACTION;

/**
 * @author dkasenberg
 */
public class VELProductMDP extends WrappedMDPContainer {
    public List<VELObjective> objectives;
    public Globals globals;
    public VELProductModel model;

    public VELProductMDP(MDPContainer mdp, List<VELObjective> objectives) {
        super(mdp);
        this.objectives = objectives;

        SADomain d = mdp.domain;

//        TODO check if objectives list is empty and respond accordingly
        this.globals = objectives.get(0).globals;

        SADomain newDomain;

        if (d instanceof OOSADomain) {
            newDomain = new OOSADomain();
            OOSADomain orig = (OOSADomain) d;
            for (PropositionalFunction pf : orig.propFunctions()) {
                ((OOSADomain) newDomain).addPropFunction(pf);
            }
            // TODO add state classes somehow?
        } else {
            throw new RuntimeException("Need an OOMDP for product functionality at this point");
        }

        d.getActionTypes().stream().forEach(aT -> {
            newDomain.addActionType(new VELActionType(aT));
        });


        FactoredModel fm = (FactoredModel) d.getModel();

        this.model = new VELProductModel(fm.getStateModel(), objectives, this);

        newDomain.setModel(new FactoredModel(this.model, fm.getRf(), new VELProductTF(fm.getTf())));
        SeeFirstStateActionType a = new SeeFirstStateActionType(initialState);
        newDomain.addActionType(a);
        this.domain = newDomain;

        List<VELObjectiveState> objectiveStates = new ArrayList<>();
        for (VELObjective objective : objectives) {
            VELObjectiveState initObjectiveState = new VELObjectiveState(objective);
            Set<Map<String, String>> allGroundings = objective.getAllGroundings(initialState);
            for (Map<String, String> grounding : allGroundings) {
                initObjectiveState.groundingsToFSM.put(grounding, 0);
            }
            objectiveStates.add(initObjectiveState);
        }

        this.initialState = new VELProductState(NullState.instance,
                objectiveStates
        );
    }

    public boolean singlePropFunValuation(FormulaVar var, State s) {
        if (domain instanceof OOSADomain && ((OOSADomain) domain).propFunction(var.name) != null) {
            try {
                if (!(s instanceof OOState)) {
                    return ((NonOOPropFunction) ((OOSADomain) domain).propFunction(var.name)).isTrue(s);
                }
                return ((OOSADomain) domain).propFunction(var.name).isTrue((OOState) s, var.params);
            } catch (Exception e) {
                return false;
            }
        }
        try {

        } catch (Exception e) {

        }
        Action lastAction = (Action) s.get(LAST_ACTION);
//        TODO Will probably need to correct this based on new toString system
        return lastAction.toString() != null && lastAction.toString().equals((var.name + " " + String.join(" ", var.params)).trim());
    }

    public static class VELActionType extends WrapperActionType {

        public VELActionType(ActionType a) {
            super(a);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            if (s instanceof VELProductState && ((VELProductState) s).s instanceof NullState) {
                return Collections.emptyList();
            }
            return super.allApplicableActions(s);
        }
    }

    public static class SeeFirstStateActionType extends UniversalActionType {
        public static String ACTION_SEE_FIRST_STATE = "SEE_FIRST_STATE";
        public State initialState;

        public SeeFirstStateActionType(State initialState) {
            super(ACTION_SEE_FIRST_STATE);
            this.initialState = initialState;
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            if (s instanceof VELProductState && ((VELProductState) s).s instanceof NullState) return allActions;
            return new ArrayList<>();
        }

    }

    public static class VELProductTF implements TerminalFunction {
        protected TerminalFunction innerTF;

        public VELProductTF(TerminalFunction tf) {
            this.innerTF = tf;
        }

        public boolean terminal(State state) {
            if (((VELProductState) state).s instanceof NullState) {
                return false;
            }
            return innerTF.isTerminal(((VELProductState) state).s);
        }


        @Override
        public boolean isTerminal(State state) {
            boolean t = terminal(state);
            return t;
        }
    }
}
