package benchmark;

import uk.ac.ox.cs.pdq.fol.Variable;

public class VariableEquality {
    private String relationName = null;
    private Variable relationVar = null;

    private String relationPrimeName = null;
    private Variable relationPrimeVar = null;

    public VariableEquality(String relationName, Variable relationVar, String relationPrimeName, Variable relationPrimeVar) {
        this.relationName = relationName;
        this.relationVar = relationVar;
        this.relationPrimeName = relationPrimeName;
        this.relationPrimeVar = relationPrimeVar;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public Variable getRelationVar() {
        return relationVar;
    }

    public void setRelationVar(Variable relationVar) {
        this.relationVar = relationVar;
    }

    public String getRelationPrimeName() {
        return relationPrimeName;
    }

    public void setRelationPrimeName(String relationPrimeName) {
        this.relationPrimeName = relationPrimeName;
    }

    public Variable getRelationPrimeVar() {
        return relationPrimeVar;
    }

    public void setRelationPrimeVar(Variable relationPrimeVar) {
        this.relationPrimeVar = relationPrimeVar;
    }

    public String toString() {
        return this.relationName + "." + this.relationVar + " = " + this.relationPrimeName + "." + this.relationPrimeVar;
    }
}
