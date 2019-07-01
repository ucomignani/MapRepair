
package client;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "MapRepairWSService", targetNamespace = "http://server/", wsdlLocation = "http://localhost:8080/MapRepair?wsdl")
public class MapRepairWSService
    extends Service
{

    private final static URL MAPREPAIRWSSERVICE_WSDL_LOCATION;
    private final static WebServiceException MAPREPAIRWSSERVICE_EXCEPTION;
    private final static QName MAPREPAIRWSSERVICE_QNAME = new QName("http://server/", "MapRepairWSService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:8080/MapRepair?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        MAPREPAIRWSSERVICE_WSDL_LOCATION = url;
        MAPREPAIRWSSERVICE_EXCEPTION = e;
    }

    public MapRepairWSService() {
        super(__getWsdlLocation(), MAPREPAIRWSSERVICE_QNAME);
    }

    public MapRepairWSService(WebServiceFeature... features) {
        super(__getWsdlLocation(), MAPREPAIRWSSERVICE_QNAME, features);
    }

    public MapRepairWSService(URL wsdlLocation) {
        super(wsdlLocation, MAPREPAIRWSSERVICE_QNAME);
    }

    public MapRepairWSService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, MAPREPAIRWSSERVICE_QNAME, features);
    }

    public MapRepairWSService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public MapRepairWSService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns MapRepairWS
     */
    @WebEndpoint(name = "MapRepairWSPort")
    public MapRepairWS getMapRepairWSPort() {
        return super.getPort(new QName("http://server/", "MapRepairWSPort"), MapRepairWS.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns MapRepairWS
     */
    @WebEndpoint(name = "MapRepairWSPort")
    public MapRepairWS getMapRepairWSPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://server/", "MapRepairWSPort"), MapRepairWS.class, features);
    }

    private static URL __getWsdlLocation() {
        if (MAPREPAIRWSSERVICE_EXCEPTION!= null) {
            throw MAPREPAIRWSSERVICE_EXCEPTION;
        }
        return MAPREPAIRWSSERVICE_WSDL_LOCATION;
    }

}