package preference;

import util.ResultComputeRewriting;
import uk.ac.ox.cs.pdq.fol.TGD;

import java.util.Set;

public class MaxAvgValuesPreference implements Preference {
    @Override
    public TGD prf(TGD dependencyOne, TGD dependencyTwo) {
        if (dependencyOne == null)
            return dependencyTwo;
        else if (dependencyTwo == null)
            return dependencyOne;
        else {
            int nbNonFrontierDep = dependencyOne.getBodyVariables().length - dependencyOne.getNumberOfFrontierVariables();
            int nbNonFrontierCurrentOutput = dependencyTwo.getBodyVariables().length - dependencyTwo.getNumberOfFrontierVariables();

            double averageDelta = (nbNonFrontierDep + nbNonFrontierCurrentOutput) / 2;

            if (averageDelta < 0)
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
        } else {

            int deltaNbFrontier = result1.getNbNewVarFrontier() - result2.getNbNewVarFrontier();
            int deltaNewVarNonFrontier = result1.getNbNewVarNonFrontier() - result2.getNbNewVarNonFrontier();

            double averageDelta = (deltaNbFrontier + deltaNewVarNonFrontier) / 2;

            if (averageDelta < 0)
                return result1;
        }

        return result2;
    }
}