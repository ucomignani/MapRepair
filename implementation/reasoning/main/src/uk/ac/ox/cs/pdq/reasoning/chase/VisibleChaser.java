package uk.ac.ox.cs.pdq.reasoning.chase;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.*;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.TriggerProperty;

import java.util.*;


/**
 * The facts that are generated during chasing are stored in a list.
 *
 * @author Efthymia Tsamoura
 */
public class VisibleChaser extends Chaser {

    public StatisticsCollector statistics = null;

    public VisibleChaser() {
        super(new StatisticsCollector(true, new EventBus()));
        this.statistics = super.statistics;
    }

    public VisibleChaser(StatisticsCollector statistics) {
        super(statistics);
    }

    /**
     * Chases the input state until termination.
     *
     * @param <S>          the generic type
     * @param instance     the instance
     * @param dependencies the dependencies
     */
    @Override
    public <S extends ChaseInstance> void reasonUntilTermination(S instance, Dependency[] dependencies) {
        Preconditions.checkArgument(instance instanceof ChaseInstance);
        RestrictedChaser restrictedChaser = new RestrictedChaser(this.statistics);
        Collection<Atom> factsOfInputInstance = instance.getFacts();
        restrictedChaser.reasonUntilTermination(instance, dependencies);
        instance.deleteFacts(factsOfInputInstance);
        //Create the inverse dependencies
        //The implementation assumes that we only have TGDs in the input
        Dependency[] inverseDependencies = new Dependency[dependencies.length];
        for (int dependencyIndex = 0; dependencyIndex < dependencies.length; ++dependencyIndex) {
            Dependency dependency = dependencies[dependencyIndex];
            inverseDependencies[dependencyIndex] = TGD.create(dependency.getHeadAtoms(), dependency.getBodyAtoms());
        }
        restrictedChaser.reasonUntilTermination(instance, inverseDependencies);

        Set<Dependency> newEGDsSet = new LinkedHashSet<>();
        for (int dependencyIndex = 0; dependencyIndex < dependencies.length; ++dependencyIndex) {
            Dependency dependency = dependencies[dependencyIndex];
            ConjunctiveQuery headQuery = ConjunctiveQuery.create(dependency.getHeadVariables(), dependency.getHeadAtoms());
            ConjunctiveQuery bodyQuery = ConjunctiveQuery.create(dependency.getBodyVariables(), dependency.getBodyAtoms());
            List<Match> headMatches = instance.getMatches(headQuery);
            List<Match> bodyMatches = instance.getMatches(bodyQuery);
            Preconditions.checkArgument(headMatches.size() <= 1);
            if (headMatches.size() == 1) {
                Map<Variable, Constant> headSubstitution = headMatches.get(0).getMapping();
                for (Match bodyMatch : bodyMatches) {
                    Map<Variable, Constant> bodySubstitution = bodyMatch.getMapping();
                    Map<Variable, Constant> substitutionOfFrontierVariables = new LinkedHashMap<>();
                    for (Variable variable : dependency.getFrontierVariables())
                        substitutionOfFrontierVariables.put(variable, bodySubstitution.get(variable));
                    if (!substitutionOfFrontierVariables.equals(headSubstitution)) {
                        Atom[] headAtoms = new Atom[dependency.getNumberOfFrontierVariables()];
                        int variableIndex = 0;
                        for (Variable variable : dependency.getFrontierVariables())
                            headAtoms[variableIndex++] =
                                    Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), variable, headSubstitution.get(variable));
                        newEGDsSet.add(EGD.create(dependency.getBodyAtoms(), headAtoms));
                    }
                }
            }
        }

        Collection<Atom> facts = instance.getFacts();
        Dependency[] newEGDs = newEGDsSet.toArray(new Dependency[newEGDsSet.size()]);
        Preconditions.checkArgument(instance instanceof ChaseInstance);
        boolean appliedStep = false;
        Dependency[] d = newEGDs;
        do {
            appliedStep = false;
            for (Dependency dependency : d) {
                List<Match> matches = instance.getTriggers(new Dependency[]{dependency}, TriggerProperty.ALL);
                if (!matches.isEmpty()) {
                    instance.chaseStep(matches);
                    if (facts.equals(instance.getFacts()))
                        appliedStep = true;
                }
            }
        } while (appliedStep);
    }


    /* (non-Javadoc)
     * @see uk.ac.ox.cs.pdq.reasoning.chase.Chaser#clone()
     */
    @Override
    public VisibleChaser clone() {
        return new VisibleChaser();
    }
}