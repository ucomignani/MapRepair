package server;

import javax.jws.WebMethod;
import javax.jws.WebService;

//Service Endpoint Interface
@WebService
public interface MapRepairEndpInterface {

    @WebMethod byte[] getResultVisChase(byte[] inputMappingBytes);

    @WebMethod byte[] getResultRepair(byte[] policyViewsBytes, byte[] mappingToRewriteBytes);

}
