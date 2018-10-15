package benchmark;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.*;

import java.util.*;

public class SchemaGenerator {

    public static Schema extractNewSchema(Schema inputSchema,
                                          int minTgdNumber,
                                          int maxTgdNumber,
                                          int minBodyRelationNumber,
                                          int maxBodyRelationNumber,
                                          int minHeadPredicateSize,
                                          int maxHeadPredicateSize,
                                          double probabilityOfFreshVariableInBody,
                                          double probabilityOfFreshVariableInHead) {

        Random random = new Random();
        Set<Predicate> usablePredicates = extractBodyPredicates(inputSchema);
        Set<Variable> existingVariables = new HashSet<>();
        Set<Dependency> generatedDependencies = new HashSet<>();
        Set<Relation> newSchemaRelations = new HashSet<>();
        newSchemaRelations.addAll(Arrays.asList(inputSchema.getRelations()));

        // for new predicates generation
        int indexHeadPredicate = 0;
        Set<String> predicatesNames = new HashSet<>();
        for (Predicate pred : extractPredicates(inputSchema))
            predicatesNames.add(pred.getName());


        // begin generation
        int numberOfTgds = random.nextInt(maxTgdNumber - minTgdNumber + 1) + minTgdNumber;
        System.out.println("Choosen number of tgds : " + numberOfTgds);

        for (int i = 0; i < numberOfTgds; i++) {
            int bodySize = random.nextInt(maxBodyRelationNumber - minBodyRelationNumber + 1) + minBodyRelationNumber;
            Set<Atom> newBody = new HashSet<>();
            Set<Atom> newHead = new HashSet<>();

            // generate body
            Set<Variable> existingBodyVariables = new HashSet<>();
            for (int j = 0; j < bodySize; j++) {
                Predicate newPredicate = pickupRandomObject(usablePredicates);
                Atom newAtom = generateNewAtom(newPredicate, existingVariables, probabilityOfFreshVariableInBody);
                newBody.add(newAtom);

                existingBodyVariables.addAll(Arrays.asList(newAtom.getVariables()));
            }

            // generate head
            Predicate headPredicate = null;
            do {
                headPredicate = generateNewPredicate(indexHeadPredicate, minHeadPredicateSize, maxHeadPredicateSize);
                indexHeadPredicate++;
            } while (predicatesNames.contains(headPredicate.getName()));
            predicatesNames.add(headPredicate.getName());

            newHead.add(generateNewAtom(headPredicate, existingBodyVariables, probabilityOfFreshVariableInHead));
            newSchemaRelations.add(generateRelationFromHeadPredicate(headPredicate));

            // generate tgd
            TGD newTgd = TGD.create(newBody.toArray(new Atom[0]), newHead.toArray(new Atom[0]));
            generatedDependencies.add(newTgd);
        }


        return new Schema(newSchemaRelations.toArray(new Relation[0]), generatedDependencies.toArray(new Dependency[0]));
    }

    private static Relation generateRelationFromHeadPredicate(Predicate headPredicate) {
        String relationName = headPredicate.getName();

        Attribute[] attributes = new Attribute[headPredicate.getArity()];
        for (int i = 0; i < headPredicate.getArity(); i++)
            attributes[i] = Attribute.create(String.class, "at" + i);

        return Relation.create(relationName, attributes);
    }

    private static Predicate generateNewPredicate(int indexHeadPredicate,
                                                  int minHeadPredicateSize,
                                                  int maxHeadPredicateSize) {
        int predicateArity = new Random().nextInt(maxHeadPredicateSize - minHeadPredicateSize + 1) + minHeadPredicateSize;

        return Predicate.create("V" + indexHeadPredicate, predicateArity);
    }

    private static Atom generateNewAtom(Predicate newPredicate, Set<Variable> existingVariables, double probabilityOfFresh) {
        int predicateArity = newPredicate.getArity();

        Variable[] vars = new Variable[predicateArity];
        for (int i = 0; i < predicateArity; i++) {
            if (existingVariables.isEmpty() || probabilityOfFresh > new Random().nextDouble()) {
                Variable newVar = generateNewVariable(existingVariables);
                existingVariables.add(newVar);
                vars[i] = newVar;
            } else {
                vars[i] = pickupRandomObject(existingVariables);
            }
        }
        return Atom.create(newPredicate, vars);
    }

    private static Variable generateNewVariable(Set<Variable> existingVariables) {
        int i = 0;
        Variable var = Variable.create("v" + i);

        while (existingVariables.contains(var)) {
            i++;
            var = Variable.create("v" + i);
        }
        return var;
    }

    private static Set<Predicate> extractPredicates(Schema inputSchema) {
        Set<Predicate> output = new HashSet<>();

        for (Dependency dep : inputSchema.getAllDependencies())
            for (Atom atom : dep.getAtoms())
                output.add(atom.getPredicate());

        return output;
    }

    private static Set<Predicate> extractBodyPredicates(Schema inputSchema) {
        Set<Predicate> output = new HashSet<>();

        for (Dependency dep : inputSchema.getAllDependencies())
            for (Atom atom : dep.getBodyAtoms())
                output.add(atom.getPredicate());

        return output;
    }

    private static <T> T pickupRandomObject(Set<T> inputSet) {

        int sizeSet = inputSet.size();
        int objectIndex = new Random().nextInt(sizeSet);
        int i = 0;

        for (T object : inputSet) {
            if (i == objectIndex)
                return object;
            i++;
        }
        return null;
    }

}
