package com.luckydl.onvif.discovery;

import java.net.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

/**
 * Device discovery class to list local accessible devices probed per UDP probe
 * messages.
 *
 * @author th
 * @version 0.1
 * @date 2015-06-18
 */
public class DeviceDiscovery {

    private static final Random RANDOM = new SecureRandom();

    /**
     * Discover WS device on the local network and returns Urls
     *
     * @return list of unique device urls
     */
    public static Collection<URL> discoverWsDevicesAsUrls(boolean useIpv4) {
        return discoverWsDevicesAsUrls("", "", useIpv4);
    }

    public static String getDeviceUrlForIpv4(String ip) {
        Collection<URL> urls = DeviceDiscovery.discoverWsDevicesAsUrls(
                "^http$", ".*onvif.*", true, ip);
        String serverUrl;
        String pattern = "(2(5[0-5]{1}|[0-4]\\d{1})|[0-1]?\\d{1,2})(\\.(2(5[0-5]{1}|[0-4]\\d{1})|[0-1]?\\d{1,2})){3}";
        if (urls != null && urls.size() > 0) {
            for(URL url : urls) {
                if (url.getHost().matches(pattern)) {
                    serverUrl = url.toString();
                    return serverUrl;
                }
            }
        }
        return null;
    }

    public static Collection<URL> discoverWsDevicesAsUrls(String regexpProtocol, String regexpPath, boolean useIpv4,
                                                          String... targetAddresses) {
        String uuid = UUID.randomUUID().toString();
        String probeMsgTemplate = DiscoveryConfig.PROB_MESSAGE.replace("<wsa:MessageID>uuid:.*</wsa:MessageID>", "<wsa:MessageID>uuid:" + uuid + "</wsa:MessageID>");
        final Collection<URL> urls = new TreeSet<>(new URLComparator());
        for (String key : discoverWsDevices(probeMsgTemplate, useIpv4, targetAddresses)) {
            try {
                final URL url = new URL(key);
                boolean ok = true;
                if (!regexpProtocol.isEmpty() && !url.getProtocol().matches(regexpProtocol)) {
                    ok = false;
                }
                if (!regexpPath.isEmpty() && !url.getPath().matches(regexpPath)) {
                    ok = false;
                }
                if (ok) {
                    urls.add(url);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }


    /**
     * Discover WS device on the local network
     *
     * @param useIpv4
     * @param targetAddresses
     * @return list of unique devices access strings which might be URLs in most
     * cases
     */
    public static Collection<String> discoverWsDevices(String probeMsgTemplate, boolean useIpv4, String... targetAddresses) {
        final Collection<String> addresses = new ConcurrentSkipListSet<>();
        final CountDownLatch serverStarted = new CountDownLatch(1);
        final CountDownLatch serverFinished = new CountDownLatch(1);
        final Collection<InetAddress> addressList = new ArrayList<>();

        if (targetAddresses != null && targetAddresses.length > 0) {
            for (String addressStr : targetAddresses) {
                try {
                    InetAddress addr = InetAddress.getByName(addressStr);
                    addressList.add(addr);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                if (interfaces != null) {
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface anInterface = interfaces.nextElement();
                        final List<InterfaceAddress> interfaceAddresses = anInterface.getInterfaceAddresses();
                        for (InterfaceAddress address : interfaceAddresses) {
                            addressList.add(address.getAddress());
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (final InetAddress address : addressList) {
            if (useIpv4 && address instanceof Inet6Address) {
                continue;
            }
            if (!useIpv4 && address instanceof Inet4Address) {
                continue;
            }
            final int port = RANDOM.nextInt(20000) + 40000;
            try {
                DatagramSocket socket = new DatagramSocket(port);
                Thread probeReceiver = new ProbeReceiverThread(addresses, serverStarted, socket, serverFinished);
                Thread probeSender = new ProbeSenderThread(address, socket, probeMsgTemplate, serverStarted, serverFinished, probeReceiver);
                executorService.submit(probeSender);
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(DiscoveryConfig.WS_DISCOVERY_TIMEOUT + 2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }
        return addresses;
    }

}
