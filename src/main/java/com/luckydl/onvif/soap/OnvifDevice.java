package com.luckydl.onvif.soap;

import com.luckydl.onvif.soap.devices.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.onvif.ver10.device.wsdl.Service;
import org.onvif.ver10.schema.Capabilities;

import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author https://github.com/milg0/onvif-java-lib
 * 代码优化：luckydl
 * @date 2019.01.03
 */
@SuppressWarnings("unused")
@Slf4j
public class OnvifDevice {
    private final String HOST_IP;
    private String originalIp;

    private boolean isProxy;

    private String username, password, nonce, utcTime;

    private String serverDeviceUri, serverPtzUri, serverMediaUri, serverImagingUri, serverEventsUri;

    private SOAP soap;

    private InitialDevices initialDevices;
    private PtzDevices ptzDevices;
    private MediaDevices mediaDevices;
    private ImagingDevices imagingDevices;


    /**
     * Initializes an Onvif device, e.g. a Network Video Transmitter (NVT) with
     * login data.
     *
     * @param hostIp   The IP address of your device, you can also add a port but
     *                 noch protocol (e.g. http://)
     * @param user     Username you need to login
     * @param password User's password to login
     * @throws ConnectException Exception gets thrown, if device isn't accessible or invalid
     *                          and doesn't answer to SOAP messages
     * @throws SOAPException    soap 解析异常
     */
    public OnvifDevice(String hostIp, Integer port, String user, String password) throws ConnectException, SOAPException {

        this.HOST_IP = hostIp;

        if (!isOnline(hostIp, port)) {
            throw new ConnectException("Host not available: " + hostIp + ":" + port);
        }
        if (port == null || port.equals(0)) {
            this.serverDeviceUri = "http://" + HOST_IP + "/onvif/device_service";
        } else {
            this.serverDeviceUri = "http://" + HOST_IP + ":" + port.toString() + "/onvif/device_service";
        }

        this.username = user;
        this.password = password;

        this.soap = new SOAP(this);
        this.initialDevices = new InitialDevices(this);
        this.ptzDevices = new PtzDevices(this);
        this.mediaDevices = new MediaDevices(this);
        this.imagingDevices = new ImagingDevices(this);

        init();
    }

    /**
     * Initializes an Onvif device, e.g. a Network Video Transmitter (NVT) with
     * login data.
     *
     * @param hostIp The IP address of your device, you can also add a port but
     *               noch protocol (e.g. http://)
     * @throws ConnectException Exception gets thrown, if device isn't accessible or invalid
     *                          and doesn't answer to SOAP messages
     * @throws SOAPException    soap 解析异常
     */
    public OnvifDevice(String hostIp) throws ConnectException, SOAPException {
        this(hostIp, null, null, null);
    }

    private static byte[] sha1(String s) throws NoSuchAlgorithmException {
        MessageDigest SHA1 = null;
        SHA1 = MessageDigest.getInstance("SHA1");

        SHA1.reset();
        SHA1.update(s.getBytes());
        return SHA1.digest();
    }

    /**
     * Internal function to check, if device is available and answers to ping
     * requests.
     */
    private boolean isOnline(String ip, Integer port) {
        Integer devicePort = port == null ? 80 : port;
        SocketAddress sockAddr = new InetSocketAddress(ip, devicePort);
        try (Socket socket = new Socket()) {
            socket.connect(sockAddr, 5000);
        } catch (NumberFormatException | IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Initialize the addresses used for SOAP messages and to get the internal
     * IP, if given IP is a proxy.
     *
     * @throws ConnectException Get thrown if device doesn't give answers to
     *                          GetCapabilities()
     * @throws SOAPException    soap 解析异常
     */
    private void init() throws ConnectException, SOAPException {

        List<Service> serviceList = getDevices().getServices(false);
        serviceList.forEach(this::setServiceUri);

        String localDeviceUri = this.serverDeviceUri;

        if (localDeviceUri.startsWith("http://")) {
            originalIp = localDeviceUri.replace("http://", "");
            originalIp = originalIp.substring(0, originalIp.indexOf('/'));
            if(originalIp.contains(":")) {
                originalIp = originalIp.split((":"))[0];
            }
        } else {
            log.error("Unknown/Not implemented local protocol!");
        }

        if (!originalIp.equals(HOST_IP)) {
            isProxy = true;
        }
    }

    private void setServiceUri(Service service) {
        switch (service.getNamespace()) {
            case ServiceNameSpace.DEVICE:
                this.serverDeviceUri = service.getXAddr();
                break;
            case ServiceNameSpace.MEDIA:
                this.serverMediaUri = service.getXAddr();
                break;
            case ServiceNameSpace.IMAGING:
                this.serverImagingUri =service.getXAddr();
                break;
            case ServiceNameSpace.PTZ:
                this.serverPtzUri = service.getXAddr();
                break;
            case ServiceNameSpace.EVENTS:
                this.serverEventsUri = service.getXAddr();
                break;
            default:
                log.error("Unsupported service, namespace: {}, xAddr: {}", service.getNamespace(), service.getXAddr());
        }
    }


    public String replaceLocalIpWithProxyIp(String original) {
        if (original.startsWith("http:///")) {
            original = original.replace("http:///", "http://" + HOST_IP);
        }

        if (isProxy) {
            return original.replace(originalIp, HOST_IP);
        }
        return original;
    }

    public String getUsername() {
        return username;
    }

    public String getEncryptedPassword() {
        return encryptPassword();
    }

    /**
     * Returns encrypted version of given password like algorithm like in WS-UsernameToken
     */
    public String encryptPassword() {
        String nonce = getNonce();
        String timestamp = getUTCTime();

        String beforeEncryption = nonce + timestamp + password;

        byte[] encryptedRaw;
        try {
            encryptedRaw = sha1(beforeEncryption);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return Base64.encodeBase64String(encryptedRaw);
    }

    private String getNonce() {
        if (nonce == null) {
            createNonce();
        }
        return nonce;
    }

    public String getEncryptedNonce() {
        if (nonce == null) {
            createNonce();
        }
        return Base64.encodeBase64String(nonce.getBytes());
    }

    public void createNonce() {
        Random generator = new Random();
        nonce = "" + generator.nextInt();
    }

    public String getLastUTCTime() {
        return utcTime;
    }

    public String getUTCTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss'Z'");
        sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        String utcTime = sdf.format(cal.getTime());
        this.utcTime = utcTime;
        return utcTime;
    }

    public SOAP getSoap() {
        return soap;
    }

    /**
     * Is used for basic devices and requests of given Onvif Device
     */
    public InitialDevices getDevices() {
        return initialDevices;
    }

    /**
     * Can be used for PTZ controlling requests, may not be supported by device!
     */
    public PtzDevices getPtz() {
        return ptzDevices;
    }

    /**
     * Can be used to get media data from OnvifDevice
     */
    public MediaDevices getMedia() {
        return mediaDevices;
    }

    /**
     * Can be used to get media data from OnvifDevice
     */
    public ImagingDevices getImaging() {
        return imagingDevices;
    }

    public String getDeviceUri() {
        return serverDeviceUri;
    }

    protected String getPtzUri() {
        return serverPtzUri;
    }

    protected String getMediaUri() {
        return serverMediaUri;
    }

    protected String getImagingUri() {
        return serverImagingUri;
    }

    protected String getEventsUri() {
        return serverEventsUri;
    }

    public Date getDate() {
        return initialDevices.getDate();
    }

    public String getName() {
        return initialDevices.getDeviceInformation().getModel();
    }

    public String getHostname() {
        return initialDevices.getHostname();
    }

    public String reboot() throws SOAPException {
        return initialDevices.reboot();
    }
}
