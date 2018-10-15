import benchmark.SchemaGenerator;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

public class TestSchemaGenerator {

    private static Logger log = Logger.getLogger(TestSchemaGenerator.class);

    @Test
    public void testSchemaGenerator1() {

        Schema s = InitExamples.initMappingTestBreakNullJoins5();

        Schema newSchema = SchemaGenerator.extractNewSchema(s, 50, 60, 1, 3, 2, 5, 0.5, 0);

        Set<Predicate> testUniquePredicate = new HashSet<>();
        for (Dependency dep : newSchema.getAllDependencies()) {

            // check if no existential variable is created
            Assert.assertEquals(0, dep.getExistential().length);

            // each head contain only one predicate
            Assert.assertEquals(1, dep.getHead().getAtoms().length);

            // each head predicate is unique
            Assert.assertFalse(testUniquePredicate.contains(dep.getHead().getAtoms()[0]));
            testUniquePredicate.add(dep.getHead().getAtoms()[0].getPredicate());

            // arities = number of terms
            for (Atom bodyAt : dep.getBodyAtoms())
                Assert.assertEquals(bodyAt.getPredicate().getArity(), bodyAt.getTerms().length);
            for (Atom headAt : dep.getBodyAtoms())
                Assert.assertEquals(headAt.getPredicate().getArity(), headAt.getTerms().length);

        }

        // check if arity of atoms and relations are the same
        for (Dependency dep : newSchema.getAllDependencies()) {
            for (Atom at : dep.getBodyAtoms()) {
                for (Relation rel : newSchema.getRelations())
                    if (rel.getName().equals(at.getPredicate().toString()))
                        Assert.assertEquals(rel.getArity(), at.getPredicate().getArity());
            }
            for (Atom at : dep.getHeadAtoms()) {
                for (Relation rel : newSchema.getRelations())
                    if (rel.getName().equals(at.getPredicate().toString()))
                        Assert.assertEquals(rel.getArity(), at.getPredicate().getArity());
            }
        }
    }

    @Test
    public void testSchemaGenerator2() {

        File policyViews = new File("./src/test/java/testFiles/policyViews.xml");

        Schema s = null;
        try {
            s = IOManager.importSchema(policyViews);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Schema newSchema = SchemaGenerator.extractNewSchema(s, 15, 30, 1, 3, 5, 8, 0.5, 0);

        Set<Predicate> testUniquePredicate = new HashSet<>();
        for (Dependency dep : newSchema.getAllDependencies()) {

            // check if no existential variable is created
            Assert.assertEquals(0, dep.getExistential().length);

            // each head contain only one predicate
            Assert.assertEquals(1, dep.getHead().getAtoms().length);

            // each head predicate is unique
            Assert.assertFalse(testUniquePredicate.contains(dep.getHead().getAtoms()[0]));
            testUniquePredicate.add(dep.getHead().getAtoms()[0].getPredicate());

            // arities = number of terms
            for (Atom bodyAt : dep.getBodyAtoms())
                Assert.assertEquals(bodyAt.getPredicate().getArity(), bodyAt.getTerms().length);
            for (Atom headAt : dep.getBodyAtoms())
                Assert.assertEquals(headAt.getPredicate().getArity(), headAt.getTerms().length);
        }

        // check if arity of atoms and relations are the same
        for (Dependency dep : newSchema.getAllDependencies()) {
            for (Atom at : dep.getBodyAtoms()) {
                for (Relation rel : newSchema.getRelations())
                    if (rel.getName().equals(at.getPredicate().toString()))
                        Assert.assertEquals(rel.getArity(), at.getPredicate().getArity());
            }
            for (Atom at : dep.getHeadAtoms()) {
                for (Relation rel : newSchema.getRelations())
                    if (rel.getName().equals(at.getPredicate().toString()))
                        Assert.assertEquals(rel.getArity(), at.getPredicate().getArity());
            }
        }
    }
}
