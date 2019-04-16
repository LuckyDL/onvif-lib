package com.luckydl.onvif.soap.devices;

/**
 * GetService namespace
 *
 * @author zdl
 * @date 2019.04.16
 */
public interface ServiceNameSpace {
    String DEVICE = "http://www.onvif.org/ver10/device/wsdl";
    String PTZ = "http://www.onvif.org/ver20/ptz/wsdl";
    String MEDIA = "http://www.onvif.org/ver10/media/wsdl";
    String IMAGING = "http://www.onvif.org/ver20/imaging/wsdl";
    String EVENTS = "http://www.onvif.org/ver10/events/wsdl";

    /**
     * unsupported currently
     */
    String DEVICE_IO = "http://www.onvif.org/ver10/deviceIO/wsdl";
    String ANALYTICS = "http://www.onvif.org/ver20/analytics/wsdl";
    String RECORDING = "http://www.onvif.org/ver10/recording/wsdl";
    String SEARCH = "http://www.onvif.org/ver10/search/wsdl";
    String REPLAY = "http://www.onvif.org/ver10/replay/wsdl";
}
