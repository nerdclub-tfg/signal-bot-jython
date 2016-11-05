package de.nerdclubtfg.signalbot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Config {
	
	private static final String PATH = "config.json";
	
	@JsonProperty
	private HashMap<String, Boolean> plugins = new HashMap<>();
	
	@JsonProperty
	private HashSet<String> sudoers = new HashSet<>();
	
	public static Config load() throws IOException {
		Config config;
		// load a maybe existing config
		File file = new File(PATH);
		ObjectMapper mapper = new ObjectMapper();
		if(file.exists()) {
			config = mapper.readValue(file, Config.class);
		} else {
			config = new Config();
		}
		
		// update it with default config
		Config defaultConfig = mapper.readValue(
				Config.class.getResourceAsStream("defaultConfig.json"), Config.class);
		// Add new plugin entries only
		defaultConfig.plugins.entrySet().stream()
				.filter(v -> !config.plugins.containsKey(v.getKey()))
				.forEach(v -> config.plugins.put(v.getKey(), v.getValue()));
		// ignore sudoers as there should not be any sudoers inside the default config
		
		// save it
		config.save();
		
		return config;
	}
	
	public void save() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			mapper.writeValue(new File(PATH), this);
		} catch (IOException e) {
			System.err.println("Could not save config! Changes will be lost: " + e.getMessage() +
					" (" + e.getClass().getSimpleName() + ")");
		}
	}
	
	public boolean isEnabled(String plugin) {
		Boolean boxed = plugins.get(plugin);
		if(boxed != null) {
			return boxed;
		} else {
			throw new IllegalArgumentException(plugin + " not known in config!");
		}
	}
	
	public void setEnabled(String plugin, boolean enabled) {
		plugins.put(plugin, enabled);
		save();
	}
	
	public boolean isSudo(String phoneNumber) {
		return sudoers.contains(phoneNumber);
	}
	
	public void setSudo(String phoneNumber, boolean sudo) {
		boolean isSudo = isSudo(phoneNumber);
		if(isSudo == sudo) {
			return;
		} else if(isSudo) {
			sudoers.remove(phoneNumber);
			save();
		} else {
			sudoers.add(phoneNumber);
			save();
		}
	}

}
