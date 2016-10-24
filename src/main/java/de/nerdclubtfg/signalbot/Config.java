package de.nerdclubtfg.signalbot;

import java.util.HashMap;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Config {
	
	@JsonProperty
	private HashMap<String, Boolean> plugins = new HashMap<>();
	
	@JsonProperty
	private HashSet<String> sudoers = new HashSet<>();
	
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
		} else {
			sudoers.add(phoneNumber);
		}
	}

}
