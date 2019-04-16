package com.luckydl.onvif.soap.devices;

import com.luckydl.onvif.soap.OnvifDevice;
import com.luckydl.onvif.soap.SOAP;
import org.onvif.ver10.schema.AbsoluteFocus;
import org.onvif.ver10.schema.FocusMove;
import org.onvif.ver10.schema.ImagingOptions20;
import org.onvif.ver10.schema.ImagingSettings20;
import org.onvif.ver20.imaging.wsdl.*;

import javax.xml.soap.SOAPException;

/**
 * @author https://github.com/milg0/onvif-java-lib
 * 代码优化： luckydl
 * @date 2019.01.04
 */
@SuppressWarnings("unused")
public class ImagingDevices {

    private OnvifDevice onvifDevice;
    private SOAP soap;

    public ImagingDevices(OnvifDevice onvifDevice) {
        this.onvifDevice = onvifDevice;
        this.soap = onvifDevice.getSoap();
    }

    public ImagingOptions20 getOptions(String videoSourceToken) {
        if (videoSourceToken == null) {
            return null;
        }

        GetOptions request = new GetOptions();
        GetOptionsResponse response = new GetOptionsResponse();

        request.setVideoSourceToken(videoSourceToken);

        try {
            response = (GetOptionsResponse) soap.createSOAPImagingRequest(request, response, false);
        } catch (SOAPException e) {
            e.printStackTrace();
            return null;
        }
        return response == null ? null : response.getImagingOptions();
    }

    public boolean moveFocus(String videoSourceToken, float absoluteFocusValue) {
        if (videoSourceToken == null) {
            return false;
        }

        Move request = new Move();
        MoveResponse response = new MoveResponse();

        AbsoluteFocus absoluteFocus = new AbsoluteFocus();
        absoluteFocus.setPosition(absoluteFocusValue);

        FocusMove focusMove = new FocusMove();
        focusMove.setAbsolute(absoluteFocus);

        request.setVideoSourceToken(videoSourceToken);
        request.setFocus(focusMove);

        try {
            response = (MoveResponse) soap.createSOAPImagingRequest(request, response, true);
        } catch (SOAPException e) {
            e.printStackTrace();
            return false;
        }

        return response != null;
    }

    public ImagingSettings20 getImagingSettings(String videoSourceToken) {
        if (videoSourceToken == null) {
            return null;
        }

        GetImagingSettings request = new GetImagingSettings();
        GetImagingSettingsResponse response = new GetImagingSettingsResponse();

        request.setVideoSourceToken(videoSourceToken);

        try {
            response = (GetImagingSettingsResponse) soap.createSOAPImagingRequest(request, response, true);
        } catch (SOAPException e) {
            e.printStackTrace();
            return null;
        }

        return response == null ? null : response.getImagingSettings();
    }

    public boolean setImagingSettings(String videoSourceToken, ImagingSettings20 imagingSettings) {
        if (videoSourceToken == null) {
            return false;
        }

        SetImagingSettings request = new SetImagingSettings();
        SetImagingSettingsResponse response = new SetImagingSettingsResponse();

        request.setVideoSourceToken(videoSourceToken);
        request.setImagingSettings(imagingSettings);

        try {
            response = (SetImagingSettingsResponse) soap.createSOAPImagingRequest(request, response, true);
        } catch (SOAPException e) {
            e.printStackTrace();
            return false;
        }

        return response != null;
    }
}
