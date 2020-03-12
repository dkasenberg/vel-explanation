/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velexplanation.mdp;


import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import velexplanation.actions.ObjectParameterizedActionType;
import velexplanation.mdp.model.RecordedActionModel;
import velexplanation.mdp.state.RecordedActionState;

import static velexplanation.mdp.state.RecordedActionState.LAST_ACTION;

/**
 * @author dkasenberg
 */
public class RecordedActionMDP extends WrappedMDPContainer {

    public RecordedActionMDP(MDPContainer mdp) {
        super(mdp);
        this.domain = recordActionsOnDomain(mdp.domain);
        this.initialState = new RecordedActionState(mdp.initialState, null);
    }

    public SADomain recordActionsOnDomain(SADomain d) {

        if (mdp instanceof RecordedActionMDP) return d;

        SADomain newDomain;

        if (d instanceof OOSADomain) {
            newDomain = new OOSADomain();
            OOSADomain orig = (OOSADomain) d;
            for (PropositionalFunction pf : orig.propFunctions()) {
                ((OOSADomain) newDomain).addPropFunction(pf);
            }

            for (ActionType atype : d.getActionTypes()) {
                ((OOSADomain) newDomain).addPropFunction(new RecordedActionPF(atype));
            }
            // TODO add state classes somehow?
        } else {
            newDomain = new SADomain();
        }


        newDomain.addActionTypes(d.getActionTypes().toArray(new ActionType[0]));

        FactoredModel fm = (FactoredModel) d.getModel();

        newDomain.setModel(new FactoredModel(new RecordedActionModel(fm.getStateModel()), fm.getRf(), fm.getTf()));

        return newDomain;
    }

    @Override
    public void setInitialState(State initialState) {
        super.setInitialState(new RecordedActionState(initialState, null));
    }

    //    NB this will break for non-OO actions, etc.
    public static class RecordedActionPF extends PropositionalFunction {

        protected ActionType atype;
        protected boolean isParameterized;

        public RecordedActionPF(ActionType atype) {
            this(atype, "did" + atype.typeName());
        }

        public RecordedActionPF(ActionType atype, String name) {
            super(name,
                    atype instanceof ObjectParameterizedActionType ?
                            ((ObjectParameterizedActionType) atype).getParameterClasses() : new String[0]);
            this.atype = atype;
            this.isParameterized = this.atype instanceof ObjectParameterizedActionType;
        }

        @Override
        public boolean isTrue(OOState ooState, String... strings) {
            try {
                Action lastAction = (Action) ooState.get(LAST_ACTION);
                if (!isParameterized) {
                    return lastAction.equals(atype.associatedAction(""));
                } else if (((ObjectParameterizedActionType) atype).getParameterClasses().length == 0) {
                    return lastAction
                            .equals(new ObjectParameterizedActionType.SAObjectParameterizedAction(atype.typeName(),
                                    new String[0]));
                }
                return lastAction.equals(atype.associatedAction(String.join(" ", strings).trim()));
            } catch (Exception e) {
                throw new RuntimeException("Attempting to call RecordedActionPF for state w/o recorded action.");
            }
        }
    }
}
