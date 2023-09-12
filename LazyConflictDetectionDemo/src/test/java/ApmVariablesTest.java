import apm.ApmVariable;
import apm.ApmVariables;
import org.junit.Assert;
import org.junit.Test;

public class ApmVariablesTest {

    @Test
    public void testGetVariable() {
        for (ApmVariable variable: ApmVariables.variables){
            ApmVariable testVar = ApmVariables.getVariable(variable.name);
            Assert.assertEquals(variable, testVar);
        }
        Assert.assertEquals(ApmVariables.getVariable("nothing"), null);
    }

    @Test
    public void testGetVariableIndex() {
        Assert.assertEquals(ApmVariables.getValueIndex("Type_WT", "wood"), 1);
        Assert.assertEquals(ApmVariables.getValueIndex("Type_WT", "tiles"), 2);
        Assert.assertEquals(ApmVariables.getValueIndex("Type_WT", "plaster"), 3);
        Assert.assertEquals(ApmVariables.getValueIndex("Type_WT", "cobble"), -1);

        Assert.assertEquals(ApmVariables.getValueIndex("AvgTemp_EC", "temp"), -1);

        Assert.assertEquals(ApmVariables.getValueIndex("Communication_MS", "wired"), 1);
        Assert.assertEquals(ApmVariables.getValueIndex("Communication_MS", "wireless"), 2);
        Assert.assertEquals(ApmVariables.getValueIndex("Communication_MS", "disconnected"), -1);
    }

    @Test
    public void testGetVariableValue() {
        Assert.assertEquals(ApmVariables.getVariableValue("Type_WT", 0), "");
        Assert.assertEquals(ApmVariables.getVariableValue("Type_WT", 1), "wood");
        Assert.assertEquals(ApmVariables.getVariableValue("Type_WT", 2), "tiles");
        Assert.assertEquals(ApmVariables.getVariableValue("Type_WT", 3), "plaster");
        Assert.assertEquals(ApmVariables.getVariableValue("Type_WT", 4), "");

        Assert.assertEquals(ApmVariables.getVariableValue("AvgTemp_EC", 1), -1);
    }
}
