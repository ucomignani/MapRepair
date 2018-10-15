package uk.ac.ox.cs.pdq.test.reasoning.chase;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.*;
import uk.ac.ox.cs.pdq.fol.*;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.reasoning.chase.VisibleChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Tests the reasonUntilTermination method of the RestrictedChaser class
 * 
 * Mostly uses the rel1,rel2 and tgd members of PdqTest as input.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
//@Ignore
public class TestVisibleChaser extends PdqTest {

	/**
	 * <pre>
	 * Dependencies:
	 *		R(x, y) â†’ V_0(x)
	 *		R(x, y) â†’ V_1(y)
	 *
	 * Facts of the chase instance: 
	 *		R(*,*)
	 *
	 * </pre>
	 * 
	 * The chaser should produces the facts R(*,*).
	 */
	@Test
	public void testSimpleCase() {
		DatabaseChaseInstance state;
		VisibleChaser chaser = new VisibleChaser();
		
		//Create the relations of the schema
		Relation R = Relation.create("R",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1")});
		Relation V0 = Relation.create("V0",
				new Attribute[] { Attribute.create(String.class, "attribute2")});
		Relation V1 = Relation.create("V1",
				new Attribute[] { Attribute.create(String.class, "attribute3")});
		Relation r[] = new Relation[] { R, V0, V1 };
		
		//Create the dependencies of the schema 
		Dependency d[] = new Dependency[] {
				TGD.create(new Atom[] { Atom.create(R, Variable.create("x"), Variable.create("y")) },
						new Atom[] { Atom.create(V0, Variable.create("x")) }),
				TGD.create(new Atom[] { Atom.create(R, Variable.create("x"), Variable.create("y")) },
						new Atom[] { Atom.create(V1, Variable.create("y")) }),
		};
		
		//Create the actual schema object 
		Schema s = new Schema(r, d);
		Set<Atom> facts = new HashSet<>();
			
		//Create the critical instance
		for(Dependency nonEgdDependency : s.getNonEgdDependencies()) {
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
			state = new DatabaseChaseInstance(facts, createConnection(DatabaseParameters.Postgres, s));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		System.out.println("Schema:" + s);
		Set<Atom> newfacts = Sets.newHashSet(state.getFacts());
		Iterator<Atom> iterator = newfacts.iterator();
		System.out.println("Initial facts:");
		while (iterator.hasNext()) {
			Atom fact = iterator.next();
			System.out.println(fact);
		}
		chaser.reasonUntilTermination(state, d);
		System.out.println("\n\nAfter resoning:");

		System.out.println(state.getFacts() + "\n\n");
		
	}

	/**
	 * <pre>
	 * Dependencies:
	 *		R(x, y) â†’ V_0(x)
	 *		R(x, y) â†’ V_1(y)
	 *		S(u, z) â†’ V_2(z)
	 *
	 * Facts of the chase instance:
	 *		R(*,*) S(*,*)
	 *
	 * </pre>
	 *
	 * The chaser should produces the facts R(*,*) S(k2,*).
	 */
	@Test
	public void testSimpleCase2() {
		DatabaseChaseInstance state;
		VisibleChaser chaser = new VisibleChaser();

		//Create the relations of the schema
		Relation R = Relation.create("R",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1")});
		Relation S = Relation.create("S",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1")});
		Relation V0 = Relation.create("V0",
				new Attribute[] { Attribute.create(String.class, "attribute2")});
		Relation V1 = Relation.create("V1",
				new Attribute[] { Attribute.create(String.class, "attribute3")});
		Relation V2 = Relation.create("V2",
				new Attribute[] { Attribute.create(String.class, "attribute3")});
		Relation r[] = new Relation[] { R, S, V0, V1, V2 };

		//Create the dependencies of the schema
		Dependency d[] = new Dependency[] {
				TGD.create(new Atom[] { Atom.create(R, Variable.create("x"), Variable.create("y")) },
						new Atom[] { Atom.create(V0, Variable.create("x")) }),
				TGD.create(new Atom[] { Atom.create(R, Variable.create("x"), Variable.create("y")) },
						new Atom[] { Atom.create(V1, Variable.create("y")) }),
				TGD.create(new Atom[] { Atom.create(S, Variable.create("u"), Variable.create("z")) },
						new Atom[] { Atom.create(V2, Variable.create("z")) }),
		};

		//Create the actual schema object
		Schema s = new Schema(r, d);
		Set<Atom> facts = new HashSet<>();

		//Create the critical instance
		for(Dependency nonEgdDependency : s.getNonEgdDependencies()) {
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
			state = new DatabaseChaseInstance(facts, createConnection(DatabaseParameters.Postgres, s));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		System.out.println("Schema:" + s);
		Set<Atom> newfacts = Sets.newHashSet(state.getFacts());
		Iterator<Atom> iterator = newfacts.iterator();
		System.out.println("Initial facts:");
		while (iterator.hasNext()) {
			Atom fact = iterator.next();
			System.out.println(fact);
		}
		chaser.reasonUntilTermination(state, d);
		System.out.println("\n\nAfter resoning:");

		System.out.println(state.getFacts() + "\n\n");

		Assert.assertEquals(10, state.getFacts().size());
	}

	/**
	 * <pre>
	 * Dependencies:
	 *		R(x, y) âˆ§ R(y, z) âˆ§ R(z, x) â†’ V_0(x)
	 *
	 * Facts of the chase instance: 
	 *		R(*,*)
	 *
	 * </pre>
	 * 
	 * The chaser should produces the facts R(*,*).
	 */
	@Test
	public void testSelfJoins() {
		DatabaseChaseInstance state;
		VisibleChaser chaser = new VisibleChaser();
		
		//Create the relations of the schema
		Relation R = Relation.create("R",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1")});
		Relation V0 = Relation.create("V0",
				new Attribute[] { Attribute.create(String.class, "attribute2")});
		Relation r[] = new Relation[] { R, V0 };
		
		//Create the dependencies of the schema 
		Dependency d[] = new Dependency[] {
				TGD.create(new Atom[] { Atom.create(R, Variable.create("x"), Variable.create("y")), 
										Atom.create(R, Variable.create("y"), Variable.create("z")),
										Atom.create(R, Variable.create("z"), Variable.create("x"))
									},
						new Atom[] { Atom.create(V0, Variable.create("x")) }),
		};
		
		//Create the actual schema object 
		Schema s = new Schema(r, d);
		List<Atom> facts = new ArrayList<>();
			
		//Create the critical instance
		for(Dependency nonEgdDependency : s.getNonEgdDependencies()) {
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
			state = new DatabaseChaseInstance(facts, createConnection(DatabaseParameters.Postgres, s));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		System.out.println("Schema:" + s);
		Set<Atom> newfacts = Sets.newHashSet(state.getFacts());
		Iterator<Atom> iterator = newfacts.iterator();
		System.out.println("Initial facts:");
		while (iterator.hasNext()) {
			Atom fact = iterator.next();
			System.out.println(fact);
		}
		chaser.reasonUntilTermination(state, d);
		System.out.println("\n\nAfter resoning:");

		System.out.println(state.getFacts() + "\n\n");
		
	}

	@Test
	public void realCaseTest2() {
		DatabaseChaseInstance state;
		VisibleChaser chaser = new VisibleChaser();

		File policyViews = new File("policyViews.xml");

		Schema policySchema = null;
		try {
			policySchema = IOManager.importSchema(policyViews);
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

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
			
//			InternalDatabaseManager dm = new InternalDatabaseManager();
//			dm.initialiseDatabaseForSchema(s);
			
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