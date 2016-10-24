package de.nerdclubtfg.signalbot;

import java.io.IOException;

import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;

import de.thoffbauer.signal4j.SignalService;
import de.thoffbauer.signal4j.store.User;

public class SignalInterface {
	
	private static SignalService signal;
	private static Config config;
	private static SignalBot bot;
	
	protected static void set(SignalService signal, Config config, SignalBot bot) {
		SignalInterface.signal = signal;
		SignalInterface.config = config;
		SignalInterface.bot = bot;
	}
	
	public static void sendMessage(User receiver, String body) throws IOException {
		sendMessage(receiver, new SignalServiceDataMessage(System.currentTimeMillis(), body));
	}
	
	private static void sendMessage(User receiver, SignalServiceDataMessage message) throws IOException {
		signal.sendMessage(receiver.getNumber(), message);
	}
	
	public static boolean isEnabled(String plugin) {
		return config.isEnabled(plugin);
	}
	
	public static void setEnabled(String plugin, boolean enabled) {
		bot.setEnabled(plugin, enabled);
	}
	
	public static boolean isSudo(User user) {
		return config.isSudo(user.getNumber());
	}
	
	public static void setSudo(User user, boolean sudo) {
		config.setSudo(user.getNumber(), sudo);
	}

}
