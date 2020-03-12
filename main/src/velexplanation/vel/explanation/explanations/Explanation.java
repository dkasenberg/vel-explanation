package velexplanation.vel.explanation.explanations;

import org.apache.commons.lang3.StringUtils;

public abstract class Explanation {

    public abstract String toStringWithIndentation(int numIndents);

    @Override
    public String toString() {
        return toStringWithIndentation(0);
    }

    public String nTabs(int nTabs) {
        return StringUtils.repeat("\t", nTabs);
    }
}
