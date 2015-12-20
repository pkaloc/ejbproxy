package ejbproxy.runtime.basic.loggers.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Type;

public class XmlCalendarGsonSerializer implements
		JsonSerializer<XMLGregorianCalendar>,
		JsonDeserializer<XMLGregorianCalendar> {

	private static DatatypeFactory datatypeFactory = getDatatypeFactory();

	@Override
	public JsonElement serialize(XMLGregorianCalendar src, Type typeOfSrc,
			JsonSerializationContext context) {
		return new JsonPrimitive(src.toXMLFormat());
	}

	@Override
	public XMLGregorianCalendar deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		return datatypeFactory.newXMLGregorianCalendar(json
				.getAsJsonPrimitive().getAsString());
	}

	private static DatatypeFactory getDatatypeFactory() {
		try {
			return DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
}
