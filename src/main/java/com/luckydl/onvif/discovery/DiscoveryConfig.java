package com.luckydl.onvif.discovery;

public interface DiscoveryConfig {

    String PROB_MESSAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<Envelope xmlns:dn=\"http://www.onvif.org/ver10/network/wsdl\" xmlns=\"http://www.w3.org/2003/05/soap-envelope\">" +
            "<Header><wsa:MessageID xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">uuid:df632d26-fe12-4255-9258-942192890191</wsa:MessageID>" +
            "<wsa:To xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>" +
            "<wsa:Action xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>" +
            "</Header>" +
            "<Body>" +
            "<Probe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\">" +
            "<Types>dn:NetworkVideoTransmitter</Types>" +
            "<Scopes /></Probe>" +
            "</Body>" +
            "</Envelope>";


    String WS_DISCOVERY_SOAP_VERSION = "SOAP 1.2 Protocol";
    String WS_DISCOVERY_CONTENT_TYPE = "application/soap+xml";
    int WS_DISCOVERY_TIMEOUT = 4000;
    int WS_DISCOVERY_PORT = 3702;
}
