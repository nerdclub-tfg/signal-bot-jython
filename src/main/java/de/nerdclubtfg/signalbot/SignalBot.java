package de.nerdclubtfg.signalbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.python.util.PythonInterpreter;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.messages.multidevice.ReadMessage;

import de.thoffbauer.signal4j.SignalService;
import de.thoffbauer.signal4j.listener.ConversationListener;
import de.thoffbauer.signal4j.listener.SecurityExceptionListener;
import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public class SignalBot implements ConversationListener, SecurityExceptionListener {

	private static final String USER_AGENT = "signal-bot";
	
	private SignalService signal;
	private Timer preKeysTimer;
	
	private PythonInterpreter python = new PythonInterpreter();
	private List<String> plugins;


	public static void main(String[] args) {
		new SignalBot("https://textsecure-service-staging.whispersystems.org", "+4915751776461");
	}
	
	public SignalBot(String url, String phoneNumber) {
		try {
			loadSignal(url, phoneNumber);
		} catch(IOException e) {
			System.err.println("Could not connect to signal server! " + e.getMessage() + 
					" (" + e.getClass().getSimpleName() + ")");
			return;
		}
		try {
			loadPlugins();
		} catch (IOException e) {
			System.err.println("Could not load plugins: " + e.getMessage() + 
					" (" + e.getClass().getSimpleName() + ")");
			return;
		}
		try {
			while(true) {
				signal.pull(60 * 1000);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void loadSignal(String url, String phoneNumber) throws IOException {
		signal = new SignalService();
		signal.addConversationListener(this);
		signal.addSecurityExceptionListener(this);
		if(!signal.isRegistered()) {
			signal.startConnectAsPrimary(url, USER_AGENT, phoneNumber, false);
			Scanner scanner = new Scanner(System.in);
			System.out.println("Verification code: ");
			String code = scanner.nextLine();
			scanner.close();
			signal.finishConnectAsPrimary(code);
		}
		preKeysTimer = new Timer(true);
		preKeysTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					signal.checkPreKeys(80);
				} catch (IOException e) {
					System.err.println("Could not update prekeys! " + e.getMessage());
				}
			}
		}, 0, 30 * 1000);
	}

	private void loadPlugins() throws IOException {
		plugins = getResourceFiles("/plugins").stream()
				.map(v -> v.substring(0, v.length() - 3))
				.filter(v -> !v.equals("__init__"))
				.collect(Collectors.toList());
		StringBuilder sb = new StringBuilder();
		for(String plugin : plugins) {
			sb.append("from plugins import " + plugin + "\n");
		}
		sb.append("def onMessage(sender, message, group):\n");
		for(String plugin : plugins) {
			sb.append("  " + plugin + ".onMessage(sender, message, group)\n");
		}
		python.exec(sb.toString());
	}

	@Override
	public void onSecurityException(User user, Exception e) {
		System.err.println("Security exception from " + user.getNumber() + ": " + e.getMessage() + 
				" (" + e.getClass().getSimpleName() + ")");
	}

	@Override
	public void onMessage(User sender, SignalServiceDataMessage message, Group group) {
		python.set("paramSender", sender);
		python.set("paramMessage", message);
		python.set("paramGroup", group);
		python.exec("onMessage(paramSender, paramMessage, paramGroup)");
	}

	private List<String> getResourceFiles(String path) throws IOException {
		List<String> filenames = new ArrayList<>();

		try (InputStream in = SignalBot.class.getResourceAsStream(path);
				BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String resource;

			while ((resource = br.readLine()) != null) {
				filenames.add(resource);
			}
		}

		return filenames;
	}

	@Override
	public void onContactUpdate(User contact) {
		// ignore
	}

	@Override
	public void onGroupUpdate(User sender, Group group) {
		// ignore
	}

	@Override
	public void onReadUpdate(List<ReadMessage> readList) {
		// ignore
	}

}
