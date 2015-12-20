package ejbproxy.runtime.basic.loggers.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ejbproxy.runtime.basic.loggers.AbstractStandardInvocationLogger;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Standard logger which uses Gson to log invocations´ result/exception.
 */
public class GsonStandardInvocationLogger extends
		AbstractStandardInvocationLogger {
	private final Gson gson;

	public GsonStandardInvocationLogger() {
		gson = createGson();
	}

	@Override
	protected String convertResultToString(Object result) {
		return gson.toJson(result);
	}

	private static Gson createGson() {
		GsonBuilder gb = new GsonBuilder();
		gb.setPrettyPrinting();
		gb.registerTypeAdapter(XMLGregorianCalendar.class,
				new XmlCalendarGsonSerializer());

		return gb.create();
	}
}
