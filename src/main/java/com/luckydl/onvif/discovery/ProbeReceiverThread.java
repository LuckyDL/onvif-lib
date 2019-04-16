package com.luckydl.onvif.discovery;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

public class ProbeReceiverThread extends Thread {
    private final Collection<String> addresses;
    private final CountDownLatch serverStarted;
    private final DatagramSocket socket;
    private final CountDownLatch serverFinished;

    public ProbeReceiverThread(Collection<String> addresses, CountDownLatch serverStarted, DatagramSocket socket,
                               CountDownLatch serverFinished) {
        this.addresses = addresses;
        this.serverStarted = serverStarted;
        this.socket = socket;
        this.serverFinished = serverFinished;
    }

    private static Collection<Node> getNodeMatching(Node body, String regexp) {
        final Collection<Node> nodes = new ArrayList<>();
        if (body.getNodeName().matches(regexp)) {
            nodes.add(body);
        }
        if (body.getChildNodes().getLength() == 0) {
            return nodes;
        }
        NodeList returnList = body.getChildNodes();
        for (int k = 0; k < returnList.getLength(); k++) {
            final Node node = returnList.item(k);
            nodes.addAll(getNodeMatching(node, regexp));
        }
        return nodes;
    }

    private static Collection<String> parseSoapResponseForUrls(byte[] data) throws SOAPException, IOException {
        final Collection<String> urls = new ArrayList<>();
        MessageFactory factory = MessageFactory.newInstance(DiscoveryConfig.WS_DISCOVERY_SOAP_VERSION);
        final MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-type", DiscoveryConfig.WS_DISCOVERY_CONTENT_TYPE);
        SOAPMessage message = factory.createMessage(headers, new ByteArrayInputStream(data));
        SOAPBody body = message.getSOAPBody();
        for (Node node : getNodeMatching(body, ".*:XAddrs")) {
            if (node.getTextContent().length() > 0) {
                urls.addAll(Arrays.asList(node.getTextContent().split(" ")));
            }
        }
        return urls;
    }

    @Override
    public void run() {
        try {
            final DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
            socket.setSoTimeout(DiscoveryConfig.WS_DISCOVERY_TIMEOUT);
            long timerStarted = System.currentTimeMillis();
            while (System.currentTimeMillis() - timerStarted < DiscoveryConfig.WS_DISCOVERY_TIMEOUT) {
                serverStarted.countDown();
                socket.receive(packet);
                final Collection<String> collection = parseSoapResponseForUrls(Arrays.copyOf(packet.getData(), packet.getLength()));
                addresses.addAll(collection);
            }
        } catch (SocketTimeoutException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverFinished.countDown();
            socket.close();
        }
    }
}
