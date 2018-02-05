package de.bruss.demontoo.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CustomZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime> {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	@Override
	public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider arg2) throws IOException {
	    gen.writeString(formatter.format(value));
	}

}
