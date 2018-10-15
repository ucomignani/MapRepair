import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import util.ProvenanceGraph;

public class TestSafeRewritingSrepair {

    private static Logger log = Logger.getLogger(TestSafeRewritingSrepair.class);

    @Test
    public void testProvenance() {

        Schema s = InitExamples.initMappingTestBreakNullJoins5();
        Schema v = InitExamples.initPolicyViewsTestBreakNullJoins5();

        log.info("Schema :" + s);

        SafeRewriting safeRew = new SafeRewriting(v);
        log.info("\n\tReference instance :" + safeRew.getInstanceRef());

        ProvenanceGraph provenanceOutput = safeRew.provenance(s);
        log.info("\n\nProvenance : \n" + provenanceOutput.toString());
        log.info("\nAtom provenance : " + provenanceOutput.getAtomProvenance());
        Assert.assertEquals(2, provenanceOutput.getProvenance().size());
        Assert.assertEquals(2, provenanceOutput.getProvenance().get(0).size());
        Assert.assertEquals(2, provenanceOutput.getProvenance().get(1).size());
    }

    @Test
    public void testSrepair() {

        Schema s = InitExamples.initMappingTestBreakNullJoins5();
        Schema v = InitExamples.initPolicyViewsTestBreakNullJoins5();

        SafeRewriting safeRew = new SafeRewriting(v);

        Schema srepairOutput = safeRew.srepair(s,10);

        Assert.assertTrue(safeRew.isSafe(srepairOutput));
        Assert.assertEquals(2, srepairOutput.getAllDependencies().length);
    }

    @Test
    public void testRepair() {

        Schema s = InitExamples.initMappingTestBreakNullJoins4();
        Schema v = InitExamples.initPolicyViewsTestBreakNullJoins4();

        SafeRewriting safeRew = new SafeRewriting(v);

        Schema repairOutput = safeRew.repair(s,10);
        log.info(repairOutput);
        Assert.assertTrue(safeRew.isSafe(repairOutput));
        Assert.assertEquals(1, repairOutput.getAllDependencies().length);
    }

    @Test
    public void testRepair2() {

        Schema s = InitExamples.initMappingTestBreakNullJoins5();
        Schema v = InitExamples.initPolicyViewsTestBreakNullJoins5();

        SafeRewriting safeRew = new SafeRewriting(v);

        Schema repairOutput = safeRew.repair(s,10);
        log.info(s);
        log.info(v);
        log.info(repairOutput);
        Assert.assertTrue(safeRew.isSafe(repairOutput));
        Assert.assertEquals(2, repairOutput.getAllDependencies().length);
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
