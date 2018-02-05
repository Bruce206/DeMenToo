package de.bruss.demontoo.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CustomZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	
	@Override
	public ZonedDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

        return ZonedDateTime.parse(jp.getValueAsString(), formatter);
		
	}

}
