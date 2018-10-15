package util;

import uk.ac.ox.cs.pdq.fol.Variable;

public class VariablePosition {

    private Variable variable;
    private int atomPosition;
    private int positionInAtom;

    public VariablePosition(Variable variable, int atomPosition, int positionInAtom){
        this.variable = variable;
        this.atomPosition = atomPosition;
        this.positionInAtom = positionInAtom;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public int getAtomPosition() {
        return atomPosition;
    }

    public void setAtomPosition(int atomPosition) {
        this.atomPosition = atomPosition;
    }

    public int getPositionInAtom() {
        return positionInAtom;
    }

    public void setPositionInAtom(int positionInAtom) {
        this.positionInAtom = positionInAtom;
    }

    public String toString(){
        return "var=" + variable + " at=" + atomPosition + " pos=" + positionInAtom;
    }
}
