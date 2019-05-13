package server;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;
import java.io.*;
import java.util.Base64;

import preference.MaxFrontierPreference;
import util.SafeRewriting;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

@WebService
public class MapRepairWS implements MapRepairEndpInterface {

    @WebMethod
    public byte[] getResultVisChase(byte[] inputMappingBytes) {

        try {
            ByteArrayOutputStream policyViews = new ByteArrayOutputStream();
            policyViews.write(inputMappingBytes);
            policyViews.close();

            Schema policySchema = null;
            try {
                policySchema = IOManager.importSchema(policyViews.toString());
            } catch (JAXBException e) {
                e.printStackTrace();
                System.exit(1);
            }

            SafeRewriting safeRew = new SafeRewriting(policySchema, new MaxFrontierPreference());


            String output = safeRew.getInstanceRef().toString();
            System.out.println("Sent document : " + new String(output.getBytes()));

            return output.getBytes();
        } catch (IOException e) {
            System.err.println(e);
            throw new WebServiceException(e);
        }
    }

    @WebMethod
    public byte[] getResultRepair(byte[] policyViewsBytes, byte[] mappingToRewriteBytes) {

        try {
            ByteArrayOutputStream policyViews = new ByteArrayOutputStream();
            policyViews.write(policyViewsBytes);
            policyViews.close();

            ByteArrayOutputStream mappingToRewrite = new ByteArrayOutputStream();
            mappingToRewrite.write(mappingToRewriteBytes);
            mappingToRewrite.close();

            Schema policySchema = null;
            Schema mappingSchema = null;
            try {
                policySchema = IOManager.importSchema(policyViews.toString());
                mappingSchema = IOManager.importSchema(mappingToRewrite.toString());
            } catch (JAXBException e) {
                e.printStackTrace();
                System.exit(1);
            }

            SafeRewriting safeRew = new SafeRewriting(policySchema, new MaxFrontierPreference());


            String output = safeRew.repair(mappingSchema, 10).toString();
            System.out.println("Sent document : " + new String(output.getBytes()));

            return output.getBytes();
        } catch (IOException e) {
            System.err.println(e);
            throw new WebServiceException(e);
        }
    }


}
