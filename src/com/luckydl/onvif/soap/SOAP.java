package com.luckydl.onvif.soap;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;

/**
 * @author https://github.com/milg0/onvif-java-lib
 * 代码优化： luckydl
 * @date 2019.01.04
 */
@Slf4j
@SuppressWarnings("unused")
public class SOAP {

    private OnvifDevice onvifDevice;

    public SOAP(OnvifDevice onvifDevice) {
        super();

        this.onvifDevice = onvifDevice;
    }

    public Object createSOAPDeviceRequest(Object soapRequestElem, Object soapResponseElem, boolean needsAuthentication) throws SOAPException {
        return createSOAPRequest(soapRequestElem, soapResponseElem, onvifDevice.getDeviceUri(), needsAuthentication);
    }

    public Object createSOAPPtzRequest(Object soapRequestElem, Object soapResponseElem, boolean needsAuthentication) throws SOAPException {
        return createSOAPRequest(soapRequestElem, soapResponseElem, onvifDevice.getPtzUri(), needsAuthentication);
    }

    public Object createSOAPMediaRequest(Object soapRequestElem, Object soapResponseElem, boolean needsAuthentication) throws SOAPException {
        return createSOAPRequest(soapRequestElem, soapResponseElem, onvifDevice.getMediaUri(), needsAuthentication);
    }

    public Object createSOAPImagingRequest(Object soapRequestElem, Object soapResponseElem, boolean needsAuthentication) throws SOAPException {
        return createSOAPRequest(soapRequestElem, soapResponseElem, onvifDevice.getImagingUri(), needsAuthentication);
    }

    public Object createSOAPEventsRequest(Object soapRequestElem, Object soapResponseElem, boolean needsAuthentication) throws SOAPException {
        return createSOAPRequest(soapRequestElem, soapResponseElem, onvifDevice.getEventsUri(), needsAuthentication);
    }

    /**
     * @param soapResponseElem Answer object for SOAP request
     * @return SOAP Response Element
     * @throws SOAPException soap解析异常
     */
    public Object createSOAPRequest(Object soapRequestElem, Object soapResponseElem, String soapUri, boolean needsAuthentication) throws SOAPException {
        SOAPConnection soapConnection = null;
        SOAPMessage soapResponse = null;
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            soapConnection = soapConnectionFactory.createConnection();

            SOAPMessage soapMessage = createSoapMessage(soapRequestElem, needsAuthentication);
            log.debug("createSOAPRequest: Request SOAP message: {}", soapMessage.getSOAPBody().toString());

            soapResponse = soapConnection.call(soapMessage, soapUri);
            if (soapResponseElem == null) {
                throw new NullPointerException("Improper SOAP Response Element given (is null).");
            }
            log.debug("Response SOAP Message ({}): {}", soapResponseElem.getClass().getSimpleName(), soapResponse.getSOAPBody().toString());

            Unmarshaller unmarshaller = JAXBContext.newInstance(soapResponseElem.getClass()).createUnmarshaller();
            try {
                soapResponseElem = unmarshaller.unmarshal(soapResponse.getSOAPBody().extractContentAsDocument());
            } catch (UnmarshalException e) {
                log.warn("Could not unmarshal, ended in SOAP fault.");
                return null;
            }

            return soapResponseElem;
        } catch (SOAPException e) {
            log.error(
                    "Unexpected response. Response should be from class " + soapResponseElem.getClass() + ", but response is: " + soapResponse);
            throw e;
        } catch (ParserConfigurationException | JAXBException e) {
            log.error("Unhandled exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (soapConnection != null) {
                soapConnection.close();
            }
        }
    }

    private SOAPMessage createSoapMessage(Object soapRequestElem, boolean needAuthentication) throws SOAPException, ParserConfigurationException,
            JAXBException {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = messageFactory.createMessage();

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Marshaller marshaller = JAXBContext.newInstance(soapRequestElem.getClass()).createMarshaller();
        marshaller.marshal(soapRequestElem, document);
        soapMessage.getSOAPBody().addDocument(document);

        createSoapHeader(soapMessage);

        soapMessage.saveChanges();
        return soapMessage;
    }

    private void createSoapHeader(SOAPMessage soapMessage) throws SOAPException {
        onvifDevice.createNonce();
        String encrypedPassword = onvifDevice.getEncryptedPassword();
        if (encrypedPassword != null && onvifDevice.getUsername() != null) {

            SOAPPart sp = soapMessage.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            SOAPHeader header = soapMessage.getSOAPHeader();
            se.addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
            se.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

            SOAPElement securityElem = header.addChildElement("Security", "wsse");

            SOAPElement usernameTokenElem = securityElem.addChildElement("UsernameToken", "wsse");

            SOAPElement usernameElem = usernameTokenElem.addChildElement("Username", "wsse");
            usernameElem.setTextContent(onvifDevice.getUsername());

            SOAPElement passwordElem = usernameTokenElem.addChildElement("Password", "wsse");
            passwordElem.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");
            passwordElem.setTextContent(encrypedPassword);

            SOAPElement nonceElem = usernameTokenElem.addChildElement("Nonce", "wsse");
            nonceElem.setAttribute("EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-com.gosun.isap.onvif.soap-message-security-1.0#Base64Binary");
            nonceElem.setTextContent(onvifDevice.getEncryptedNonce());

            SOAPElement createdElem = usernameTokenElem.addChildElement("Created", "wsu");
            createdElem.setTextContent(onvifDevice.getLastUTCTime());
        }
    }
}
