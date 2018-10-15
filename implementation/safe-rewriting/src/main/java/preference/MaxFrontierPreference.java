package preference;

import util.ResultComputeRewriting;
import uk.ac.ox.cs.pdq.fol.TGD;

import java.util.Set;

public class MaxFrontierPreference implements Preference {
    @Override
    public TGD prf(TGD dependencyOne, TGD dependencyTwo) {
        if (dependencyOne == null)
            return dependencyTwo;
        else if (dependencyTwo == null
                || dependencyOne.getNumberOfFrontierVariables() > dependencyTwo.getNumberOfFrontierVariables())
            return dependencyOne;
        else if (dependencyOne.getNumberOfFrontierVariables() == dependencyTwo.getNumberOfFrontierVariables()) {
            int nbNonFrontierDep = dependencyOne.getBodyVariables().length - dependencyOne.getNumberOfFrontierVariables();
            int nbNonFrontierCurrentOutput = dependencyTwo.getBodyVariables().length - dependencyTwo.getNumberOfFrontierVariables();

            if (nbNonFrontierDep < nbNonFrontierCurrentOutput)
                return dependencyOne;
        }
        return dependencyTwo;
    }

    @Override
    public TGD prf(Set<TGD> TGDSet) {
        TGD outputDep = null;

        for (TGD dep : TGDSet) {
            if (outputDep == null)
                outputDep = dep;
            else
                outputDep = prf(outputDep, dep);
        }
        return outputDep;
    }

    @Override
    public ResultComputeRewriting prf(ResultComputeRewriting result1, ResultComputeRewriting result2) {
        if (result2 == null) {
            return result1;
        } else if (result2.getNbNewVarFrontier() > result1.getNbNewVarFrontier()
                ||
                (result2.getNbNewVarFrontier() == result1.getNbNewVarFrontier()
                        && result2.getNbNewVarNonFrontier() > result1.getNbNewVarNonFrontier()
                )) {
            return result1;
        }
        
        return result2;
    }
}