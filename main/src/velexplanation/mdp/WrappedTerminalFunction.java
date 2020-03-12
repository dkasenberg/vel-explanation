package velexplanation.mdp;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import velexplanation.mdp.state.WrapperState;

/**
 * Created by dkasenberg on 11/13/17.
 */
public class WrappedTerminalFunction implements TerminalFunction {
    public TerminalFunction unwrappedTF;

    public WrappedTerminalFunction(TerminalFunction unwrappedTF) {
        this.unwrappedTF = unwrappedTF;
    }

    @Override
    public boolean isTerminal(State state) {
        try {
            return unwrappedTF.isTerminal(state);
        } catch (Exception e) {
            if (state instanceof WrapperState) {
                return isTerminal(((WrapperState) state).s);
            } else {
                return false;
            }
        }
    }

}
