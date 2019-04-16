package com.luckydl.onvif;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.List;

import javax.xml.soap.SOAPException;

import org.onvif.ver10.schema.Profile;

import com.luckydl.onvif.soap.OnvifDevice;
/**
 * @author https://github.com/milg0/onvif-java-lib
 */
public class Main {

	private static final String INFO = "Commands:\n  \n  url: Get snapshort URL.\n  info: Get information about each valid command.\n  profiles: Get all profiles.\n  exit: Exit this application.";

	public static void main(String[] args) {
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
		}
		catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		cameraAddress = cameraAddress.contains(":") ? cameraAddress.split(":")[0] : cameraAddress;
		Integer port = Integer.valueOf(cameraAddress.contains(":") ? cameraAddress.split(":")[1] : "80");
		System.out.println("Connect to camera, please wait ...");
		OnvifDevice cam;
		try {
			cam = new OnvifDevice(cameraAddress, port,user, password);
		}
		catch (ConnectException | SOAPException e1) {
			System.err.println("No connection to camera, please try again.");
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
						}
						catch (SOAPException e) {
							System.err.println("Cannot grap snapshot URL, got Exception "+e.getMessage());
						}
					}
					break;
				}
				case "profiles":
					List<Profile> profiles = cam.getDevices().getProfiles();
					System.out.println("Number of profiles: " + profiles.size());
					for (Profile p : profiles) {
						System.out.println("  Profile "+p.getName()+" token is: "+p.getToken());
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
			}
			catch (IOException | SOAPException e) {
				e.printStackTrace();
			}
		}
	}
}