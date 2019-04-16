package com.luckydl.onvif.discovery;

public interface DiscoveryConfig {

    String PROB_MESSAGE = "<soap:Envelope\n" +
            "xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"" +
            "xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\"" +
            "xmlns:tns=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\">" +
            "<soap:Header>" +
            "<wsa:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>" +
            "<wsa:MessageID>uuid:c032cfdd-c3ca-49dc-820e-ee6696ad63e2</wsa:MessageID>" +
            "<wsa:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>" +
            "</soap:Header>" +
            "<soap:Body>" +
            "<tns:Scopes />" +
            "<tns:Probe />" +
            "</soap:Body>" +
            "</soap:Envelope>";

    String WS_DISCOVERY_SOAP_VERSION = "SOAP 1.2 Protocol";
    String WS_DISCOVERY_CONTENT_TYPE = "application/soap+xml";
    int WS_DISCOVERY_TIMEOUT = 4000;
    int WS_DISCOVERY_PORT = 3702;
}
