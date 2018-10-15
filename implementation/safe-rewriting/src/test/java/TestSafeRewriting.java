import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestSafeRewriting {

    private static Logger log = Logger.getLogger(TestSafeRewriting.class);

    @Test
    public void testHidePredicate1() {

        Schema s = InitExamples.initMappingTestHidePredicate1();
        Schema v = InitExamples.initPolicyViewsTestHidePredicate1();

        log.info("Schema :" + s);
        log.info("Policy view :" + v);

        SafeRewriting safeRew = new SafeRewriting(v);
        Schema sHiddenPred = safeRew.hidePredicates(s);

        log.info("Reference instance :" + safeRew.getInstanceRef());
        log.info("Outputed mapping : " + sHiddenPred);

        /** allowed predicates */
        Set<String> allowedPredicates = new HashSet();
        for (Relation r : v.getRelations()) {
            allowedPredicates.add(r.getName());
        }
        for (Dependency d : sHiddenPred.getNonEgdDependencies()) {
            for (Atom at : d.getBody().getAtoms()) {
                Assert.assertTrue(allowedPredicates.contains(at.getPredicate().toString()));
            }
        }
    }

    @Test
    public void testExistMorphism() {

        Schema s = InitExamples.initMappingTestBreakNullJoins1();
        Schema v = InitExamples.initPolicyViewsTestBreakNullJoins1();

        log.info("Schema :" + s);
        //   log.info("Policy view :" + v);

        SafeRewriting safeRew = new SafeRewriting(v);
        SafeRewriting safeRew2 = new SafeRewriting(s);
        //   log.info("Reference instance :" + safeRew.getInstanceRef());

        /** version with formula */
        Assert.assertFalse(safeRew.existsMorphismConjunction(s.getAllDependencies()[0].getBody()));
        Assert.assertTrue(safeRew2.existsMorphismConjunction(s.getAllDependencies()[0].getBody()));

        /** version with atom set */
        Set<Atom> atSet = new HashSet<Atom>(Arrays.asList(s.getAllDependencies()[0].getBody().getAtoms()));
        Assert.assertFalse(safeRew.existsMorphismConjunction(atSet));
        Assert.assertTrue(safeRew2.existsMorphismConjunction(atSet));
    }

    @Test
    public void testBreakNullJoins1() {

        Schema s = InitExamples.initMappingTestBreakNullJoins1();
        Schema v = InitExamples.initPolicyViewsTestBreakNullJoins1();

        SafeRewriting safeRew = new SafeRewriting(v);
        Schema out = safeRew.fRepair(s);

        Assert.assertTrue(safeRew.isSafe(out));
        Assert.assertEquals(1, out.getAllDependencies().length);
    }

    @Test
    public void testBreakNullJoins2() {

        Schema s = InitExamples.initMappingTestBreakNullJoins2();
        Schema v = InitExamples.initPolicyViewsTestBreakNullJoins2();

        SafeRewriting safeRew = new SafeRewriting(v);

        Schema out = safeRew.fRepair(s);

        log.info(out);
        Assert.assertTrue(safeRew.isSafe(out));
        Assert.assertEquals(1, out.getAllDependencies().length);
    }

    @Test
    public void testBreakNullJoins3() {

        Schema s = InitExamples.initMappingTestBreakNullJoins3();
        Schema v = InitExamples.initPolicyViewsTestBreakNullJoins2();

        SafeRewriting safeRew = new SafeRewriting(v);

        Schema out = safeRew.fRepair(s);

        log.info(out);
        Assert.assertTrue(safeRew.isSafe(out));
        Assert.assertEquals(1, out.getAllDependencies().length);

    }

    @Test
    public void testBreakNullJoins4() {

        Schema s = InitExamples.initMappingTestBreakNullJoins4();
        Schema v = InitExamples.initPolicyViewsTestBreakNullJoins4();

        SafeRewriting safeRew = new SafeRewriting(v);

        Schema out1 = safeRew.fRepair(s);
        Schema out2 = safeRew.srepair(out1, 10);

        SafeRewriting safeRew2 = new SafeRewriting(out2);

        log.info(safeRew.getInstanceRef());

        Assert.assertTrue(safeRew.isSafe(out1));
        Assert.assertEquals(1, out2.getAllDependencies().length);
    }

    @Test
    public void realCaseTest1() {

        File policyViews = new File("./src/test/java/testFiles/simplePolicyViews.xml");

        Schema policySchema = null;
        try {
            policySchema = IOManager.importSchema(policyViews);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Relation birth = Relation.create("birth",
                new Attribute[]{Attribute.create(String.class, "row_id"), Attribute.create(String.class, " study_id_mother"), Attribute.create(String.class, " study_id_child"), Attribute.create(String.class, " sex"), Attribute.create(String.class, " total_births_live_and_still"), Attribute.create(String.class, " live_males"), Attribute.create(String.class, " live_females"), Attribute.create(String.class, " stillborn_males"), Attribute.create(String.class, " stillborn_sex_not_known"), Attribute.create(String.class, " liveborn_sex_not_known"), Attribute.create(String.class, " council_area"), Attribute.create(String.class, " institution"), Attribute.create(String.class, " mother_country_of_residence"), Attribute.create(String.class, " mother_country_of_birth"), Attribute.create(String.class, " occupation"), Attribute.create(String.class, " employment_status"), Attribute.create(String.class, " socio_economic_group"), Attribute.create(String.class, " father_country_of_birth"), Attribute.create(String.class, " health_board_area"), Attribute.create(String.class, " mother_occupation"), Attribute.create(String.class, " mother_occupation_code"), Attribute.create(String.class, " mother_social_class"), Attribute.create(String.class, " father_occupation"), Attribute.create(String.class, " father_occupation_code"), Attribute.create(String.class, " father_employment_status"), Attribute.create(String.class, " father_social_class"), Attribute.create(String.class, " multiple_births_linked_records"), Attribute.create(String.class, " postcode"), Attribute.create(String.class, " date_of_birth_str"), Attribute.create(String.class, " date_of_birth"), Attribute.create(String.class, " time_of_birth"), Attribute.create(String.class, " year_of_registration"), Attribute.create(String.class, " registration_district"), Attribute.create(String.class, " entry_number")
                });
        Relation patient = Relation.create("patient",
                new Attribute[]{Attribute.create(String.class, "row_id"), Attribute.create(String.class, " study_id"), Attribute.create(String.class, " nhs_number"), Attribute.create(String.class, " ew_number"), Attribute.create(String.class, " ni_number"), Attribute.create(String.class, " forename"), Attribute.create(String.class, " middle_name"), Attribute.create(String.class, " surname"), Attribute.create(String.class, " date_of_birth"), Attribute.create(String.class, " country_of_birth"), Attribute.create(String.class, " date_of_death"), Attribute.create(String.class, " birth_place"), Attribute.create(String.class, " mothers_birth_name"), Attribute.create(String.class, " postcode"), Attribute.create(String.class, " sex"), Attribute.create(String.class, " uprn"), Attribute.create(String.class, " postcode_source")
                });

        Relation V0 = Relation.create("V1",
                new Attribute[]{Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation r[] = new Relation[]{birth, patient, V0};

        TGD tgd = TGD.create(new Atom[]{
                        Atom.create(birth, Variable.create("v081"), Variable.create("v1"), Variable.create("v093"), Variable.create("v080"), Variable.create("v278"), Variable.create("v2102"), Variable.create("v374"), Variable.create("v071"), Variable.create("v488"), Variable.create("v169"), Variable.create("v182"), Variable.create("v547"), Variable.create("v6"), Variable.create("v0100"), Variable.create("v4103"), Variable.create("v2104"), Variable.create("v795"), Variable.create("v897"), Variable.create("v991"), Variable.create("v7"), Variable.create("v1079"), Variable.create("v11"), Variable.create("v092"), Variable.create("v1284"), Variable.create("v1089"), Variable.create("v1175"), Variable.create("v1377"), Variable.create("v13105"), Variable.create("v1468"), Variable.create("v14"), Variable.create("v1599"), Variable.create("v16101"), Variable.create("v1725"), Variable.create("v1886")),
                        Atom.create(patient, Variable.create("v19"), Variable.create("v20"), Variable.create("v363"), Variable.create("v2161"), Variable.create("v2258"), Variable.create("v2359"), Variable.create("v2455"), Variable.create("v2551"), Variable.create("v19"), Variable.create("v21"), Variable.create("v252"), Variable.create("v18"), Variable.create("v2666"), Variable.create("v2053"), Variable.create("v2667"), Variable.create("v2757"), Variable.create("v2864")),
                        //  Atom.create(birth, Variable.create("v2936"), Variable.create("v30"), Variable.create("v2828"), Variable.create("v101"), Variable.create("v3124"), Variable.create("v3240"), Variable.create("v3344"), Variable.create("v3433"), Variable.create("v147"), Variable.create("v2013"), Variable.create("v3549"), Variable.create("v547"), Variable.create("v0"), Variable.create("v3635"), Variable.create("v1020"), Variable.create("v305"), Variable.create("v3731"), Variable.create("v3815"), Variable.create("v1638"), Variable.create("v39"), Variable.create("v373"), Variable.create("v39"), Variable.create("v4042"), Variable.create("v418"), Variable.create("v511"), Variable.create("v3145"), Variable.create("v2230"), Variable.create("v4219"), Variable.create("v109"), Variable.create("v43"), Variable.create("v617"), Variable.create("v246"), Variable.create("v1726"), Variable.create("v2322"))
                },
                new Atom[]{Atom.create(V0, Variable.create("v29"), Variable.create("v2"), Variable.create("v39"), Variable.create("v29"), Variable.create("v1"), Variable.create("v41"), Variable.create("v11"))
                });


        Schema newSchema = new Schema(r, new Dependency[]{tgd});

        SafeRewriting safeRew = new SafeRewriting(policySchema);

        Schema result = safeRew.repair(newSchema, 1);
        Assert.assertTrue(safeRew.isSafe(result));
    }

    @Test
    public void realCaseTest2() {

        File policyViews = new File("./src/test/java/testFiles/policyViews.xml");

        Schema policySchema = null;
        try {
            policySchema = IOManager.importSchema(policyViews);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Relation birth = Relation.create("birth",
                new Attribute[]{Attribute.create(String.class, "row_id"), Attribute.create(String.class, " study_id_mother"), Attribute.create(String.class, " study_id_child"), Attribute.create(String.class, " sex"), Attribute.create(String.class, " total_births_live_and_still"), Attribute.create(String.class, " live_males"), Attribute.create(String.class, " live_females"), Attribute.create(String.class, " stillborn_males"), Attribute.create(String.class, " stillborn_sex_not_known"), Attribute.create(String.class, " liveborn_sex_not_known"), Attribute.create(String.class, " council_area"), Attribute.create(String.class, " institution"), Attribute.create(String.class, " mother_country_of_residence"), Attribute.create(String.class, " mother_country_of_birth"), Attribute.create(String.class, " occupation"), Attribute.create(String.class, " employment_status"), Attribute.create(String.class, " socio_economic_group"), Attribute.create(String.class, " father_country_of_birth"), Attribute.create(String.class, " health_board_area"), Attribute.create(String.class, " mother_occupation"), Attribute.create(String.class, " mother_occupation_code"), Attribute.create(String.class, " mother_social_class"), Attribute.create(String.class, " father_occupation"), Attribute.create(String.class, " father_occupation_code"), Attribute.create(String.class, " father_employment_status"), Attribute.create(String.class, " father_social_class"), Attribute.create(String.class, " multiple_births_linked_records"), Attribute.create(String.class, " postcode"), Attribute.create(String.class, " date_of_birth_str"), Attribute.create(String.class, " date_of_birth"), Attribute.create(String.class, " time_of_birth"), Attribute.create(String.class, " year_of_registration"), Attribute.create(String.class, " registration_district"), Attribute.create(String.class, " entry_number")
                });
        Relation patient = Relation.create("patient",
                new Attribute[]{Attribute.create(String.class, "row_id"), Attribute.create(String.class, " study_id"), Attribute.create(String.class, " nhs_number"), Attribute.create(String.class, " ew_number"), Attribute.create(String.class, " ni_number"), Attribute.create(String.class, " forename"), Attribute.create(String.class, " middle_name"), Attribute.create(String.class, " surname"), Attribute.create(String.class, " date_of_birth"), Attribute.create(String.class, " country_of_birth"), Attribute.create(String.class, " date_of_death"), Attribute.create(String.class, " birth_place"), Attribute.create(String.class, " mothers_birth_name"), Attribute.create(String.class, " postcode"), Attribute.create(String.class, " sex"), Attribute.create(String.class, " uprn"), Attribute.create(String.class, " postcode_source")
                });

        Relation V0 = Relation.create("V1",
                new Attribute[]{Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5"), Attribute.create(String.class, "a5")});
        Relation r[] = new Relation[]{birth, patient, V0};

        TGD tgd = TGD.create(new Atom[]{
                        Atom.create(birth, Variable.create("v081"), Variable.create("v1"), Variable.create("v093"), Variable.create("v080"), Variable.create("v278"), Variable.create("v2102"), Variable.create("v374"), Variable.create("v071"), Variable.create("v488"), Variable.create("v169"), Variable.create("v182"), Variable.create("v547"), Variable.create("v6"), Variable.create("v0100"), Variable.create("v4103"), Variable.create("v2104"), Variable.create("v795"), Variable.create("v897"), Variable.create("v991"), Variable.create("v7"), Variable.create("v1079"), Variable.create("v11"), Variable.create("v092"), Variable.create("v1284"), Variable.create("v1089"), Variable.create("v1175"), Variable.create("v1377"), Variable.create("v13105"), Variable.create("v1468"), Variable.create("v14"), Variable.create("v1599"), Variable.create("v16101"), Variable.create("v1725"), Variable.create("v1886")),
                        Atom.create(patient, Variable.create("v19"), Variable.create("v20"), Variable.create("v363"), Variable.create("v2161"), Variable.create("v2258"), Variable.create("v2359"), Variable.create("v2455"), Variable.create("v2551"), Variable.create("v19"), Variable.create("v21"), Variable.create("v252"), Variable.create("v18"), Variable.create("v2666"), Variable.create("v2053"), Variable.create("v2667"), Variable.create("v2757"), Variable.create("v2864")),
                },
                new Atom[]{Atom.create(V0, Variable.create("v29"), Variable.create("v2"), Variable.create("v39"), Variable.create("v29"), Variable.create("v1"), Variable.create("v41"), Variable.create("v11"))
                });


        Schema newSchema = new Schema(r, new Dependency[]{tgd});

        SafeRewriting safeRew = new SafeRewriting(policySchema);

        Schema result = safeRew.repair(newSchema, 1);
        Assert.assertTrue(safeRew.isSafe(result));
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
