package preference;

import util.ResultComputeRewriting;
import uk.ac.ox.cs.pdq.fol.TGD;

import java.util.Set;

public interface Preference {
    TGD prf(TGD TGDOne, TGD TGDTwo);
    TGD prf(Set<TGD> TGDSet);
    ResultComputeRewriting prf(ResultComputeRewriting result1, ResultComputeRewriting result2);
}
