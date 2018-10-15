package util;

import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;

import java.util.Set;

public class MetaTrigger {
    private Match match;
    private Set<Atom> equalities;
    private TGD dep;

    public MetaTrigger(Match match, TGD dep){
        this.match = match;
        this.equalities = null;
        this.dep = dep;
    }
    public MetaTrigger(Match match, Set<Atom> equalities, TGD dep){
        this.match = match;
        this.equalities = equalities;
        this.dep = dep;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Set<Atom> getequalities() {
        return equalities;
    }

    public void setequalities(Set<Atom> equalities) {
        this.equalities = equalities;
    }

    public TGD getDep() {
        return dep;
    }

    public void setDep(TGD dep) {
        this.dep = dep;
    }

    public String toString(){
        return "Match : " + match + "\nequalities : " + equalities + "\nDependency : " + dep;
    }
}
