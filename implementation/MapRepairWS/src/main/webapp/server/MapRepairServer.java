package server;


import javax.xml.ws.Endpoint;

/**
 * MapRepair web service server.
 */
public class MapRepairServer {

        public static void main(String[] args) {
            String bindingURI = "http://localhost:8080/MapRepair";
            MapRepairEndpInterface service = new MapRepairWS();
            Endpoint.publish(bindingURI, service);
            System.out.println("Server started at: " + bindingURI);
        }
}
