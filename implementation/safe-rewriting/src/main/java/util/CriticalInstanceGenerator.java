package util;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;

import java.util.HashSet;
import java.util.Set;

public class CriticalInstanceGenerator {

    /**
     * Generation of the critical instance of a source schema
     *
     * @param s Input Schema
     * @return the critical instance of the source schema of s
     */
    public static Set<Atom> generateCriticalInstance(Schema s){
        Set<Atom> facts = new HashSet<>();

        //Create the critical instance
        for (Dependency nonEgdDependency : s.getNonEgdDependencies()) {
            Atom[] bodyAtoms = nonEgdDependency.getBodyAtoms();
            for (Atom bodyAtom : bodyAtoms) {
                int bodyArity = bodyAtom.getTerms().length;
                Predicate bodyRelation = bodyAtom.getPredicate();
                Term[] terms = new Term[bodyArity];
                for (int termIndex = 0; termIndex < bodyArity; ++termIndex)
                    terms[termIndex] = TypedConstant.create("*");
                facts.add(Atom.create(bodyRelation, terms));
            }
        }

        return facts;
    }
}
