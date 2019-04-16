package com.luckydl.onvif;

import com.luckydl.onvif.discovery.DeviceDiscovery;
import com.luckydl.onvif.soap.OnvifDevice;
import lombok.extern.slf4j.Slf4j;
import org.onvif.ver10.schema.Profile;

import javax.xml.soap.SOAPException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * @author https://github.com/milg0/onvif-java-lib
 */
@Slf4j
public class Main {

    private static final String INFO = "Commands:\n  \n  url: Get snapshort URL.\n  info: Get information about each valid command.\n  profiles: Get all profiles.\n  exit: Exit this application.";

    public static void main(String[] args) throws IOException {
        System.out.println("What do you want to do?\n1: discovery\n2: Get Url");
        InputStreamReader inputStream = new InputStreamReader(System.in);
        BufferedReader keyboardInput = new BufferedReader(inputStream);
        String input = keyboardInput.readLine();
        switch (input) {
            case "1":
                discoveryDevice();
                break;
            case "2":
                getUrlByIP();
                break;
            default:
                break;
        }
    }

    private static void discoveryDevice() throws IOException {
        while (true) {
            System.out.println("Enter an IP address or 'q' to quit:");
            InputStreamReader inputStream = new InputStreamReader(System.in);
            BufferedReader keyboardInput = new BufferedReader(inputStream);
            String input = keyboardInput.readLine();
            if("q".equals(input)) {
                System.out.println("Bye Bye !!!");
                break;
            }
            Collection<URL> urls = DeviceDiscovery.discoverWsDevicesAsUrls(
                    "^http$", ".*onvif.*", true, input);
            if (urls != null) {
                urls.forEach(url -> System.out.println(url.toString()));
            }
        }
    }

    private static void getUrlByIP() {
        InputStreamReader inputStream = new InputStreamReader(System.in);
        BufferedReader keyboardInput = new BufferedReader(inputStream);
        String input, cameraAddress, user, password;

        try {
            System.out.println("Please enter camera IP (with port if not 80):");
            cameraAddress = keyboardInput.readLine();
            System.out.println("Please enter camera username:");
            user = keyboardInput.readLine();
            System.out.println("Please enter camera password:");
            password = keyboardInput.readLine();
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        String ip = cameraAddress.contains(":") ? cameraAddress.split(":")[0] : cameraAddress;
        Integer port = Integer.valueOf(cameraAddress.contains(":") ? cameraAddress.split(":")[1] : "80");
        System.out.println("Connect to camera, please wait ...");
        OnvifDevice cam;
        try {
            cam = new OnvifDevice(ip, port, user, password);
        } catch (ConnectException | SOAPException e1) {
            e1.printStackTrace();
            return;
        }
        System.out.println("Connection to camera successful!");

        while (true) {
            try {
                System.out.println();
                System.out.println("Enter a command (type \"info\" to get commands):");
                input = keyboardInput.readLine();

                switch (input) {
                    case "url": {
                        List<Profile> profiles = cam.getDevices().getProfiles();
                        for (Profile p : profiles) {
                            try {
                                System.out.println("URL from Profile \'" + p.getName() + "\': " + cam.getMedia().getSnapshotUri(p.getToken()));
                            } catch (SOAPException e) {
                                System.err.println("Cannot grap snapshot URL, got Exception " + e.getMessage());
                            }
                        }
                        break;
                    }
                    case "profiles":
                        List<Profile> profiles = cam.getDevices().getProfiles();
                        System.out.println("Number of profiles: " + profiles.size());
                        for (Profile p : profiles) {
                            System.out.println("  Profile " + p.getName() + " token is: " + p.getToken());
                            try {
                                String url = cam.getMedia().getTCPStreamUri(p.getToken());
                                System.out.println("TCP Url is: " + url);
                                System.out.println("UDP url: " + cam.getMedia().getRTSPStreamUri(p.getToken()));
                            } catch (SOAPException err) {
                                err.printStackTrace();
                            }
                        }
                        break;
                    case "info":
                        System.out.println(INFO);
                        break;
                    case "quit":
                    case "exit":
                    case "end":
                        return;
                    default:
                        System.out.println("Unknown command!");
                        System.out.println();
                        System.out.println(INFO);
                        break;
                }
            } catch (IOException | SOAPException e) {
                e.printStackTrace();
            }
        }
    }
}