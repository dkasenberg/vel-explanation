package velexplanation.misc;

import java.util.Arrays;

public class FormulaVar {
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
