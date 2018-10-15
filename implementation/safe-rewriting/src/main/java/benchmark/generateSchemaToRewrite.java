package benchmark;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;

public class generateSchemaToRewrite {

    public static void main(String[] args){

        File policyViews = new File(args[0]);

        Schema policySchema = null;
        try {
            policySchema = IOManager.importSchema(policyViews);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Schema newSchema = SchemaGenerator.extractNewSchema(policySchema,
                Integer.parseInt(args[1]),
                Integer.parseInt(args[2]),
                Integer.parseInt(args[3]),
                Integer.parseInt(args[4]),
                Integer.parseInt(args[5]),
                Integer.parseInt(args[6]),
                Double.parseDouble(args[7]),
                Double.parseDouble(args[8]));

        try {
            IOManager.exportSchemaToXml(newSchema, new File(args[9]));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}
