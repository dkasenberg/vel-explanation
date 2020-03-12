package velexplanation.mdp;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.model.statemodel.SampleStateModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import velexplanation.mdp.state.RecordedActionState;
import velexplanation.mdp.state.WrapperState;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static velexplanation.mdp.TerminatingActionMDP.TerminationWrapperState.VAR_TERMINATED;

public class TerminatingActionMDP extends MDPContainer {
    private boolean actionRecord;

    public TerminatingActionMDP(MDPContainer mdp) {
        this.actionRecord = (mdp instanceof RecordedActionMDP);
        this.hashingFactory = mdp.hashingFactory;
        this.domain = addTerminateAction(mdp.domain);
        if (actionRecord) {
            this.initialState = mdp.initialState;
        } else {
            this.initialState = new TerminationWrapperState(mdp.initialState, false);
        }


    }

    public SADomain addTerminateAction(SADomain d) {

        SADomain newDomain;

        if (d instanceof OOSADomain) {
            newDomain = new OOSADomain();
            OOSADomain orig = (OOSADomain) d;
            for (PropositionalFunction pf : orig.propFunctions()) {
                ((OOSADomain) newDomain).addPropFunction(pf);
            }

        } else {
            newDomain = new SADomain();
        }


        newDomain.addActionTypes(d.getActionTypes().toArray(new ActionType[0]));

        ActionType terminateActionType = new UniversalActionType("TERMINATE");
        newDomain.addActionType(terminateActionType);


        ((OOSADomain) newDomain).addPropFunction(actionRecord ?
                new RecordedActionMDP.RecordedActionPF(terminateActionType, VAR_TERMINATED) : new TerminatedPF());

        FactoredModel fm = (FactoredModel) d.getModel();

        TerminalFunction origTF = fm.getTf();

//        Should already be a recorded-action state, so we can assume actions are recorded

        TerminalFunction newTF = new TerminalFunction() {
            @Override
            public boolean isTerminal(State state) {
                return (origTF != null && origTF.isTerminal(state)) || ((OOSADomain) newDomain)
                        .propFunction(VAR_TERMINATED)
                        .isTrue((OOState) state);
            }
        };

        FactoredModel newFM = new FactoredModel(new TerminatingActionModel(fm.getStateModel()), fm.getRf(), newTF);

        newDomain.setModel(newFM);

        return newDomain;
    }

    private static class TerminatedPF extends PropositionalFunction {

        public TerminatedPF(String name) {
            super(name, new String[0]);
        }

        public TerminatedPF() {
            this(VAR_TERMINATED);
        }

        @Override
        public boolean isTrue(OOState ooState, String... strings) {
            return StateUtilities.stringOrBoolean(ooState.get(VAR_TERMINATED));
        }
    }

    static class TerminationWrapperState extends WrapperState {

        public static String VAR_TERMINATED = "terminated";
        private boolean terminated;

        public TerminationWrapperState(State initialState, boolean b) {
            this.s = initialState.copy();
            this.terminated = b;
        }

        @Override
        public Object get(Object variableKey) {
            if (variableKey.equals(VAR_TERMINATED)) {
                return terminated;
            }
            return super.get(variableKey);
        }

        @Override
        public List<Object> uniqueKeys() {
            return Collections.singletonList(VAR_TERMINATED);
        }

        @Override
        public State copy() {
            return new TerminationWrapperState(this.s, this.terminated);
        }
    }

    public class TerminatingActionModel implements FullStateModel {

        public SampleStateModel model;

        public TerminatingActionModel(SampleStateModel model) {
            this.model = model;
        }

        @Override
        public List<StateTransitionProb> stateTransitions(State s, Action a) {
            if (actionRecord) {
                RecordedActionState st = (RecordedActionState) s;
                if (a.actionName().equals("TERMINATE")) {
                    return Collections.singletonList(new StateTransitionProb(new RecordedActionState(st.s, a), 1.));
                }
                return ((FullStateModel) model).stateTransitions(s, a);
            } else {
                TerminationWrapperState tws = (TerminationWrapperState) s;
                if (a.actionName().equals("TERMINATE")) {
                    return Collections.singletonList(new StateTransitionProb(new TerminationWrapperState(tws.s, true), 1.));
                }
                return ((FullStateModel) model).stateTransitions(tws.s, a).stream()
                        .map(tp -> new StateTransitionProb(new TerminationWrapperState(tp.s, false), tp.p))
                        .collect(Collectors.toList());
            }


        }

        @Override
        public State sample(State s, Action a) {
            return Helper.sampleByEnumeration(this, s, a);
        }
    }
}
