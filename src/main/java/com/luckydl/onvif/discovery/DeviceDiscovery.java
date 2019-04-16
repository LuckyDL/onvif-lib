package com.luckydl.onvif.discovery;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

/**
 * Device discovery class to list local accessible devices probed per UDP probe
 * messages.
 * 
 * @author th
 * @date 2015-06-18
 * @version 0.1
 */
public class DeviceDiscovery {

	public static String WS_DISCOVERY_SOAP_VERSION = "SOAP 1.2 Protocol";
	public static String WS_DISCOVERY_CONTENT_TYPE = "application/soap+xml";
	public static int WS_DISCOVERY_TIMEOUT = 4000;
	public static int WS_DISCOVERY_PORT = 3702;
	private static final Random random = new SecureRandom();

	/**
	 * Discover WS device on the local network and returns Urls
	 * 
	 * @return list of unique device urls
	 */
	public static Collection<URL> discoverWsDevicesAsUrls(boolean useIpv4) {
		return discoverWsDevicesAsUrls("", "",useIpv4);
	}
	public static Collection<URL> discoverWsDevicesAsUrls(String regexpProtocol, String regexpPath, boolean useIpv4,
			String... targetAddresses) {
		String probeMsgTemplate;
		try {
			File probeMsgFile = new File("/probe-template.xml");
			probeMsgTemplate = FileUtils.readFileToString(probeMsgFile);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		String uuid = UUID.randomUUID().toString();
		probeMsgTemplate = probeMsgTemplate.replace("<wsa:MessageID>uuid:.*</wsa:MessageID>", "<wsa:MessageID>uuid:" + uuid + "</wsa:MessageID>");
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
	 * Discover WS device on the local network with specified filter
	 * 
	 * @param regexpProtocol
	 *            url protocol matching regexp like "^http$", might be empty ""
	 * @param regexpPath
	 *            url path matching regexp like "onvif", might be empty ""
	 * @return list of unique device urls filtered
	 */
	public static Collection<URL> discoverWsDevicesAsUrls(String regexpProtocol, String regexpPath, boolean useIpv4) {
		return discoverWsDevicesAsUrls(regexpProtocol,regexpPath,useIpv4);
	}

	/**
	 * Discover WS device on the local network
	 * @param useIpv4 
	 * @param targetAddresses 
	 * 
	 * @return list of unique devices access strings which might be URLs in most
	 *         cases
	 */
	public static Collection<String> discoverWsDevices(String probeMsgTemplate, boolean useIpv4, String... targetAddresses) {
		final Collection<String> addresses = new ConcurrentSkipListSet<>();
		final CountDownLatch serverStarted = new CountDownLatch(1);
		final CountDownLatch serverFinished = new CountDownLatch(1);
		final Collection<InetAddress> addressList = new ArrayList<>();
		
		if(targetAddresses!=null && targetAddresses.length>0){
			for (String addressStr : targetAddresses) {
				try {
					InetAddress addr = InetAddress.getByName(addressStr);
					addressList.add(addr);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}else{
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
			if(useIpv4 && address instanceof Inet6Address) {
				continue;
			}
			if(!useIpv4 && address instanceof Inet4Address) {
				continue;
			}
			final int port = random.nextInt(20000) + 40000;
			try {
				DatagramSocket socket = new DatagramSocket(port);
				Thread probeReceiver = new ProbeReceiverThread(addresses, serverStarted, socket, serverFinished);
				Thread probeSender = new ProbeSenderThread(address, socket, probeMsgTemplate, serverStarted, serverFinished,probeReceiver);
				executorService.submit(probeSender);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			executorService.shutdown();
			executorService.awaitTermination(WS_DISCOVERY_TIMEOUT + 2000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ignored) {
		}
		return addresses;
	}

}
