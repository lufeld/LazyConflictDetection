package apm;

import apm.ApmVariable;

import java.util.Arrays;
import java.util.Objects;

public class ApmVariables {

    public static final ApmVariable[] variables = new ApmVariable[]{
            new ApmVariable("Communication_MS", new String[]{"wired", "wireless"}),
            new ApmVariable("Storage_MS", new String[]{"local", "cloud"}),
            new ApmVariable("Enclosure_MS", new String[]{"rugged", "standard"}),
            new ApmVariable("Type_DE", new String[]{"indoor", "outdoor"}),
            new ApmVariable("Context_DE", new String[]{"country", "tropical"}),
            new ApmVariable("Location_DE", new String[]{"urban", "countryside", "industrial"}),
            new ApmVariable("Type_A", new String[]{"indoor", "outdoor"}),
            new ApmVariable("Category_A", new String[]{"industry", "shop", "library", "field"}),
            new ApmVariable("Traffic_A", new String[]{"light", "medium", "heavy"}),
            new ApmVariable("Type_WT", new String[]{"wood", "tiles", "plaster"}),
            new ApmVariable("AvgTemp_EC", -50, 50),
            new ApmVariable("AvgWind_EC", 0, 17),
            new ApmVariable("AvgPressure_EC", 1, 10)
    };

    public static int getValueIndex(String variableName, String valueName){
        ApmVariable variable = getVariable(variableName);
        int index = -1;
        if (variable != null) {
            String[] domain = variable.domValues;
            if (domain != null) {
                for (int idx = 0; idx < domain.length; idx++) {
                    if (domain[idx].equals(valueName)) {
                        index = idx + 1;
                    }
                }
            }
        }
        return index;
    }

    public static String getVariableValue(String variableName, int domainIndex){
        ApmVariable variable = getVariable(variableName);
        String[] domainValues = variable.domValues;
        String value = "";
        if (domainValues != null && domainIndex >= 1 && domainIndex <= domainValues.length)
            value = domainValues[domainIndex-1];
        else if (domainValues == null && domainIndex >= variable.lowerBound && domainIndex <= variable.upperBound) {
            value = String.valueOf(domainIndex);
        }
        return value;
    }

    public static ApmVariable getVariable(String variableName){
        ApmVariable variable;
        var filter = Arrays.stream(variables).filter(var -> var.name == variableName).findFirst();
        variable = filter.orElse(null);

        return variable;
    }

    public static int getVariableIndex(String variableName){
        int index = -1;
        for (int idx = 0; idx < variables.length; idx++) {
            if (variables[idx].name.equals(variableName)) {
                index = idx;
                break;
            }
        }
        return index;
    }
}

