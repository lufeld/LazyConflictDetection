import apm.ApmVariable;
import apm.ApmVariables;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.unary.PropEqualXC;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class GeneticAlgorithm {
    private final int populationSize;
    private final float mutationProbability;
    private final float initNoPreference;
    private final int numberOfGenerations;
    private UserReq[] population;
    private ArrayList<UserReq> parents;
    private Random random = new Random();

    private List<UserReq> csList = new ArrayList<>();

    public GeneticAlgorithm(int populationSize, float mutationProbability, float initNoPreference){
        this.populationSize = populationSize;
        this.mutationProbability = mutationProbability;
        this.initNoPreference = initNoPreference;
        this.numberOfGenerations = 0;
    }

    public void start(int numberOfGenerations) {
        population = new UserReq[populationSize];
        parents = new ArrayList<>();

        instantiatePopulation();
        int currentGen = 0;
        while (currentGen++ <= numberOfGenerations) {
            evaluatePopulation();
            generateNewPopulation();
            parents.clear();
            //TODO ?
        }
        printMinimalCSList();
    }

    private void instantiatePopulation() {
        for(int idx = 0; idx < populationSize; idx++){
            population[idx] = new UserReq(true);
        }
    }

    private void generateNewPopulation() {
        UserReq[] newGen = new UserReq[populationSize];
        for (int idx = 0; idx < populationSize; idx++) {
            UserReq child = generateNewChild();
            while (alreadyStoredCS(child))
                child = generateNewChild();
            newGen[idx] = child;
        }
        population = mutate(newGen);
    }

    private UserReq generateNewChild() {
        UserReq parent1 = parents.get(random.nextInt(parents.size()));
        UserReq parent2 = parents.get(random.nextInt(parents.size()));
        return new UserReq(parent1, parent2); //TODO: different parents?
    }

    private void evaluatePopulation() {
        for (UserReq req : population) {
            // feed REQ input to model
            AirPollutionMonitoringModel model = new AirPollutionMonitoringModel();
            model.addRequirements(req);
            List<Constraint> minCS = model.getMinimalConflictSet();
            if (minCS != null) {
                // skip REQs that have been identified as minimal CS before
                if (!alreadyStoredCS(req))
                    store(minCS);
                resolveAddParents(minCS, req);
            }
        }
    }

    private boolean alreadyStoredCS(UserReq req) {
        boolean isStored = false;
        for (UserReq cs : csList) {
            if (req.isSubset(cs)) {
                isStored = true;
                break;
            }
        }
        return isStored;
    }

    private void store(List<Constraint> minCS) {
        UserReq storeCS = new UserReq(minCS);
        csList.add(storeCS);
    }

    private void resolveAddParents(List<Constraint> minCS, UserReq req) {
        for (Constraint constraint : minCS) {
            UserReq resolvedReq = new UserReq(req);
            String varName = constraint.getPropagators()[0].getVars()[0].getName();
            int varIndex = ApmVariables.getVariableIndex(varName);
            resolvedReq.resetVariable(varIndex);
            parents.add(resolvedReq);
        }
    }

    private UserReq[] mutate(UserReq[] population) {
        for (UserReq req : population) {
            for (int idx = 0; idx < req.variables.length; idx++) {
                if (random.nextDouble() <= mutationProbability){
                    req.mutateVariable(idx);
                }
            }
        }
        return population;
    }

    private void printMinimalCSList() {
        for (int idx = 0; idx < csList.size(); idx++) {
            UserReq cs = csList.get(idx);
            List<String> list = cs.getVariableValues();
            String listStr = list.stream().collect(Collectors.joining(", "));
            System.out.println(String.format("CS_%d={%s}",idx,listStr));
        }
    }


    public static void main(String[] args) {
        GeneticAlgorithm geneticAlgorithm =
                new GeneticAlgorithm(100, 0.1f, 0.7f);

        geneticAlgorithm.start(20);
    }




    /**
     * Container class for user requirements (REQ). Holds values for all variables in the knowledge base.
     * Whether a value is initialized, is indicated by the initMask member. Currently, supports 32 values.
     * Each set bit represents an initialized variable, indicated by it's index (big-endian).
     * E.g.: The first variable (with index 0) is initialized if the first bit is set to 1 (i.e. 0b0001 for index 0).
     */
    class UserReq {
        public int[] variables;
        public int initMask = 0;

        /**
         * Base constructor for UserReq. Randomly initializes variables if randomize is set to true.
         * @param randomize specifies whether to initialize variables at random to random values in their domain.
         */
        UserReq(boolean randomize) {
            variables = new int[ApmVariables.variables.length];
            for (int idx = 0; idx < variables.length; idx++) {
                variables[idx] = 0;
                // initialize variable with fixed probability
                if (randomize && random.nextDouble() > initNoPreference) {
                    initMask |= 0b01 << idx;
                    ApmVariable variable = ApmVariables.variables[idx];
                    variables[idx] = getRandomAssignment(variable);
                }
            }
        }

        /**
         * Cross constructor.
         * Takes two parents and returns a new User Requirement based on the parents.
         * @param parent1 first parent
         * @param parent2 second parent
         */
        UserReq(UserReq parent1, UserReq parent2) {
            this(false);
            assert parent1.variables.length == parent2.variables.length;
            for (int idx = 0; idx < parent1.variables.length; idx++){
                if (random.nextDouble() < 0.5f) {
                    int value = parent1.variables[idx];
                    if (parent1.isValueInit(idx))
                        this.setVariable(idx, value);
                }
                else {
                    int value = parent2.variables[idx];
                    if (parent2.isValueInit(idx))
                        this.setVariable(idx, value);
                }
            }
        }

        /**
         * Copy constructor.
         * @param userReq object to copy.
         */
        UserReq(UserReq userReq) {
            variables = userReq.variables.clone();
            initMask = userReq.initMask;
        }

        /**
         * Constructor that creates a UserReq object from a minimal conflict set.
         * @param minCS Minimal conflict set as returned by QuickXPlain.findMinimumConflictingSet(conflictSet)
         */
        UserReq(List<Constraint> minCS) {
            this(false);
            for (Constraint conflict : minCS) {
                String name = conflict.getPropagator(0).getVar(0).getName();
                int value = Integer.parseInt(conflict.getPropagator(0).toString().substring(conflict.getPropagator(0).toString().lastIndexOf('=')+2));
                int variableIndex = ApmVariables.getVariableIndex(name);
                this.setVariable(variableIndex, value);
            }
        }

        private int getRandomValue(int from, int to) {
            int bound = to - from + 1;
            int rand = random.nextInt(bound);
            rand += from;
            return rand;
        }

        private int getRandomAssignment(ApmVariable variable) {
            int from, to;
            if (variable.domValues == null) {
                from = variable.lowerBound;
                to = variable.upperBound;
            } else {
                from = 1;
                to = variable.domValues.length;
            }
            return getRandomValue(from, to);
        }

        public void resetVariable(int variableIndex) {
            variables[variableIndex] = 0;
            initMask ^= 1 << variableIndex;
        }

        public void setVariable(int variableIndex, int variableValue) {
            if (ApmVariables.variables[variableIndex].domValues == null) {
                assert  (variableValue >= ApmVariables.variables[variableIndex].lowerBound) &&
                        (variableValue <= ApmVariables.variables[variableIndex].upperBound)
                        : "Variable value is out of bounds";
            }
            else {
                assert  (variableValue > 0) &&
                        (variableValue <= ApmVariables.variables[variableIndex].domValues.length)
                        : "Variable value is out of bounds";
            }
            variables[variableIndex] = variableValue;
            initMask |= 0b01 << variableIndex;
        }

        public void mutateVariable(int variableIndex) {
            int idx = variableIndex;
            this.initMask |= 0b01 << idx;
            ApmVariable variable = ApmVariables.variables[idx];
            this.variables[idx] = getRandomAssignment(variable);
        }

        public boolean isSubset(UserReq subset) {
            assert subset.initMask != 0;
            assert this.initMask != 0;

            boolean isSubset = true;

            for (int idx = 0; idx < variables.length; idx++) {
                if (subset.isValueInit(idx)) {
                    // check if instance has same value as subset
                    if (this.isValueInit(idx) && subset.variables[idx] == this.variables[idx])
                        isSubset = isSubset;
                    // else it's not subset
                    else {
                        isSubset = false;
                        break;
                    }
                }
            }

            return isSubset;
        }

        public boolean isValueInit(int idx) {
            return ((initMask >> idx) & 0b01) == 1;
        }

        public List<String> getVariableValues() {
            List<String> list = new ArrayList<>();
            for (int idx = 0; idx < variables.length; idx++) {
                if (isValueInit(idx)) {
                    String name = ApmVariables.variables[idx].name;
                    String value = ApmVariables.getVariableValue(name, variables[idx]);
                    list.add(String.format("%s=%s", name, value));
                }
            }
            return list;
        }
    }
}
