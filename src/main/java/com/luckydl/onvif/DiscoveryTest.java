package com.luckydl.onvif;

import com.luckydl.onvif.discovery.DeviceDiscovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;

public class DiscoveryTest {
    public static void main(String[] args) throws IOException {
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
}
