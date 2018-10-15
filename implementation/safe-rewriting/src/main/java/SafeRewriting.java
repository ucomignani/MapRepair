import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import org.apache.log4j.Logger;
import preference.MaxFrontierPreference;
import preference.Preference;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.*;
import uk.ac.ox.cs.pdq.fol.*;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.VisibleChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import util.*;

import java.util.*;

public class SafeRewriting {

    private static Logger log = Logger.getLogger(SafeRewriting.class);

    /**
     * Needed to run the visible chase
     */
    private DatabaseChaseInstance state;
    private VisibleChaser visChaser;

    /**
     * The policy views, considered as the reference for the rewriting
     */
    private final Schema policyViews;

    /**
     * Result of the visible chase over critical instance of the policy views
     */
    private final Set<Atom> instanceRef;

    /**
     * Used for rewritings
     */
    private int newVarCount;
    private Preference preferences;

    /**
     * Constructor for SafeRewriting.
     *
     * @param policyViews the policy views used as reference for the rewriting
     */
    public SafeRewriting(Schema policyViews) {
        this.state = null;
        this.newVarCount = 0;
        this.visChaser = new VisibleChaser(new StatisticsCollector(true, new EventBus()));

        this.preferences = new MaxFrontierPreference();

        this.policyViews = policyViews;
        this.instanceRef = new HashSet<>();

        Set<Atom> facts = CriticalInstanceGenerator.generateCriticalInstance(policyViews);

        try {

            DatabaseManager databaseConnection = createConnection(DatabaseParameters.Postgres, policyViews);
            assert (databaseConnection != null);
            this.state = new DatabaseChaseInstance(facts, databaseConnection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        visChaser.reasonUntilTermination(state, policyViews.getAllDependencies());

        this.instanceRef.addAll(state.getFacts());
    }

    public SafeRewriting(Schema policyViews, Preference preferenceFunction) {
        this.state = null;
        this.newVarCount = 0;
        this.visChaser = new VisibleChaser(new StatisticsCollector(true, new EventBus()));

        this.preferences = preferenceFunction;

        this.policyViews = policyViews;
        this.instanceRef = new HashSet<>();

        Set<Atom> facts = CriticalInstanceGenerator.generateCriticalInstance(policyViews);

        try {

            DatabaseManager databaseConnection = createConnection(DatabaseParameters.Postgres, policyViews);
            assert (databaseConnection != null);
            this.state = new DatabaseChaseInstance(facts, databaseConnection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        visChaser.reasonUntilTermination(state, policyViews.getAllDependencies());

        this.instanceRef.addAll(state.getFacts());
    }

    /**
     * Rewrite an input mapping in order to return a mapping safe w.r.t. the policy views of SafeRewriting instantiation
     *
     * @param s the schema to repair
     * @param maxN the maximum number of rewritings before switching on a computation guaranteed to be finite
     * @return a safe schema w.r.t. the policy views od the schema used to create the instance of SafeRewritings, if there is no indirectly exported variables
     */
    public Schema repair(Schema s, int maxN){
        Schema rewritingTmp = fRepair(s);

        if(isSafe(rewritingTmp)) {
            return srepair(rewritingTmp, maxN);
        }

        return rewritingTmp;
    }

    /**
     * Detect and hide predicates that shouldn't be visible
     *
     * @param mappingToRewrite the mapping to rewrite
     * @return a rewritten mapping w.r.t. the visibility of predicates in the policy views
     */
    public Schema hidePredicates(Schema mappingToRewrite) {

        /* allowed predicates */
        Set<String> allowedPredicates = new HashSet<>();
        for (Relation r : this.policyViews.getRelations()) {
            allowedPredicates.add(r.getName());
        }

        /* filter predicates in mappingToRewrite tgds */
        Set<Dependency> newDependencies = new HashSet<>(Arrays.asList(mappingToRewrite.getKeyDependencies()));

        // filtering
        for (Dependency d : mappingToRewrite.getNonEgdDependencies()) {
            Set<Atom> newBody = new HashSet<>();
            for (Atom at : d.getBody().getAtoms()) {
                if (allowedPredicates.contains(at.getPredicate().toString())) {
                    newBody.add(at);
                }
            }
            if (!newBody.isEmpty()) {
                TGD newTgd = TGD.create(newBody.toArray(new Atom[0]),
                        d.getHead().getAtoms());
                newDependencies.add(newTgd);
            }
        }

        return new Schema(mappingToRewrite.getRelations(), newDependencies.toArray(new Dependency[0]));
    }


    public DatabaseChaseInstance getState() {
        return state;
    }

    public VisibleChaser getVisChaser() {
        return visChaser;
    }

    public Schema getPolicyViews() {
        return policyViews;
    }

    public Set<Atom> getInstanceRef() {
        return instanceRef;
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

    /**
     * Check if there exists morphisms from a body conjunction to the stored reference instance
     *
     * @param conjunction a formula
     * @return true if there exists a morphism from the body of the formula into the instance of reference
     */
    public boolean existsMorphismConjunction(Formula conjunction) {
        ConjunctiveQuery bodyQuery = ConjunctiveQuery.create(conjunction.getFreeVariables(), conjunction.getAtoms());
        List<Match> bodyMatches = state.getMatches(bodyQuery);
        return !bodyMatches.isEmpty();
    }

    /**
     * Check if there exists morphisms from a set of atoms to the stored reference instance
     *
     * @param atSet a set of atoms
     * @return true if there exists a morphism from the set of atoms into the instance of reference
     */
    public boolean existsMorphismConjunction(Set<Atom> atSet) {

        ConjunctiveQuery bodyQuery = instanceToConjunctiveQuery(atSet);
        List<Match> bodyMatches = state.getMatches(bodyQuery);

        return !bodyMatches.isEmpty();
    }

    /**
     * Check if a schema is safe w.r.t. the policy views used to instantiate SafeRewriting
     *
     * @param schemaToEvaluate schema to evaluate
     * @return true if schemaToEvaluate is safe w.r.t. SafeRewriting.instanceRef
     */
    public boolean isSafe(Schema schemaToEvaluate) {
        // compute the instance to compare
        SafeRewriting safeRewToCompare = new SafeRewriting(schemaToEvaluate);
        Set<Atom> instanceTmp = safeRewToCompare.getInstanceRef();

        // extract body predicates symbols
        Set<Predicate> compPredicates = new HashSet<>();
        for (Dependency dep : schemaToEvaluate.getAllDependencies()) {
            for (Atom at : dep.getBody().getAtoms())
                compPredicates.add(at.getPredicate());
        }

        // suppress head predicates and equalities
        Set<Atom> instanceToEvaluate = new HashSet<>(instanceTmp);
        for (Atom at : instanceTmp) {
            if (at.getPredicate().toString().equals("EQUALITY"))
                instanceToEvaluate.remove(at);
            else if (!compPredicates.contains(at.getPredicate()))
                instanceToEvaluate.remove(at);

        }

        ConjunctiveQuery bodyQuery = instanceToConjunctiveQuery(instanceToEvaluate);
        List<Match> bodyMatches = state.getMatches(bodyQuery);

        return !bodyMatches.isEmpty();
    }

    /**
     * Convert an instance to a conjunctive query
     *
     * @param instance a set of facts (Atom)
     * @return a conjunctive query
     */
    private ConjunctiveQuery instanceToConjunctiveQuery(Set<Atom> instance) {
        Set<Atom> newSetAt = new HashSet<>();
        for (Atom at : instance) {
            Variable[] vars = new Variable[at.getTerms().length];
            for (int i = 0; i < at.getTerms().length; i++)
                vars[i] = Variable.create(at.getTerms()[i].toString());
            newSetAt.add(Atom.create(at.getPredicate(), vars));
        }

        TGD dep = TGD.create(newSetAt.toArray(new Atom[0]), newSetAt.toArray(new Atom[0]));

        return ConjunctiveQuery.create(dep.getBody().getFreeVariables(), dep.getBody().getAtoms());
    }

    /**
     * Return the morphisms from a set of atoms into the stored reference instance
     *
     * @param atSet a set of atoms
     * @return the set of homomorphisms from the set of atoms into the instance of reference
     */
    private List<Match> returnMorphismConjunction(Set<Atom> atSet) {
        // extract variables
        Set<Variable> vars = new HashSet<>();
        for (Atom at : atSet) {
            vars.addAll(Arrays.asList(at.getVariables()));
        }

        ConjunctiveQuery bodyQuery = ConjunctiveQuery.create(vars.toArray(new Variable[0]), atSet.toArray(new Atom[0]));

        return state.getMatches(bodyQuery);
    }

    /**
     * Implementation of the rewriting algorithm for directly exported variables
     *
     * @param s a schema such that its tgds need to be rewrite w.r.t. the stored policy views
     * @return a safe schema w.r.t. the policy views od the schema used to create the instance of SafeRewritings, if there is no indirectly exported variables
     */
    public Schema fRepair(Schema s) {

        Set<Atom> instanceRef = this.getInstanceRef();
        Set<Dependency> outputDependencies = new HashSet<>();

        s = hidePredicates(s); // hide predicates that should not be visibles

        for (Dependency d : s.getNonEgdDependencies()) {

            Map<Atom, Atom> initialAtToNewAtom = new HashMap<>();
            Set<Atom> newInstance = initVarsRenamedInstance(d, initialAtToNewAtom);

            List<Match> mathchesNewAtoms = this.returnMorphismConjunction(newInstance);

            ResultComputeRewriting bestComputeRewriting = null;

            for (Match match : mathchesNewAtoms) {
                bestComputeRewriting = computeMatch(d, initialAtToNewAtom, bestComputeRewriting, match);
            }

            assert bestComputeRewriting != null;
            TGD newDependency = TGD.create(bestComputeRewriting.getSetAtoms().toArray(new Atom[0]),
                    d.getHead().getAtoms());
            outputDependencies.add(newDependency);
        }

        return new Schema(s.getRelations(),
                outputDependencies.toArray(new Dependency[0]));
    }

    /**
     * Create an instance from the body of an input dependency. Update the map from atoms to facts which is given in argument.
     *
     * @param d initial dependency
     * @param initialAtToNewAtom the map from atoms to the facts generated by the function
     * @return an instance generated from the body of d. initialAtToNewAtom is updated during the process.
     */
    private Set<Atom> initVarsRenamedInstance(Dependency d, Map<Atom, Atom> initialAtToNewAtom) {

        Set<Atom> atSet = new HashSet<>();
        int countVariable = 0;
        Map<Term, Variable> termToNewVar = new HashMap<>();

        for (Atom at : d.getBody().getAtoms()) {

            Predicate atPred = at.getPredicate();
            Term[] newTerms = new Term[at.getTerms().length];
            for (int i = 0; i < at.getTerms().length; i++) {
                newTerms[i] = Variable.create("v" + countVariable);
                countVariable++;
            }

            Atom newAtom = Atom.create(atPred, newTerms);
            initialAtToNewAtom.put(at, newAtom);
            atSet.add(newAtom);
        }
        return atSet;
    }

    /**
     * Apply rewriting for a given match and return the best rewriting between the one given in input and the one which is computed
     *
     * @param d the dependency to rewrite
     * @param initialAtToNewAtom
     * @param bestComputeRewriting best computed rewriting until now
     * @param match the match to apply
     * @return the best rewriting between bestComputeRewriting and the one corresponding to match
     */
    private ResultComputeRewriting computeMatch(Dependency d,
                                                Map<Atom, Atom> initialAtToNewAtom,
                                                ResultComputeRewriting bestComputeRewriting,
                                                Match match) {
        ResultComputeRewriting resultComputeRewriting;
        resultComputeRewriting = computeRewriting(d, initialAtToNewAtom, match);

        return this.preferences.prf(resultComputeRewriting, bestComputeRewriting);
    }

    /**
     * Apply rewriting for a given match
     *
     * @param d the dependency to rewrite
     * @param initialAtToNewAtom
     * @param matchNewAtoms the match to apply
     * @return compute the rewriting corresponding to matchNewAtoms
     */
    private ResultComputeRewriting computeRewriting(Dependency d, Map<Atom, Atom> initialAtToNewAtom, Match matchNewAtoms) {

        // initialize vars positions
        VariablePositionMultimap varPositions = new VariablePositionMultimap(d.getBodyAtoms());

        Set<Variable> frontierVariables = new HashSet<>(Arrays.asList(d.getBodyVariables()));
        frontierVariables.retainAll(Arrays.asList(d.getHeadVariables()));

        // initialize two morphisms
        Multimap<Variable, Variable> varToVar = HashMultimap.create();
        Map<Variable, Constant> varToConst = new HashMap<>();

        Set<Atom> outputAtomSet = new HashSet<>();

        // statistics
        int nbNewNonFrontier = 0;
        int nbNewFrontier = 0;
        newVarCount = 0;


        for (Atom at : d.getBody().getAtoms()) {
            // prepare creation of the new atom
            Term[] newTerms = new Term[at.getTerms().length];

            for (VariablePosition vp : varPositions.get(at)) {
                Variable atVar = Variable.create(at.getTerms()[vp.getPositionInAtom()].toString());
                Variable matchVar = Variable.create(getNewVarCorrespondingToPosition(at, vp, initialAtToNewAtom).toString());
                Constant matchConst = matchNewAtoms.getMapping().get(matchVar);

                // keep information about all old vars names, in order to generate really fresh vars
                Set<Variable> oldVariables = new HashSet<>(Arrays.asList(d.getBodyVariables()));
                oldVariables.addAll(Arrays.asList(d.getHeadVariables()));
                if (matchConst.toString().equals("*")) {
                    newTerms[vp.getPositionInAtom()] = atVar; // nothing changed
                } else if (frontierVariables.contains(vp.getVariable())) {
                    nbNewFrontier = computeFrontierVariable(varToVar, varToConst, nbNewFrontier, newTerms, vp, atVar, matchConst, oldVariables);
                } else
                    nbNewNonFrontier = computeNonFrontierVariable(varToVar, varToConst, nbNewNonFrontier, newTerms, vp, atVar, matchConst, oldVariables);

                // newVarCount++;
            }
            Atom outputAtom = Atom.create(at.getPredicate(), newTerms);
            outputAtomSet.add(outputAtom);
        }

        return new ResultComputeRewriting(outputAtomSet, nbNewFrontier, nbNewNonFrontier);
    }

    /**
     * Compute the rewriting for a frontier variable
     *
     * @param varToVar
     * @param varToConst
     * @param nbNewFrontier
     * @param newTerms
     * @param vp
     * @param atVar
     * @param matchConst
     * @param oldVariables
     * @return the number of frontier variables in the new rewriting. Results of the rewriting are returned using side effect with newTerms.
     */
    private int computeFrontierVariable(Multimap<Variable, Variable> varToVar, Map<Variable, Constant> varToConst, int nbNewFrontier, Term[] newTerms, VariablePosition vp, Variable atVar, Constant matchConst, Set<Variable> oldVariables) {
        boolean alreadyExists = false;

        if (varToVar.containsKey(atVar)) { // if there exists a "good" fresh variable, use it
            for (Variable var : varToVar.get(atVar)) {
                if (matchConst == varToConst.get(var)) {
                    newTerms[vp.getPositionInAtom()] = var;
                    alreadyExists = true;
                }
            }
        }

        if (!alreadyExists) { // else generate a fresh var
            nbNewFrontier = introduceNewFreshVariable(varToVar, varToConst, nbNewFrontier, newTerms, vp, atVar, matchConst, oldVariables);
        }
        return nbNewFrontier;
    }

    /**
     * Compute the rewriting for a non-frontier variable
     *
     * @param varToVar
     * @param varToConst
     * @param nbNewNonFrontier
     * @param newTerms
     * @param vp
     * @param atVar
     * @param matchConst
     * @param oldVariables
     * @return the number of non-frontier variables in the new rewriting. Results of the rewriting are returned using side effect with newTerms.
     */
    private int computeNonFrontierVariable(Multimap<Variable, Variable> varToVar,
                                           Map<Variable, Constant> varToConst,
                                           int nbNewNonFrontier, Term[] newTerms,
                                           VariablePosition vp,
                                           Variable atVar,
                                           Constant matchConst,
                                           Set<Variable> oldVariables) {

        boolean alreadyExists = false;

        if (!varToVar.containsKey(atVar)) {
            nbNewNonFrontier = introduceNewFreshVariable(varToVar, varToConst, nbNewNonFrontier, newTerms, vp, atVar, matchConst, oldVariables);
        } else if (varToVar.containsKey(atVar)) {           // if there exists a "good" fresh variable, use it
            for (Variable var : varToVar.get(atVar)) {
                if (matchConst == varToConst.get(var)) {
                    newTerms[vp.getPositionInAtom()] = var;
                    alreadyExists = true;
                }
            }
        }
        if (!alreadyExists) {                               // else generate a fresh var
            nbNewNonFrontier = introduceNewFreshVariable(varToVar, varToConst, nbNewNonFrontier, newTerms, vp, atVar, matchConst, oldVariables);
        }
        return nbNewNonFrontier;
    }

    private int introduceNewFreshVariable(Multimap<Variable, Variable> varToVar,
                                          Map<Variable, Constant> varToConst,
                                          int nbNewFrontier, Term[] newTerms,
                                          VariablePosition vp, Variable atVar,
                                          Constant matchConst,
                                          Set<Variable> oldVariables) {
        Variable newVar = generateFreshVariable(atVar, varToVar, oldVariables);
        varToVar.put(atVar, newVar);
        varToConst.put(newVar, matchConst);
        newTerms[vp.getPositionInAtom()] = newVar;
        nbNewFrontier++;

        return nbNewFrontier;
    }

    private Variable generateFreshVariable(Variable atVar,
                                           Multimap<Variable, Variable> varToVar,
                                           Set<Variable> oldVariables) {
        Variable newVar;

        do { // check if the produced variable already exist
            String newVarName = atVar.toString() + newVarCount;
            newVar = Variable.create(newVarName);

            if (varToVar.containsValue(newVar) || oldVariables.contains(newVar))
                newVar = null;

            newVarCount++;
        } while (newVar == null);

        return newVar;
    }

    private Term getNewVarCorrespondingToPosition(Atom at, VariablePosition
            vp, Map<Atom, Atom> initialAtToNewAtom) {
        Atom newAtom = initialAtToNewAtom.get(at);
        return newAtom.getTerms()[vp.getPositionInAtom()];
    }

    /**
     * Generate the provenance graph of the VisChase over the critical instance of a given schema
     *
     * @param s the reference schema
     * @return a provenance graph
     */
    public ProvenanceGraph provenance(Schema s) {
        DatabaseChaseInstance state = null;
        RestrictedChaser chaser = new RestrictedChaser(new StatisticsCollector(true, new EventBus()));
        Set<Atom> critS = CriticalInstanceGenerator.generateCriticalInstance(s);
        try {
            DatabaseManager databaseConnection = createConnection(DatabaseParameters.Postgres, s);
            assert (databaseConnection != null);
            state = new DatabaseChaseInstance(critS, databaseConnection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        chaser.reasonUntilTermination(state, s.getAllDependencies());
 //       log.info("Prov test : " + s.getAllDependencies().length);
        state.deleteFacts(critS);
 //       log.info("Prov test : " + state.getFacts());

        Set<Atom> instanceTmp = new HashSet<>(state.getFacts());
        Set<Atom> previousInstanceTmp = null;

        ProvenanceGraph gamma = new ProvenanceGraph();
        Set<Dependency> depEGD = new HashSet<>();
        Set<Dependency> delta = null;
        Map<Dependency, TGD> egdToTgd = new HashMap<>();

        int i = 0;

        do {

            if (i == 0) {
                Dependency[] dependencies = s.getAllDependencies();
                Dependency[] inverseDependencies = new Dependency[dependencies.length];
                for (int dependencyIndex = 0; dependencyIndex < dependencies.length; ++dependencyIndex) {
                    Dependency dependency = dependencies[dependencyIndex];
                    inverseDependencies[dependencyIndex] = TGD.create(dependency.getHeadAtoms(), dependency.getBodyAtoms());
                }
                delta = new HashSet<>(Arrays.asList(inverseDependencies));
            } else {
                previousInstanceTmp = instanceTmp;
                delta = depEGD;
            }

            int j = 0;
            int countVal = 0;

            for (Dependency dependency : delta) {
                assert (dependency instanceof TGD || dependency instanceof EGD);

                Set<Match> matches = this.returnMorphismConjunctionWithInstance(s, instanceTmp, dependency.getBody());
                for (Match match : matches) {
                    if (dependency instanceof TGD) {

                        Map<Variable, Constant> newMapping = match.getMapping();
                        for (Variable var : dependency.getExistential()) {
                            String newName = "c" + countVal;
                            newMapping.put(var, TypedConstant.create(newName));
                            ++countVal;
                        }

                        Match newMatch = Match.create(dependency, newMapping);

                        //Set<Atom> newInstanceBody = applyMatch(dependency.getBody().getAtoms(), newMatch);
                        Set<Atom> newInstanceHead = applyMatch(dependency.getHead().getAtoms(), newMatch);

                        instanceTmp.addAll(newInstanceHead);

                        // generate new util.Config
                        ConfigID configID = new ConfigID(i, j);
                        TGD initialTGD = TGD.create(dependency.getHeadAtoms(), dependency.getBodyAtoms());
                        MetaTrigger metaTrigger = new MetaTrigger(match, initialTGD);
                        Config newConfig = new Config(configID, metaTrigger, newInstanceHead);

                        gamma.add(i, j, newConfig);

                    } else {
                        Set<Atom> newAtoms = new HashSet<>();
                        Set<Atom> oldAtoms = new HashSet<>();

                        oldAtoms.addAll(applyMatch(dependency.getBody().getAtoms(), match));

                        for (Atom instanceAtom : oldAtoms) {
                            Term[] newTerms = instanceAtom.getTerms().clone();
                            for (Atom equalityAtom : dependency.getHead().getAtoms()) {
                                for (int k = 0; k < instanceAtom.getTerms().length; k++) {
                                    if (newTerms[k] == equalityAtom.getTerms()[1]) {
                                        newTerms[k] = TypedConstant.create(equalityAtom.getTerms()[0].toString());
                                    }
                                }
                            }
                            newAtoms.add(Atom.create(instanceAtom.getPredicate(), newTerms));
                        }

                        instanceTmp.removeAll(oldAtoms);
                        instanceTmp.addAll(newAtoms);

                        oldAtoms.removeAll(newAtoms);
                        if (!oldAtoms.isEmpty()) {
                            // generate new util.Config
                            ConfigID configID = new ConfigID(i, j);
                            TGD initialTGD = egdToTgd.get(dependency);
                            Set<Atom> equalities = new HashSet<>(Arrays.asList(dependency.getHead().getAtoms()));
                            MetaTrigger metaTrigger = new MetaTrigger(match, equalities, initialTGD);

                            Config newConfig = new Config(configID, metaTrigger, newAtoms);

                            gamma.add(i, j, newConfig);

                            gamma = updateParentConfigs(configID, gamma, oldAtoms);

                        }
                    }

                    j++;

                }
            }

            if (i == 0) {
                for (Dependency dependency : delta) {
                    TGD initialTGD = TGD.create(dependency.getHeadAtoms(), dependency.getBodyAtoms());
                    Set<Match> matches = this.returnMorphismConjunctionWithInstance(s, instanceTmp, dependency.getHead());
                    for (Match match : matches) {
                        Set<Atom> atSetEGD = new HashSet<>();
                        for (Variable var : match.getMapping().keySet()) {
                            Set<Variable> frontierVars = new HashSet<>(Arrays.asList(dependency.getFrontierVariables()));
                            if (frontierVars.contains(var) && !match.getMapping().get(var).toString().equals("*")) {
                                atSetEGD.add(Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),
                                        Variable.create("*"),
                                        match.getMapping().get(var)));

                            }
                        }

                        if (atSetEGD.size() > 0) {
                            EGD newEGD = EGD.create(dependency.getHeadAtoms(), atSetEGD.toArray(new Atom[0]), false);
                            depEGD.add(newEGD);
                            egdToTgd.put(newEGD, initialTGD);
                        }
                    }

                }
            }
            i++;
        } while (!instanceTmp.equals(previousInstanceTmp));

        return gamma;
    }


    /**
     * Extract set of configuration for which metaTrigger is an immediate descendant
     *
     * @param currentConfigID onfiguration for which we search descendants
     * @param gamma current provenance graph
     * @param parentAtoms
     * @return a set of config IDs
     */
    private ProvenanceGraph updateParentConfigs(ConfigID currentConfigID, ProvenanceGraph gamma, Set<Atom> parentAtoms) {

        for (Atom at : parentAtoms) {
            for (ConfigID confID : gamma.getAtomProvenance().get(at)) {
                if (confID.getI() < currentConfigID.getI())
                    gamma.addDescendantToConfig(confID, currentConfigID);
            }
        }

        return gamma;
    }

    /**
     * Apply a match over atoms
     *
     * @param atoms an array of atoms
     * @param match a match to apply
     * @return the result of applying match over atoms
     */
    private Set<Atom> applyMatch(Atom[] atoms, Match match) {
        Set<Atom> atomSet = new HashSet<>();
        Map<Variable, Constant> mapping = match.getMapping();

        for (Atom atom : atoms) {
            Term[] oldTerms = atom.getTerms();
            Term[] newTerms = new Term[oldTerms.length];
            for (int i = 0; i < oldTerms.length; i++) {
                newTerms[i] = mapping.get(oldTerms[i]);
            }
            atomSet.add(Atom.create(atom.getPredicate(), newTerms));
        }
        return atomSet;
    }

    private Set<Match> returnMorphismConjunctionWithInstance(Schema s, Set<Atom> inputSet, Formula inputDep) {

        DatabaseChaseInstance state = null;
        try {
            DatabaseManager databaseConnection = createConnection(DatabaseParameters.Postgres, s);
            assert (databaseConnection != null);
            state = new DatabaseChaseInstance(inputSet, databaseConnection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // extract variables
        Set<Variable> vars = new HashSet<>();
        for (Atom at : inputDep.getAtoms()) {
            vars.addAll(Arrays.asList(at.getVariables()));
        }

        ConjunctiveQuery bodyQuery = ConjunctiveQuery.create(vars.toArray(new Variable[0]), inputDep.getAtoms());
        return new HashSet<>(state.getMatches(bodyQuery));
    }

    /**
     * Rewriting for undirectly exported variables
     *
     * @param inputMapping the schema to rewrite
     * @param maxN the maximum number of rewritings before switching on a computation guaranteed to be finite
     * @return a schema safe w.r.t. to the policy views
     */
    public Schema srepair(Schema inputMapping, int maxN) {

        Set<Dependency> mappingDependencies = new HashSet<>(Arrays.asList(inputMapping.getAllDependencies()));

        ProvenanceGraph provenance = provenance(inputMapping);
        Set<Dependency> previousSigmaTmp = new HashSet<>(Arrays.asList(inputMapping.getAllDependencies()));
        Set<Dependency> sigmaTmp = null;
        int i = 0;
        boolean cont = false;
//log.info("Provenance : " + provenance.getProvenance().isEmpty() + " " + provenance);
        for (Config groundConfig : provenance.getProvenance().get(0).values()) {
//log.info(groundConfig);
            List<ConfigID> configList = new ArrayList<>();
            //configList.add(groundConfig.configID);
            Set<List<ConfigID>> wellFormedMinSeqSet = new HashSet<>();
            extractWellFormedMinSequences(groundConfig.getConfigID(), provenance, configList, wellFormedMinSeqSet);
//log.info("WF sequence size : " + wellFormedMinSeqSet.size());

            for (List<ConfigID> wFMSequence : wellFormedMinSeqSet) {
                do {
//log.info("do srepair");

                    sigmaTmp = previousSigmaTmp;
                    cont = false;
                    ConfigID lastConfig = wFMSequence.get(wFMSequence.size() - 1);

                    if (i >= maxN) {
                        if (provenance.get(lastConfig).getDescendantConfigSet().isEmpty()) {

                            Dependency newTgd = hideFrn(groundConfig, wFMSequence, provenance);

                            mappingDependencies.remove(provenance.get(lastConfig).getCurrentMetaTrigger().getDep());
                            mappingDependencies.add(newTgd);
                        } else {
                            mappingDependencies.remove(provenance.get(lastConfig).getCurrentMetaTrigger().getDep());
                        }
                    } else {
                        mappingDependencies.remove(provenance.get(lastConfig).getCurrentMetaTrigger().getDep());

                        TGD newTgdHF = hideFrn(groundConfig, wFMSequence, provenance);
                        TGD newTgdEV = elimVars(groundConfig, provenance.get(wFMSequence.get(0)), inputMapping);

                        Dependency choosenDep = preferences.prf(newTgdHF, newTgdEV);

                        if (choosenDep == newTgdHF) {
                            mappingDependencies.remove(provenance.get(lastConfig).getCurrentMetaTrigger().getDep());
                        } else {
                            mappingDependencies.remove(wFMSequence.get(0));
                        }

                        mappingDependencies.add(choosenDep);
                    }

                    i++;
                } while (cont && i < maxN);
            }


        }
//        log.info("end srepair");

        return new Schema(inputMapping.getRelations(), mappingDependencies.toArray(new Dependency[0]));
    }

    private TGD elimVars(Config firstConfig, Config secondConfig, Schema mapping) {

        Dependency firstDep = firstConfig.getCurrentMetaTrigger().getDep();
        Dependency secondDep = secondConfig.getCurrentMetaTrigger().getDep();

        Set<Atom> firstBody = new HashSet<>(Arrays.asList(firstDep.getBodyAtoms().clone()));
        Set<Atom> secondBody = new HashSet<>(Arrays.asList(secondDep.getBodyAtoms().clone()));

        Set<Match> bodyMatches = returnMatchBetweenAtomSets(firstBody, secondBody, mapping);

        if (bodyMatches.isEmpty())
            return null;
        else {
            TGD preferredDependency = null;
            TGD tmpDependency = null;
            for (Match match : bodyMatches) {
                for (Variable var : match.getMapping().keySet()) {
                    if (numbVarOccurencesInAtomSet(var, secondBody) > 1) {
                        tmpDependency = renameRepeatedVar(var, secondDep);
                        preferredDependency = preferences.prf(preferredDependency, tmpDependency);
                    }
                }
            }

            return preferredDependency;
        }
    }

    private TGD renameRepeatedVar(Variable var, Dependency dependency) {
        Set<Variable> oldVars = new HashSet<>(Arrays.asList(dependency.getBodyVariables().clone()));
        oldVars.addAll(Arrays.asList(dependency.getHeadVariables().clone()));

        Variable newVariable = null;
        int countVar = 0;

        Set<Atom> newBody = new HashSet<>();

        for (Atom atomTgd : dependency.getBody().getAtoms().clone()) {
            List<Variable> newVars = new ArrayList<>();
            for (Variable varAtom : atomTgd.getFreeVariables()) {
                if (varAtom == var) {
                    do {
                        newVariable = Variable.create(var.toString() + countVar);
                        countVar++;
                    } while (oldVars.contains(newVariable));

                    newVars.add(newVariable);
                } else
                    newVars.add(varAtom);
            }
            newBody.add(Atom.create(atomTgd.getPredicate(), newVars.toArray(new Variable[0])));
        }

        return TGD.create(newBody.toArray(new Atom[0]), dependency.getHead().getAtoms());
    }

    private int numbVarOccurencesInAtomSet(Variable var, Set<Atom> atomSet) {
        int count = 0;

        for (Atom atom : atomSet)
            for (Term term : atom.getTerms())
                if (term.toString().equals(var.toString()))
                    count++;

        return count;
    }

    private Set<Match> returnMatchBetweenAtomSets(Set<Atom> fromSet, Set<Atom> toSet, Schema mapping) {

        DatabaseChaseInstance newState = null;

        Set<Atom> facts = atomsToFacts(toSet);
        try {

            DatabaseManager databaseConnection = createConnection(DatabaseParameters.Postgres, mapping);
            assert (databaseConnection != null);
            newState = new DatabaseChaseInstance(facts, databaseConnection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConjunctiveQuery fromQuery = instanceToConjunctiveQuery(fromSet);
        List<Match> fromMatches = newState.getMatches(fromQuery);

        return new HashSet<>(fromMatches);
    }

    /**
     *
     *
     * @param
     * @param
     * @return
     */
    private TGD hideFrn(Config groundConfig, List<ConfigID> wFMSequence, ProvenanceGraph provenance) {
        Config lastConfBeforeViolation = null;

        if (wFMSequence.size() > 1)
            lastConfBeforeViolation = provenance.get(wFMSequence.get(wFMSequence.size() - 2));
        else
            lastConfBeforeViolation = groundConfig;

        Set<TGD> mappingDependencies = new HashSet<>();

        List<Match> matches = returnMorphismConjunction(factsToAtoms(lastConfBeforeViolation.getNewFacts()));

        MetaTrigger mt = provenance.get(wFMSequence.get(wFMSequence.size() - 1)).getCurrentMetaTrigger();
        Set<Variable> frontierVars = new HashSet<>(Arrays.asList(mt.getDep().getFrontierVariables()));

        for (Match match : matches) {
            TGD tgdPrime = mt.getDep();
            Map<Variable, Constant> mapping = match.getMapping();

            for (Variable varX : mapping.keySet())
                if (!mapping.get(varX).toString().equals("*"))
                    for (Variable varY : mt.getMatch().getMapping().keySet())
                        if (mt.getMatch().getMapping().get(varY).toString() == varX.toString()
                                && frontierVars.contains(varY))
                            tgdPrime = renameVarInBody(varY, tgdPrime);
            if (tgdPrime != mt.getDep())
                mappingDependencies.add(tgdPrime);
        }
        return preferences.prf(mappingDependencies);
    }

    private TGD renameVarInBody(Variable initialVar, TGD dependency) {
        Set<Variable> oldVars = new HashSet<>(Arrays.asList(dependency.getBodyVariables()));
        oldVars.addAll(Arrays.asList(dependency.getHeadVariables()));

        Variable newVariable = null;
        int countVar = 0;

        do {
            newVariable = Variable.create(initialVar.toString() + countVar);
            countVar++;
        } while (oldVars.contains(newVariable));

        Set<Atom> newBody = new HashSet<>();

        for (Atom atomTgd : dependency.getBody().getAtoms()) {
            List<Variable> newVars = new ArrayList<>();
            for (Variable varAtom : atomTgd.getFreeVariables()) {
                if (varAtom == initialVar)
                    newVars.add(newVariable);
                else
                    newVars.add(varAtom);
            }
            newBody.add(Atom.create(atomTgd.getPredicate(), newVars.toArray(new Variable[0])));
        }

        return TGD.create(newBody.toArray(new Atom[0]), dependency.getHead().getAtoms());
    }

    private void extractWellFormedMinSequences(ConfigID currentConfigID, ProvenanceGraph provenance, List<ConfigID> currentList, Set<List<ConfigID>> currentWFMS) {

        for (ConfigID configID : provenance.get(currentConfigID).getDescendantConfigSet()) {
            Config config = provenance.get(configID);
            List<ConfigID> listTmp = new ArrayList<>(currentList);
            listTmp.add(configID);

            if (!existsMorphismConjunction(config.getNewFacts())) {
                currentWFMS.add(listTmp);
            } else {
                extractWellFormedMinSequences(configID, provenance, listTmp, currentWFMS);
            }
        }
    }

    private Set<Atom> factsToAtoms(Set<Atom> facts) {
        Set<Atom> atoms = new HashSet<>();

        for (Atom fact : facts)
            atoms.add(factToAtom(fact));

        return atoms;
    }

    private Atom factToAtom(Atom fact) {
        Term[] oldTerms = fact.getTerms();
        Variable[] newVariables = new Variable[oldTerms.length];
        for (int i = 0; i < oldTerms.length; i++) {
            newVariables[i] = Variable.create(oldTerms[i].toString());
        }

        return Atom.create(fact.getPredicate(), newVariables);
    }

    private Set<Atom> atomsToFacts(Set<Atom> atoms) {
        Set<Atom> facts = new HashSet<>();

        for (Atom atom : atoms)
            facts.add(atomToFact(atom));

        return facts;
    }

     private Atom atomToFact(Atom atom) {
        Term[] oldTerms = atom.getTerms();
        Constant[] newConstants = new Constant[oldTerms.length];
        for (int i = 0; i < oldTerms.length; i++) {
            newConstants[i] = TypedConstant.create(oldTerms[i].toString());
        }

        return Atom.create(atom.getPredicate(), newConstants);
    }
}
