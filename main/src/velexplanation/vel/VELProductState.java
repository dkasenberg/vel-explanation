package velexplanation.vel;

import burlap.mdp.core.state.State;
import velexplanation.mdp.state.WrapperState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by dkasenberg on 5/21/18.
 */
public class VELProductState extends WrapperState {

    public List<VELObjectiveState> objectiveStates;


    public VELProductState(State origState, List<VELObjectiveState> objectiveStates
    ) {
        super(origState);
        this.objectiveStates = new ArrayList<>(objectiveStates);
    }

    @Override
    public List<Object> uniqueKeys() {
        return IntStream.range(0, objectiveStates.size()).mapToObj(i -> i).collect(Collectors.toList());
    }

    @Override
    public Object get(Object variableKey) {
        if (variableKey instanceof Integer) {
            int key = (int) variableKey;
            return objectiveStates.get(key);
        }
        return s.get(variableKey);
    }

    @Override
    public State copy() {
        return new VELProductState(s.copy(),
                new ArrayList<>(objectiveStates.stream().map(os -> os.copy()).collect(Collectors.toList())));
    }

    @Override
    public String toString() {
        String str = "ProductState(\n" + s.toString() + "\n,\n";
        for (int i = 0; i < objectiveStates.size(); i++) {
            if (i != 0) str = str + ",";
            str = str + objectiveStates.get(i);
        }
        return str + ")";
    }
}
