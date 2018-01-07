package de.bruss.demontoo.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	
	@Override
	public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

        return LocalDateTime.parse(jp.getValueAsString(), formatter);
		
	}

}
