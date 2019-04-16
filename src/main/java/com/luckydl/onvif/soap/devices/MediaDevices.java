package com.luckydl.onvif.soap.devices;

import com.luckydl.onvif.soap.OnvifDevice;
import com.luckydl.onvif.soap.SOAP;
import org.onvif.ver10.media.wsdl.*;
import org.onvif.ver10.schema.*;

import javax.xml.soap.SOAPException;
import java.net.ConnectException;
import java.util.List;

/**
 * @author https://github.com/milg0/onvif-java-lib
 * 代码优化： luckydl
 * @date 2019.01.04
 */
@SuppressWarnings("unused")
public class MediaDevices {
    private OnvifDevice onvifDevice;
    private SOAP soap;

    public MediaDevices(OnvifDevice onvifDevice) {
        this.onvifDevice = onvifDevice;
        this.soap = onvifDevice.getSoap();
    }

    public static VideoEncoderConfiguration getVideoEncoderConfiguration(Profile profile) {
        return profile.getVideoEncoderConfiguration();
    }

    @Deprecated
    public String getHTTPStreamUri(int profileNumber) throws SOAPException {
        StreamSetup setup = new StreamSetup();
        setup.setStream(StreamType.RTP_UNICAST);
        Transport transport = new Transport();
        transport.setProtocol(TransportProtocol.HTTP);
        setup.setTransport(transport);
        return getStreamUri(setup, profileNumber);
    }

    public String getHTTPStreamUri(String profileToken) throws SOAPException {
        StreamSetup setup = new StreamSetup();
        setup.setStream(StreamType.RTP_UNICAST);
        Transport transport = new Transport();
        transport.setProtocol(TransportProtocol.HTTP);
        setup.setTransport(transport);
        return getStreamUri(profileToken, setup);
    }

    @Deprecated
    public String getUDPStreamUri(int profileNumber) throws SOAPException {
        StreamSetup setup = new StreamSetup();
        setup.setStream(StreamType.RTP_UNICAST);
        Transport transport = new Transport();
        transport.setProtocol(TransportProtocol.UDP);
        setup.setTransport(transport);
        return getStreamUri(setup, profileNumber);
    }

    public String getUDPStreamUri(String profileToken) throws SOAPException {
        StreamSetup setup = new StreamSetup();
        setup.setStream(StreamType.RTP_UNICAST);
        Transport transport = new Transport();
        transport.setProtocol(TransportProtocol.UDP);
        setup.setTransport(transport);
        return getStreamUri(profileToken, setup);
    }

    @Deprecated
    public String getTCPStreamUri(int profileNumber) throws SOAPException {
        StreamSetup setup = new StreamSetup();
        setup.setStream(StreamType.RTP_UNICAST);
        Transport transport = new Transport();
        transport.setProtocol(TransportProtocol.TCP);
        setup.setTransport(transport);
        return getStreamUri(setup, profileNumber);
    }

    public String getTCPStreamUri(String profileToken) throws SOAPException {
        StreamSetup setup = new StreamSetup();
        setup.setStream(StreamType.RTP_UNICAST);
        Transport transport = new Transport();
        transport.setProtocol(TransportProtocol.TCP);
        setup.setTransport(transport);
        return getStreamUri(profileToken, setup);
    }

    @Deprecated
    public String getRTSPStreamUri(int profileNumber) throws SOAPException {
        StreamSetup setup = new StreamSetup();
        setup.setStream(StreamType.RTP_UNICAST);
        Transport transport = new Transport();
        transport.setProtocol(TransportProtocol.TCP);
        setup.setTransport(transport);
        return getStreamUri(setup, profileNumber);
    }

    public String getRTSPStreamUri(String profileToken) throws SOAPException {
        StreamSetup setup = new StreamSetup();
        setup.setStream(StreamType.RTP_UNICAST);
        Transport transport = new Transport();
        transport.setProtocol(TransportProtocol.TCP);
        setup.setTransport(transport);
        return getStreamUri(profileToken, setup);
    }

    @Deprecated
    public String getStreamUri(StreamSetup streamSetup, int profileNumber) throws SOAPException {
        Profile profile = onvifDevice.getDevices().getProfiles().get(profileNumber);
        return getStreamUri(profile, streamSetup);
    }

    @Deprecated
    public String getStreamUri(Profile profile, StreamSetup streamSetup) throws SOAPException {
        return getStreamUri(profile.getToken(), streamSetup);
    }

    public String getStreamUri(String profileToken, StreamSetup streamSetup) throws SOAPException {
        GetStreamUri request = new GetStreamUri();
        GetStreamUriResponse response = new GetStreamUriResponse();

        request.setProfileToken(profileToken);
        request.setStreamSetup(streamSetup);
        response = (GetStreamUriResponse) soap.createSOAPMediaRequest(request, response, false);

        return response == null ? null : onvifDevice.replaceLocalIpWithProxyIp(response.getMediaUri().getUri());
    }

    public VideoEncoderConfigurationOptions getVideoEncoderConfigurationOptions(String profileToken) throws SOAPException {
        GetVideoEncoderConfigurationOptions request = new GetVideoEncoderConfigurationOptions();
        GetVideoEncoderConfigurationOptionsResponse response = new GetVideoEncoderConfigurationOptionsResponse();

        request.setProfileToken(profileToken);
        response = (GetVideoEncoderConfigurationOptionsResponse) soap.createSOAPMediaRequest(request, response, false);

        return response == null ? null : response.getOptions();
    }

    public boolean setVideoEncoderConfiguration(VideoEncoderConfiguration videoEncoderConfiguration) throws SOAPException {
        SetVideoEncoderConfiguration request = new SetVideoEncoderConfiguration();
        SetVideoEncoderConfigurationResponse response = new SetVideoEncoderConfigurationResponse();

        request.setConfiguration(videoEncoderConfiguration);
        request.setForcePersistence(true);
        response = (SetVideoEncoderConfigurationResponse) soap.createSOAPMediaRequest(request, response, true);

        return response != null;
    }

    public String getSceenshotUri(String profileToken) throws SOAPException, ConnectException {
        return getSnapshotUri(profileToken);
    }

    public String getSnapshotUri(String profileToken) throws SOAPException {
        GetSnapshotUri request = new GetSnapshotUri();
        GetSnapshotUriResponse response = new GetSnapshotUriResponse();

        request.setProfileToken(profileToken);
        response = (GetSnapshotUriResponse) soap.createSOAPMediaRequest(request, response, true);

        return response == null ? null : onvifDevice.replaceLocalIpWithProxyIp(response.getMediaUri().getUri());
    }

    public List<VideoSource> getVideoSources() throws SOAPException {
        GetVideoSources request = new GetVideoSources();
        GetVideoSourcesResponse response = new GetVideoSourcesResponse();
        response = (GetVideoSourcesResponse) soap.createSOAPMediaRequest(request, response, false);

        return response == null ? null : response.getVideoSources();
    }
}
