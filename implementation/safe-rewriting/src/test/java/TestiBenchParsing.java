import benchmark.SchemaGenerator;
import benchmark.iBenchParser;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import org.apache.log4j.Logger;
import org.junit.Test;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.VisibleChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import util.SafeRewriting;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TestiBenchParsing {

    private static Logger log = Logger.getLogger(TestiBenchParsing.class);

    @Test
    public void testImport() {

        File sourceSch = new File("./src/test/java/testFiles/sourceSchema.xml");
        File targetSch = new File("./src/test/java/testFiles/targetSchTrg.xsd");
        File mapping = new File("./src/test/java/testFiles/sourceSc.xsml");

        Schema parsedSchema = iBenchParser.importSchema(sourceSch, targetSch, mapping);

        System.out.println(parsedSchema);

        Schema newSchema = SchemaGenerator.extractNewSchema(parsedSchema, 2, 5, 1, 3, 5, 8, 0.5, 0);
        SafeRewriting safeRew = new SafeRewriting(parsedSchema);
        System.out.println("sR");
        Schema repairedSchema = safeRew.repair(newSchema, 10);

        System.out.println(safeRew.isSafe(repairedSchema));
    }

    @Test
    public void testChaseImport2() {

        DatabaseChaseInstance state;
        VisibleChaser chaser = new VisibleChaser(new StatisticsCollector(true, new EventBus()));

        File sourceSch = new File("./src/test/java/testFiles/sourceSchema2.xml");
        File targetSch = new File("./src/test/java/testFiles/targetScheTrg.xsd");
        File mapping = new File("./src/test/java/testFiles/sourceSch.xsml");

        Schema policySchema = iBenchParser.importSchema(sourceSch, targetSch, mapping);

        System.out.println(policySchema);


        //Create the actual schema object
        List<Atom> facts = new ArrayList<>();

        //Create the critical instance
        for(Dependency nonEgdDependency : policySchema.getNonEgdDependencies()) {
            Atom[] bodyAtoms = nonEgdDependency.getBodyAtoms();
            for(Atom bodyAtom : bodyAtoms) {
                int bodyArity = bodyAtom.getVariables().length;
                Predicate bodyRelation = bodyAtom.getPredicate();
                Term[] terms = new Term[bodyArity];
                for(int termIndex = 0; termIndex < bodyArity; ++termIndex)
                    terms[termIndex] = TypedConstant.create("*");
                facts.add(Atom.create(bodyRelation, terms));
            }
        }


        try {
            state = new DatabaseChaseInstance(facts, createConnection(DatabaseParameters.Postgres, policySchema));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Schema:" + policySchema);
        Set<Atom> newfacts = Sets.newHashSet(state.getFacts());
        Iterator<Atom> iterator = newfacts.iterator();
        System.out.println("Initial facts:");
        while (iterator.hasNext()) {
            Atom fact = iterator.next();
            System.out.println(fact);
        }
        chaser.reasonUntilTermination(state, policySchema.getAllDependencies());
        System.out.println("\n\nAfter resoning:");

        System.out.println(state.getFacts() + "\n\n");

    }

    private DatabaseManager createConnection(DatabaseParameters params, Schema s) {
        try {
            DatabaseManager dm = new InternalDatabaseManager(new MultiInstanceFactCache(),
                    GlobalCounterProvider.getNext("DatabaseInstanceId"));
            //LogicalDatabaseInstance dm = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(params),1);
            dm.initialiseDatabaseForSchema(s);
            return dm;
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
