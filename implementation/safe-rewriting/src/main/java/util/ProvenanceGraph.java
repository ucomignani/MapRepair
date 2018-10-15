package util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import uk.ac.ox.cs.pdq.fol.Atom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProvenanceGraph {
    private Map<Integer, Map<Integer, Config>> provenance;

    // for each atom, record Configs in which this atom occurs (only i,j are necessary to retrieve the right config)
    private Multimap<Atom, ConfigID> atomProvenance;

    public ProvenanceGraph() {
        this.provenance = new HashMap();
        this.atomProvenance = HashMultimap.create();
    }

    public Map<Integer, Map<Integer, Config>> getProvenance() {
        return provenance;
    }

    public void setProvenance(Map<Integer, Map<Integer, Config>> provenance) {
        this.provenance = provenance;
    }

    public Multimap<Atom, ConfigID> getAtomProvenance() {
        return atomProvenance;
    }

    public void setAtomProvenance(Multimap<Atom, ConfigID> atomProvenance) {
        this.atomProvenance = atomProvenance;
    }

    public void add(int i, int j, Config config){
        if(this.provenance.containsKey(i)) {
            Map<Integer, Config> mapTmp = this.provenance.get(i);
            mapTmp.put(j,config);
        } else {
            Map<Integer,Config> newMap = new HashMap<Integer, Config>();
            newMap.put(j,config);
            this.provenance.put(i, newMap);
        }
        insertAtomProvenance(i,j, config.getNewFacts());
    }

    public void addDescendantToConfig(ConfigID toUpdate, ConfigID descendant){
        Map<Integer, Config> newMap = this.provenance.get(toUpdate.getI());
        Config newConfig = newMap.get(toUpdate.getJ());

        newConfig.addDescendant(descendant);
        newMap.put(toUpdate.getJ(), newConfig);

        this.provenance.put(toUpdate.getI(), newMap);
    }

    private void insertAtomProvenance(int i, int j, Set<Atom> facts){
        ConfigID newPair = new ConfigID(i, j);

        for(Atom at : facts){
            this.atomProvenance.put(at, newPair);
        }
    }

    public Config get(int i, int j){
        return this.provenance.get(i).get(j);
    }

    public Config get(ConfigID confID){
        return this.provenance.get(confID.getI()).get(confID.getJ());
    }

    public String toString(){
        String out = "";
        for(int i : this.provenance.keySet()){
            Map<Integer,Config> mapTmp = this.provenance.get(i);
            for(int j : mapTmp.keySet())
                out += " " + mapTmp.get(j).toString() + "\n\n";
        }

        return out;
    }
}
