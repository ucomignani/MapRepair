import org.apache.commons.cli.*;
import org.openjdk.jmh.runner.options.CommandLineOptionException;
import preference.KNNPreference;
import preference.MaxAvgValuesPreference;
import preference.MaxFrontierPreference;
import preference.Preference;
import uk.ac.ox.cs.pdq.db.Schema;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

public class RepairMain {

    /**
     * Main
     * <p>
     * first parameter is the policy views of reference
     * second parameter is the schema to repair
     * third parameter is the name of the output file
     * fourth parameter is the maximum number of rewritings before switching on a computation guaranteed to be finite
     */
    public static void main(String[] args) {

        Options options = getOptions();

        CommandLine line = getCommandLine(args, options);

        SafeRewriting safeRew = null;
        Preference preference = null;
        File policyViews = null;
        File schema = null;
        File outputFile = null;
        int maxN = 0;
        try {
            if (line.hasOption("v")) {
                policyViews = new File(line.getOptionValue("v"));
            } else {
                throw new Exception("Please provide policy views in input.");
            }
            if (line.hasOption("m")) {
                schema = new File(line.getOptionValue("m"));
            } else {
                throw new Exception("Please provide a mapping to rewrite in input.");
            }
            if (line.hasOption("o")) {
                outputFile = new File(line.getOptionValue("o"));
            } else {
                throw new Exception("Please provide a name for the output file.");
            }
            if (line.hasOption("n")) {
                maxN = Integer.parseInt(line.getOptionValue("n"));
            } else {
                throw new Exception("Please provide a maximum number of iterations in srepair" +
                        "before stopping to break joins in order to repair.");
            }
            if (line.hasOption("p")) {
                if (line.getOptionValue("p").equals("Max")) {
                    preference = new MaxFrontierPreference();
                } else if (line.getOptionValue("p").equals("Avg")) {
                    preference = new MaxAvgValuesPreference();
                } else if (line.getOptionValue("p").equals("Learn")) {
                    if (line.hasOption("t"))
                        preference = new KNNPreference(line.getOptionValue("t"));
                    else
                        throw new Exception("Please provide a training set.");
                } else {
                    throw new Exception("Wrong argument for the preference function to use.");
                }
            } else {
                throw new Exception("Please provide a choice for the preference function to use.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        Schema policySchema = null;
        Schema schemaToRewrite = null;
        try {
            policySchema = IOManager.importSchema(policyViews);
            schemaToRewrite = IOManager.importSchema(schema);
        } catch (JAXBException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        safeRew = new SafeRewriting(policySchema, preference);

        Schema parsedSchema = safeRew.repair(schemaToRewrite, maxN);

        try {
            IOManager.exportSchemaToXml(parsedSchema, outputFile);
        } catch (JAXBException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static CommandLine getCommandLine(String[] args, Options options) {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("MapRepair", options);
        return line;
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("v", true, "path to policy views file.");
        options.addOption("m", true, "path to mapping to rewrite file.");

        options.addOption("o", true, "output file.");
        options.addOption("p", true, "Preference function to use : Max, Avg or Learn.");
        options.addOption("n", true, "maximum number of iterations in srepair" +
                "before stopping to break joins in order to repair.");
        options.addOption("t", true, "(optional) training set file.");
        return options;
    }
}
