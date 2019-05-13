
package client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetResultVisChase_QNAME = new QName("http://server/", "getResultVisChase");
    private final static QName _GetResultRepairResponse_QNAME = new QName("http://server/", "getResultRepairResponse");
    private final static QName _GetResultVisChaseResponse_QNAME = new QName("http://server/", "getResultVisChaseResponse");
    private final static QName _GetResultRepair_QNAME = new QName("http://server/", "getResultRepair");
    private final static QName _GetResultVisChaseResponseReturn_QNAME = new QName("", "return");
    private final static QName _GetResultRepairArg1_QNAME = new QName("", "arg1");
    private final static QName _GetResultRepairArg0_QNAME = new QName("", "arg0");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetResultRepair }
     * 
     */
    public GetResultRepair createGetResultRepair() {
        return new GetResultRepair();
    }

    /**
     * Create an instance of {@link GetResultVisChaseResponse }
     * 
     */
    public GetResultVisChaseResponse createGetResultVisChaseResponse() {
        return new GetResultVisChaseResponse();
    }

    /**
     * Create an instance of {@link GetResultRepairResponse }
     * 
     */
    public GetResultRepairResponse createGetResultRepairResponse() {
        return new GetResultRepairResponse();
    }

    /**
     * Create an instance of {@link GetResultVisChase }
     * 
     */
    public GetResultVisChase createGetResultVisChase() {
        return new GetResultVisChase();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetResultVisChase }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server/", name = "getResultVisChase")
    public JAXBElement<GetResultVisChase> createGetResultVisChase(GetResultVisChase value) {
        return new JAXBElement<GetResultVisChase>(_GetResultVisChase_QNAME, GetResultVisChase.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetResultRepairResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server/", name = "getResultRepairResponse")
    public JAXBElement<GetResultRepairResponse> createGetResultRepairResponse(GetResultRepairResponse value) {
        return new JAXBElement<GetResultRepairResponse>(_GetResultRepairResponse_QNAME, GetResultRepairResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetResultVisChaseResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server/", name = "getResultVisChaseResponse")
    public JAXBElement<GetResultVisChaseResponse> createGetResultVisChaseResponse(GetResultVisChaseResponse value) {
        return new JAXBElement<GetResultVisChaseResponse>(_GetResultVisChaseResponse_QNAME, GetResultVisChaseResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetResultRepair }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server/", name = "getResultRepair")
    public JAXBElement<GetResultRepair> createGetResultRepair(GetResultRepair value) {
        return new JAXBElement<GetResultRepair>(_GetResultRepair_QNAME, GetResultRepair.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "return", scope = GetResultVisChaseResponse.class)
    public JAXBElement<byte[]> createGetResultVisChaseResponseReturn(byte[] value) {
        return new JAXBElement<byte[]>(_GetResultVisChaseResponseReturn_QNAME, byte[].class, GetResultVisChaseResponse.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "return", scope = GetResultRepairResponse.class)
    public JAXBElement<byte[]> createGetResultRepairResponseReturn(byte[] value) {
        return new JAXBElement<byte[]>(_GetResultVisChaseResponseReturn_QNAME, byte[].class, GetResultRepairResponse.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "arg1", scope = GetResultRepair.class)
    public JAXBElement<byte[]> createGetResultRepairArg1(byte[] value) {
        return new JAXBElement<byte[]>(_GetResultRepairArg1_QNAME, byte[].class, GetResultRepair.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "arg0", scope = GetResultRepair.class)
    public JAXBElement<byte[]> createGetResultRepairArg0(byte[] value) {
        return new JAXBElement<byte[]>(_GetResultRepairArg0_QNAME, byte[].class, GetResultRepair.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "arg0", scope = GetResultVisChase.class)
    public JAXBElement<byte[]> createGetResultVisChaseArg0(byte[] value) {
        return new JAXBElement<byte[]>(_GetResultRepairArg0_QNAME, byte[].class, GetResultVisChase.class, ((byte[]) value));
    }

}
