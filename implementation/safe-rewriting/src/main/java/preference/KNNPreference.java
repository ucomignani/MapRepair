package preference;

import util.ResultComputeRewriting;
import uk.ac.ox.cs.pdq.fol.TGD;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;

public class KNNPreference implements Preference {

    private Classifier ibk;
    private Instances data;

    public KNNPreference(String dataFilename) throws Exception {
        BufferedReader datafile = readDataFile(dataFilename);

        this.data = new Instances(datafile);
        this.data.setClassIndex(data.numAttributes() - 1);

        this.ibk = new IBk(5);
        this.ibk.buildClassifier(data);
    }

    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }

    @Override
    public TGD prf(TGD dependencyOne, TGD dependencyTwo) {

        if (dependencyOne == null)
            return dependencyTwo;
        else if (dependencyTwo == null)
            return dependencyOne;
        else {
            ArrayList<Attribute> atts = new ArrayList<Attribute>();
            atts.add(this.data.attribute(0));
            atts.add(this.data.attribute(1));
            atts.add(this.data.attribute(2));
            Instances instances = new Instances("preference", atts, 0);
            instances.setClassIndex(instances.numAttributes() - 1);

            int deltaNbFrontier = dependencyOne.getNumberOfFrontierVariables() - dependencyTwo.getNumberOfFrontierVariables();

            int nbNonFrontierDep = dependencyOne.getBodyVariables().length - dependencyOne.getNumberOfFrontierVariables();
            int nbNonFrontierCurrentOutput = dependencyTwo.getBodyVariables().length - dependencyTwo.getNumberOfFrontierVariables();
            int nbNewVarNonFrontier = nbNonFrontierDep - nbNonFrontierCurrentOutput;

            double[] values = new double[]{deltaNbFrontier,nbNewVarNonFrontier,0};
            DenseInstance newInst = new DenseInstance(1.0,values);

            instances.add(newInst);
            newInst.setDataset(instances);

            double choice = 0;
            try {
                choice = ibk.classifyInstance(newInst);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (choice == 0)
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
            ArrayList<Attribute> atts = new ArrayList<Attribute>();
            atts.add(this.data.attribute(0));
            atts.add(this.data.attribute(1));
            atts.add(this.data.attribute(2));
            Instances instances = new Instances("preference", atts, 0);
            instances.setClassIndex(instances.numAttributes() - 1);

            int deltaNbFrontier = result1.getNbNewVarFrontier() - result2.getNbNewVarFrontier();
            int deltaNewVarNonFrontier = result1.getNbNewVarNonFrontier() - result2.getNbNewVarNonFrontier();

            double[] values = new double[]{deltaNbFrontier,deltaNewVarNonFrontier,0};
            DenseInstance newInst = new DenseInstance(1.0,values);

            instances.add(newInst);
            newInst.setDataset(instances);

            double choice = 0;
            try {
                choice = ibk.classifyInstance(newInst);
            } catch (Exception e) {
                e.printStackTrace();
            }

//            BufferedWriter writer = null;
//            try {
//                writer = new BufferedWriter(new FileWriter("runData.txt", true));
//                writer.append(deltaNbFrontier + ";" + deltaNewVarNonFrontier + ";" + choice);
//                writer.close();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            if (choice == 0)
                return result1;
        }

        return result2;
    }

}