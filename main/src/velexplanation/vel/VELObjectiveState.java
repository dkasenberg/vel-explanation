package velexplanation.vel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dkasenberg on 4/17/18.
 */
public class VELObjectiveState {

    public VELObjective velObjective;
    public Map<Map<String, String>, Integer> groundingsToFSM;


    public VELObjectiveState(VELObjective velObjective) {
        this.velObjective = velObjective;
        this.groundingsToFSM = new HashMap<>();
    }

    public VELObjectiveState(VELObjective velObjective, Map<Map<String, String>, Integer> groundings) {
        this.velObjective = velObjective;
        this.groundingsToFSM = groundings;
    }

    @Override
    public int hashCode() {
        return groundingsToFSM.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof VELObjectiveState)) {
            return velObjective.equals(((VELObjectiveState) obj).velObjective)
                    && groundingsToFSM.equals(((VELObjectiveState) obj).groundingsToFSM);
        }
        return false;
    }

    @Override
    public String toString() {
        return groundingsToFSM.toString();
    }

    public VELObjectiveState copy() {
        return new VELObjectiveState(velObjective, new HashMap<>(groundingsToFSM));
    }
}
