package apm;

public class ApmVariable {
    public final String name;
    public final String[] domValues;
    public final int lowerBound;
    public final int upperBound;

    public ApmVariable(String varName, String[] domainValues) {
        name = varName;
        domValues = domainValues;
        lowerBound = -1;
        upperBound = -1;
    }

    public ApmVariable(String varName, int lowerBound, int upperBound) {
        name = varName;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        domValues = null;
    }
}
