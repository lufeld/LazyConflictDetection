import apm.ApmVariable;
import apm.ApmVariables;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.QuickXPlain;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.*;

import java.util.ArrayList;
import java.util.List;

public class AirPollutionMonitoringModel {
    //public static AirPollutionMonitoringModel Instance = new AirPollutionMonitoringModel();
    private Model model;
    private IntVar[] variables;
    private Solver solver;

    private List<Constraint> userREQ;

    /**
     * Constructor for the Air Pollution Monitoring Model
     */
    public AirPollutionMonitoringModel(){
        model = new Model("Air pollution monitor");
        variables = new IntVar[ApmVariables.variables.length];
        solver = model.getSolver();

        // init all variables
        for(int idx = 0; idx < variables.length; idx++){
            ApmVariable varInfo = ApmVariables.variables[idx];
            String varName = varInfo.name;

            if (varInfo.domValues != null){
                variables[idx] = model.intVar(varName, 1, varInfo.domValues.length, false);
            }
            else {
                variables[idx] = model.intVar(varName, varInfo.lowerBound, varInfo.upperBound, true);
            }
         }

        // init constraints manual
        // Cat_A = field --> Type_DE = outdoor
        model.ifThen( model.arithm(variables[7], "=", 4), model.arithm(variables[3], "=", 2) );
        // Type_DE = indoor --> Communication_MS != wired
        model.ifThen( model.arithm(variables[3], "=", 2), model.arithm(variables[0], "!=", 1) );
        // Cat_A = library --> Type_DE = indoor
        model.ifThen( model.arithm(variables[7], "=", 3), model.arithm(variables[3], "=", 1) );
        // Type_DE = indoor <--> Type_A = indoor
        model.ifThen( model.arithm(variables[3], "=", 1), model.arithm(variables[6], "=", 1) );
        model.ifThen( model.arithm(variables[6], "=", 1), model.arithm(variables[3], "=", 1) );
        // Category_A = field --> Traffic_A = light
        model.ifThen( model.arithm(variables[7], "=", 4), model.arithm(variables[8], "=", 1) );
        // Type_DE = outdoor <--> AvgWind_EC != 0
        model.ifThen( model.arithm(variables[3], "=", 2), model.arithm(variables[11], "!=", 0) );
        model.ifThen( model.arithm(variables[11], "!=", 0), model.arithm(variables[3], "=", 2) );
    }

    /**
     * Experimental for model reuse (model as singleton)
     */
    public boolean evaluateRequirements(GeneticAlgorithm.UserReq requirements) {
        boolean isConflictSet = true;

        for (int idx = 0; idx < requirements.variables.length; idx++) {
            if ((requirements.initMask & (1 << idx)) == 1) {
                int value = requirements.variables[idx];
                solver.addHint(variables[idx], value);
            }
        }
        Solution solution = solver.findSolution();

        if(solution != null){
            isConflictSet = false;
        }

        solver.removeHints();
        solver.reset();

        return isConflictSet;
    }

    public void addRequirements(GeneticAlgorithm.UserReq requirements) {
        userREQ = new ArrayList<>();

        for (int idx = 0; idx < requirements.variables.length; idx++) {
            int mask = requirements.initMask & (1 << idx);
            if (mask > 0) {
                int value = requirements.variables[idx];
                IntVar variable = variables[idx];
                Constraint constraint = model.arithm(variable, "=", value);
                constraint.post();
                userREQ.add(constraint);
            }
        }
    }

    public List<Constraint> getMinimalConflictSet() {
        List<Constraint> cs = null;
        Solution solution = solver.findSolution();

        if(solution == null){
            QuickXPlain quickXPlain = new QuickXPlain(model);
            cs = quickXPlain.findMinimumConflictingSet(userREQ);
        }

        return cs;
    }



}
