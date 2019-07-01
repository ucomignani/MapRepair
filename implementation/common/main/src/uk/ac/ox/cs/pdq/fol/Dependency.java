package uk.ac.ox.cs.pdq.fol;

import java.util.*;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.io.jaxb.adapters.DependencyAdapter;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * A universally quantified implication where the body is a quantifier-free formula and
 * the head is an existentially-quantified or quantifier-free formula.
 *
 * @author Efthymia Tsamoura
 */
@XmlJavaTypeAdapter(DependencyAdapter.class)
public class Dependency extends QuantifiedFormula {

    private static final long serialVersionUID = 6522148218362709983L;
    protected final Formula body;
    protected final Formula head;

    /**
     * The dependency's universally quantified variables.
     */
    protected Variable[] universal;

    /**
     * The dependency's existentially quantified variables.
     */
    protected Variable[] existential;

    protected final Atom[] bodyAtoms;

    protected final Atom[] headAtoms;

    protected final Variable[] bodyVariables;

    protected final Variable[] headVariables;

    protected final Variable[] frontierVariables;

    protected Dependency(Formula body, Formula head) {
        super(LogicalSymbols.UNIVERSAL, body.getFreeVariables(), Implication.create(body, head));
        Assert.assertTrue(isUnquantified(body));
        Assert.assertTrue(isExistentiallyQuantified(head) || isUnquantified(head));
        Assert.assertTrue(Arrays.asList(body.getFreeVariables()).containsAll(Arrays.asList(head.getFreeVariables())));
        this.body = body;
        this.head = head;
        this.bodyAtoms = this.body.getAtoms();
        this.headAtoms = this.head.getAtoms();

        Set<Variable> bodyVariables = new LinkedHashSet<>();
        for (int atomIndex = 0; atomIndex < this.bodyAtoms.length; ++atomIndex) {
            Atom atom = bodyAtoms[atomIndex];
            for (int termIndex = 0; termIndex < atom.getNumberOfTerms(); ++termIndex) {
                if (atom.getTerm(termIndex).isVariable())
                    bodyVariables.add((Variable) atom.getTerm(termIndex));
            }
        }
        this.bodyVariables = bodyVariables.toArray(new Variable[bodyVariables.size()]);

        Set<Variable> headVariables = new LinkedHashSet<>();
        for (int atomIndex = 0; atomIndex < this.headAtoms.length; ++atomIndex) {
            Atom atom = headAtoms[atomIndex];
            for (int termIndex = 0; termIndex < atom.getNumberOfTerms(); ++termIndex) {
                if (atom.getTerm(termIndex).isVariable())
                    headVariables.add((Variable) atom.getTerm(termIndex));
            }
        }
        this.headVariables = headVariables.toArray(new Variable[headVariables.size()]);

        Collection<Variable> frontierVariablesSet = CollectionUtils.intersection(bodyVariables, headVariables);
        this.frontierVariables = frontierVariablesSet.toArray(new Variable[frontierVariablesSet.size()]);
    }


    protected Dependency(Atom[] body, Atom[] head) {
        this(Conjunction.of(body), createHead(body, head));
    }

    private static Formula createHead(Atom[] body, Atom[] head) {
        List<Variable> bodyVariables = Utility.getVariables(body);
        List<Variable> headVariables = Utility.getVariables(head);
        if (bodyVariables.containsAll(headVariables))
            return Conjunction.of(head);
        else {
            headVariables.removeAll(bodyVariables);
            return QuantifiedFormula.create(LogicalSymbols.EXISTENTIAL, headVariables.toArray(new Variable[headVariables.size()]), Conjunction.of(head));
        }
    }

    private static boolean isUnquantified(Formula formula) {
        if (formula instanceof Conjunction || formula instanceof Implication || formula instanceof Disjunction)
            return isUnquantified(formula.getChildren()[0]) && isUnquantified(formula.getChildren()[1]);
        else if (formula instanceof Negation)
            return isUnquantified(formula.getChildren()[0]);
        else if (formula instanceof Literal)
            return true;
        else if (formula instanceof Atom)
            return true;
        else if (formula instanceof QuantifiedFormula)
            return false;
        return false;
    }

    private static boolean isExistentiallyQuantified(Formula formula) {
        if (formula instanceof Conjunction || formula instanceof Implication || formula instanceof Disjunction)
            return isUnquantified(formula.getChildren()[0]) && isUnquantified(formula.getChildren()[0]);
        else if (formula instanceof Negation)
            return isUnquantified(formula.getChildren()[0]);
        else if (formula instanceof Literal)
            return true;
        else if (formula instanceof Atom)
            return true;
        else if (formula instanceof QuantifiedFormula) {
            if (((QuantifiedFormula) formula).isExistential()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Gets the left-hand side of this constraint.
     *
     * @return the left-hand side of this constraint
     */
    public Formula getBody() {
        return this.body;
    }

    /**
     * Gets the right-hand side of this constraint.
     *
     * @return the right-hand side of this constraint
     */
    public Formula getHead() {
        return this.head;
    }

    /**
     * Gets the universally quantified variables.
     *
     * @return List<Variable>
     */
    public Variable[] getUniversal() {
        if (this.universal == null)
            this.universal = this.variables;
        return this.universal.clone();
    }

    /**
     * Gets the existentially quantified variables.
     *
     * @return List<Variable>
     */
    public Variable[] getExistential() {
        if (this.existential == null)
            this.existential = this.head.getBoundVariables();
        return this.existential.clone();
    }


    public int getNumberOfBodyAtoms() {
        return this.bodyAtoms.length;
    }

    public int getNumberOfHeadAtoms() {
        return this.headAtoms.length;
    }

    public Atom getBodyAtom(int bodyAtomIndex) {
        return this.bodyAtoms[bodyAtomIndex];
    }

    public Atom getHeadAtom(int headAtomIndex) {
        return this.headAtoms[headAtomIndex];
    }

    public Atom[] getBodyAtoms() {
        return this.bodyAtoms.clone();
    }

    public Atom[] getHeadAtoms() {
        return this.headAtoms.clone();
    }

    public Variable[] getFrontierVariables() {
        return this.frontierVariables.clone();
    }

    public Variable[] getHeadVariables() {
        return this.headVariables.clone();
    }

    public Variable[] getBodyVariables() {
        return this.bodyVariables.clone();
    }

    public int getNumberOfFrontierVariables() {
        return this.frontierVariables.length;
    }

    public static Dependency create(Atom[] body, Atom[] head) {
        return Cache.dependency.retrieve(new Dependency(body, head));
    }

    public String toViewString() {
        String bodyString = getAtomsString(this.getBodyAtoms());
        String headString = getAtomsString(this.getHeadAtoms());

        return headString + "<br>=  " + bodyString;
    }

    public String toTgdString() {
        StringBuilder bodyString = new StringBuilder(getAtomsString(this.getBodyAtoms()));
        StringBuilder headString = new StringBuilder(getAtomsString(this.getHeadAtoms()));

        return bodyString.toString() + "<br>\n  -> " + headString.toString();
    }

    private String getAtomsString(Atom[] atoms) {
        StringBuilder bodyString = new StringBuilder("");
        for (int i = 0; i < atoms.length; i++) {
            if (i != 0) {
                bodyString.append("  AND  ");
            }
            bodyString.append("<font color=blue>")
                    .append(atoms[i].getPredicate().toString())
                    .append("</font>")
                    .append("(");
            for(Variable var : atoms[i].getVariables())
                    bodyString.append(var).append(",");
            bodyString.setLength(bodyString.length() - 1);
            bodyString.append(")");
        }
        return bodyString.toString();
    }

    public Set<Relation> getRelations() {
        Set<Relation> relations = new HashSet<>();

        for(Atom at : this.bodyAtoms) {
            List<Attribute> attributes = new ArrayList<>();
            for(Term term : at.getTerms())
                attributes.add(Attribute.create(String.class, term.toString()));

            relations.add(Relation.create(at.getPredicate().getName(), attributes.toArray(new Attribute[0])));
        }
        return relations;
    }

    public String bodyToString() {
        StringBuilder bodyString = new StringBuilder();
        for(Atom atom : this.bodyAtoms)
            bodyString.append(atom).append("\n");
        bodyString.setLength(bodyString.length() - 1);
        return bodyString.toString();
    }

    public String headToString() {
        StringBuilder headString = new StringBuilder();
        for(Atom atom : this.headAtoms)
            headString.append(atom);
        return headString.toString();
    }
}
