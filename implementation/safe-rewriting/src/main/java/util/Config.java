package util;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;

import java.util.HashSet;
import java.util.Set;

public class Config {

    ConfigID configID = null;
    MetaTrigger currentMetaTrigger = null;
    Set<ConfigID> descendantConfigSet = new HashSet<>();
    Set<Atom> newFacts = new HashSet<>();

    public Config(ConfigID configID, MetaTrigger currentMetaTrigger, Set<Atom> facts) {
        this.configID = configID;
        this.currentMetaTrigger = currentMetaTrigger;
        this.newFacts = facts;
    }

    public ConfigID getConfigID() {
        return configID;
    }

    public void setConfigID(ConfigID configID) {
        this.configID = configID;
    }

    public Set<Atom> getNewFacts() {
        return this.newFacts;
    }

    public void setNewFacts(Set<Atom> facts) {
        this.newFacts = facts;
    }

    public MetaTrigger getCurrentMetaTrigger() {
        return currentMetaTrigger;
    }

    public void setCurrentMetaTrigger(MetaTrigger currentMetaTrigger) {
        this.currentMetaTrigger = currentMetaTrigger;
    }

    public Set<ConfigID> getDescendantConfigSet() {
        return descendantConfigSet;
    }

    public void addDescendant(ConfigID confID) {
        this.descendantConfigSet.add(confID);
    }

    public void setdescendantConfigSet(Set<ConfigID> descendantConfigSet) {
        this.descendantConfigSet = descendantConfigSet;
    }

    public String toString(){
        return this.configID.toString()
                + "; new facts : " + this.newFacts
                + "; triggers : " + this.descendantConfigSet;
    }

    public Set<Constant> getConstantsFacts() {
        Set<Constant> output = new HashSet<>();

        for(Atom fact : this.newFacts){
            for(Term term : fact.getTerms()) {
                output.add(TypedConstant.create(term.toString()));
            }
        }
        return output;
    }
}
