package util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class VariablePositionMultimap {

    private Multimap<Atom,VariablePosition> mapVarPosition;

    public VariablePositionMultimap() {
        mapVarPosition = HashMultimap.create();
    }

    public VariablePositionMultimap(Atom[] atoms) {
        mapVarPosition = HashMultimap.create();

        for(int i = 0; i<atoms.length; i++){

            Atom at = atoms[i];
            Term[] terms = at.getTerms();

            for(int j = 0; j<terms.length;j++){
                mapVarPosition.put(at, new VariablePosition(Variable.create(terms[j].toString()),i,j));
            }
        }
    }
    
    public int size() {
        return this.mapVarPosition.size();
    }

    
    public boolean isEmpty() {
        return this.mapVarPosition.isEmpty();
    }

    
    public boolean containsKey(Atom o) {
        return this.mapVarPosition.containsKey(o);
    }

    
    public boolean containsValue(VariablePosition o) {
        return this.mapVarPosition.containsValue(o);
    }

    
    public boolean containsEntry(Atom o, VariablePosition o1) {
        return this.mapVarPosition.containsEntry(o,o1);
    }

    
    public boolean put(Atom o, VariablePosition o2) {
        return this.mapVarPosition.put(o,o2);
    }

    
    public boolean remove(Atom o, VariablePosition o1) {
        return this.mapVarPosition.remove(o,o1);
    }

    
    public boolean putAll(Atom o, Iterable iterable) {
        return this.mapVarPosition.putAll(o,iterable);
    }

    
    public boolean putAll(Multimap multimap) {
        return this.mapVarPosition.putAll(multimap);
    }

    
    public Collection replaceValues(Atom o, Iterable iterable) {
        return this.mapVarPosition.replaceValues(o,iterable);
    }

    
    public Collection removeAll(Object o) {
        return this.mapVarPosition.removeAll(o);
    }

    
    public void clear() {
        this.mapVarPosition.clear();
    }

    
    public Collection<VariablePosition> get(Atom o) {
        return this.mapVarPosition.get(o);
    }

    
    public Set<Atom> keySet() {
        return this.mapVarPosition.keySet();
    }

    
    public Multiset keys() {
        return this.mapVarPosition.keys();
    }

    
    public Collection values() {
        return this.mapVarPosition.values();
    }

    
    public Collection<Map.Entry<Atom,VariablePosition>> entries() {
        return this.mapVarPosition.entries();
    }


    public Map asMap() {
        return this.mapVarPosition.asMap();
    }

    public String toString() {
        String output = "";
        for(Atom at : this.mapVarPosition.keySet()){
            output += at.toString() + "\n";
            for(VariablePosition vp : this.mapVarPosition.get(at)){
                output += "\t" + vp.toString() + "\n";
            }
        }

        return output;
    }

}
