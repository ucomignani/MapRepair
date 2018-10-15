package util;

import uk.ac.ox.cs.pdq.fol.Atom;

import java.util.Set;

public class ResultComputeRewriting {

    private Set<Atom> setAtoms;
    private int nbNewVarFrontier;
    private int nbNewVarNonFrontier;


    public ResultComputeRewriting(Set<Atom> atomSet, int nbNewVarFrontier, int nbNewVarNonFrontier) {
        this.setAtoms = atomSet;
        this.nbNewVarFrontier = nbNewVarFrontier;
        this.nbNewVarNonFrontier = nbNewVarNonFrontier;
    }

    public Set<Atom> getSetAtoms() {
        return setAtoms;
    }

    public int getNbNewVarFrontier() {
        return nbNewVarFrontier;
    }

    public int getNbNewVarNonFrontier() {
        return nbNewVarNonFrontier;
    }

    public String toString() {
        return this.getSetAtoms() + "\n\t\t\t\t\tNew non frontier vars : " + nbNewVarNonFrontier + "\tNew frontier vars : " + nbNewVarFrontier;
    }

    public int getNbNewVars() {
        return nbNewVarFrontier + nbNewVarNonFrontier;
    }
}
