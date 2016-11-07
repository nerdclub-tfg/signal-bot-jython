package de.nerdclubtfg.signalbot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyString;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class Storage {
	
	public static final String PATH = "data";
	
	public static PyDictionary load(String name) throws IOException {
		File file = Paths.get(PATH, name + ".json").toFile();
		if(!file.exists()) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addDeserializer(PyDictionary.class, new PyDictionaryDeserializer());
		mapper.registerModule(module);
		PyDictionary pyDictionary = mapper.readValue(file, PyDictionary.class);
		return pyDictionary;
	}
	
	public static void save(String name, PyDictionary pyDictionary) throws IOException {
		File directory = new File(PATH);
		if(!directory.exists()) {
			directory.mkdir();
		}
		File file = Paths.get(PATH, name + ".json").toFile();
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.writeValue(file, pyDictionary);
	}

}

@SuppressWarnings("serial")
class PyDictionaryDeserializer extends StdDeserializer<PyDictionary> {
	
	public PyDictionaryDeserializer() {
		super(PyDictionary.class);
	}
	
	@SuppressWarnings("unchecked")
	public PyDictionary deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		HashMap<String, Object> map = p.readValueAs(HashMap.class);
		return (PyDictionary) toPy(map);
	}
	
	@SuppressWarnings("unchecked")
	PyObject toPy(Object o) {
		if(o instanceof List) {
			Iterator<PyObject> converter = ((List<Object>) o).stream()
					.map(this::toPy)
					.iterator();
			return new PyList(converter);
		} else if(o instanceof Map) {
			PyDictionary dictionary = new PyDictionary();
			((Map<Object, Object>) o).entrySet().stream()
					.map(v -> new AbstractMap.SimpleEntry<>(toPy(v.getKey()), toPy(v.getValue())))
					.forEach(v -> dictionary.put(v.getKey(), v.getValue()));
			return dictionary;
		} else if(o instanceof Integer) {
			return new PyInteger((int) o);
		} else if(o instanceof Long) {
			return new PyLong((long) o);
		} else if(o instanceof Float) {
			return new PyFloat((float) o);
		} else if(o instanceof Double) {
			return new PyFloat((double) o);
		} else if(o instanceof Boolean) {
			return new PyBoolean((boolean) o);
		} else if(o instanceof Character) {
			return new PyString((char) o);
		} else if(o instanceof String) {
			return new PyString((String) o);
		} else {
			return null;
		}
	}
	
}