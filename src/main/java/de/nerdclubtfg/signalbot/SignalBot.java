package de.nerdclubtfg.signalbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.python.util.PythonInterpreter;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.messages.multidevice.ReadMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.thoffbauer.signal4j.SignalService;
import de.thoffbauer.signal4j.listener.ConversationListener;
import de.thoffbauer.signal4j.listener.SecurityExceptionListener;
import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public class SignalBot implements ConversationListener, SecurityExceptionListener {

	private static final String USER_AGENT = "signal-bot";
	private static final String CONFIG_PATH = "config.json";

	private Config config;
	
	private SignalService signal;
	private Timer preKeysTimer;
	
	private PythonInterpreter python = new PythonInterpreter();
	private List<String> plugins;

	public static void main(String[] args) {
		new SignalBot();
	}
	
	public SignalBot() {
		try {
			loadConfig();
		} catch(IOException e) {
			System.err.println("Could not load config! " + e.getMessage() + 
					" (" + e.getClass().getSimpleName() + ")");
			return;
		}
		try {
			loadSignal();
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
		SignalInterface.set(signal, config, this);
		System.out.println("Running");
		try {
			while(true) {
				signal.pull(60 * 1000);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void loadConfig() throws IOException {
		File configFile = new File(CONFIG_PATH);
		if(!configFile.exists()) {
			Files.copy(SignalBot.class.getResourceAsStream("defaultConfig.json"), configFile.toPath(), 
					StandardCopyOption.REPLACE_EXISTING);
		}
		ObjectMapper mapper = new ObjectMapper();
		config = mapper.readValue(configFile, Config.class);
	}
	
	private void saveConfig() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File(CONFIG_PATH), config);
	}

	private void loadSignal() throws IOException {
		signal = new SignalService();
		signal.addConversationListener(this);
		signal.addSecurityExceptionListener(this);
		if(!signal.isRegistered()) {
			Scanner scanner = new Scanner(System.in);
			System.out.println("Url (or 'production' or 'staging' for whispersystems' server):");
			String url = scanner.nextLine();
			url = url.replace("production", "https://textsecure-service.whispersystems.org");
			url = url.replace("staging", "https://textsecure-service-staging.whispersystems.org");
			System.out.println("Phone Number:");
			String phoneNumber = scanner.nextLine();
			System.out.println("Device type, one of 'primary' (new registration) or 'secondary' (linking):");
			String deviceType = scanner.nextLine();
			
			if(deviceType.equals("primary")) {
				signal.startConnectAsPrimary(url, USER_AGENT, phoneNumber, false);
				System.out.println("Verification code: ");
				String code = scanner.nextLine();
				code = code.replace("-", "");
				signal.finishConnectAsPrimary(code);
			} else if(deviceType.equals("secondary")) {
				try {
					String uuid = signal.startConnectAsSecondary(url, USER_AGENT, phoneNumber);
					System.out.println("Scan this uuid as a QR code, e.g. using an online qr code generator "
							+ "(The url does not contain sensitive information):");
					System.out.println(uuid);
					signal.finishConnectAsSecondary(USER_AGENT, false);
					signal.requestSync();
				} catch (TimeoutException e) {
					scanner.close();
					throw new IOException(e);
				}
			} else {
				scanner.close();
				throw new IOException("Invalid option!");
			}
			scanner.close();
			System.out.println("Registered!");
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
		plugins = getResourceFiles("/signalbot/plugins").stream()
				.map(v -> v.substring(0, v.length() - 3))
				.filter(v -> !v.equals("__init__"))
				.collect(Collectors.toList());
		StringBuilder sb = new StringBuilder();
		for(String plugin : plugins) {
			sb.append("from signalbot.plugins." + plugin + " import " + firstUpperCase(plugin) + "\n");
		}
		sb.append("from signalbot import bot\n");
		for(String plugin : plugins) {
			sb.append("bot.plugins.append(" + firstUpperCase(plugin) + "(");
			if(config.isEnabled(plugin)) {
				sb.append("True");
			} else {
				sb.append("False");
			}
			sb.append("))\n");
		}
		python.exec(sb.toString());
	}
	
	public void setEnabled(String plugin, boolean enabled) {
		python.exec("from signalbot import bot\n"
				+ "next(v for v in bot.plugins if type(v).__name__ == '" + firstUpperCase(plugin) + "')"
						+ ".setEnabled(" + enabled + ")\n");
		config.setEnabled(plugin, enabled);
		try {
			saveConfig();
		} catch (IOException e) {
			System.err.println("Could not save config! Changes will be lost: " + e.getMessage() +
					" (" + e.getClass().getSimpleName() + ")");
		}
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
		python.exec("from signalbot import bot\n"
				+ "bot.onMessage(paramSender, paramMessage, paramGroup)");
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

	private String firstUpperCase(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
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
