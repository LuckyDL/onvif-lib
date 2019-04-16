package com.luckydl.onvif.soap.devices;

import com.luckydl.onvif.soap.OnvifDevice;
import com.luckydl.onvif.soap.SOAP;
import org.onvif.ver10.device.wsdl.*;
import org.onvif.ver10.media.wsdl.*;
import org.onvif.ver10.schema.Capabilities;
import org.onvif.ver10.schema.*;

import javax.xml.soap.SOAPException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author https://github.com/milg0/onvif-java-lib
 * 代码优化： luckydl
 * @date 2019.01.04
 */
@SuppressWarnings("unused")
public class InitialDevices {

    private SOAP soap;
    private OnvifDevice onvifDevice;

    public InitialDevices(OnvifDevice onvifDevice) {
        this.onvifDevice = onvifDevice;
        this.soap = onvifDevice.getSoap();
    }

    public java.util.Date getDate() {
        Calendar cal;
        GetSystemDateAndTimeResponse response = new GetSystemDateAndTimeResponse();

        try {
            response = (GetSystemDateAndTimeResponse) soap.createSOAPDeviceRequest(new GetSystemDateAndTime(), response, false);
        } catch (SOAPException e) {
            e.printStackTrace();
            return null;
        }

        Date date = response.getSystemDateAndTime().getUTCDateTime().getDate();
        Time time = response.getSystemDateAndTime().getUTCDateTime().getTime();
        cal = new GregorianCalendar(date.getYear(), date.getMonth() - 1, date.getDay(), time.getHour(), time.getMinute(), time.getSecond());

        return cal.getTime();
    }

    public GetDeviceInformationResponse getDeviceInformation() {
        GetDeviceInformation getHostname = new GetDeviceInformation();
        GetDeviceInformationResponse response = new GetDeviceInformationResponse();
        try {
            response = (GetDeviceInformationResponse) soap.createSOAPDeviceRequest(getHostname, response, true);
        } catch (SOAPException e) {
            e.printStackTrace();
            return response;
        }
        return response;
    }

    public String getHostname() {
        GetHostname getHostname = new GetHostname();
        GetHostnameResponse response = new GetHostnameResponse();
        try {
            response = (GetHostnameResponse) soap.createSOAPDeviceRequest(getHostname, response, true);
        } catch (SOAPException e) {
            e.printStackTrace();
            return null;
        }
        return response.getHostnameInformation().getName();
    }

    public boolean setHostname(String hostname) {
        SetHostname setHostname = new SetHostname();
        setHostname.setName(hostname);
        SetHostnameResponse response = new SetHostnameResponse();
        try {
            soap.createSOAPDeviceRequest(setHostname, response, true);
        } catch (SOAPException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<User> getUsers() throws SOAPException {
        GetUsers getUsers = new GetUsers();
        GetUsersResponse response = new GetUsersResponse();
        response = (GetUsersResponse) soap.createSOAPDeviceRequest(getUsers, response, true);

        return response == null ? null : response.getUser();
    }

    public Capabilities getCapabilities() throws SOAPException {
        GetCapabilities getCapabilities = new GetCapabilities();
        GetCapabilitiesResponse response = new GetCapabilitiesResponse();

        response = (GetCapabilitiesResponse) soap.createSOAPRequest(getCapabilities, response, onvifDevice.getDeviceUri(), false);

        return response == null ? null : response.getCapabilities();
    }

    public List<Profile> getProfiles() throws SOAPException {
        GetProfiles request = new GetProfiles();
        GetProfilesResponse response = new GetProfilesResponse();
        response = (GetProfilesResponse) soap.createSOAPMediaRequest(request, response, true);

        return response == null ? null : response.getProfiles();
    }

    public Profile getProfile(String profileToken) {
        GetProfile request = new GetProfile();
        GetProfileResponse response = new GetProfileResponse();

        request.setProfileToken(profileToken);

        try {
            response = (GetProfileResponse) soap.createSOAPMediaRequest(request, response, true);
        } catch (SOAPException e) {
            e.printStackTrace();
            return null;
        }

        return response == null ? null : response.getProfile();
    }

    public Profile createProfile(String name) throws SOAPException {
        CreateProfile request = new CreateProfile();
        CreateProfileResponse response = new CreateProfileResponse();
        request.setName(name);
        response = (CreateProfileResponse) soap.createSOAPMediaRequest(request, response, true);

        return response == null ? null : response.getProfile();
    }

    public List<Service> getServices(boolean includeCapability) throws SOAPException{
        GetServices request = new GetServices();
        GetServicesResponse response = new GetServicesResponse();
        request.setIncludeCapability(includeCapability);
        response = (GetServicesResponse) soap.createSOAPDeviceRequest(request, response, true);

        return response == null ? null : response.getService();
    }

    public List<Scope> getScopes() throws SOAPException {
        GetScopes request = new GetScopes();
        GetScopesResponse response = new GetScopesResponse();
        response = (GetScopesResponse) soap.createSOAPMediaRequest(request, response, true);

        return response == null ? null : response.getScopes();
    }

    public String reboot() throws SOAPException {
        SystemReboot request = new SystemReboot();
        SystemRebootResponse response = new SystemRebootResponse();

        response = (SystemRebootResponse) soap.createSOAPMediaRequest(request, response, true);
        return response == null ? null : response.getMessage();
    }
}
