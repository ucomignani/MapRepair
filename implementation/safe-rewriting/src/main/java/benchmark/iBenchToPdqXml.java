package benchmark;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

import javax.xml.bind.JAXBException;
import java.io.File;

public class iBenchToPdqXml {

    public static void main(String[] args){

        File sourceSch = new File(args[0]);
        File targetSch = new File(args[1]);
        File mapping = new File(args[2]);

        Schema parsedSchema = iBenchParser.importSchema(sourceSch, targetSch, mapping);

        try {
            IOManager.exportSchemaToXml(parsedSchema, new File(args[3]));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}
