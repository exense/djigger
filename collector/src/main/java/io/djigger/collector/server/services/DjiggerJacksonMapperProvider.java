package io.djigger.collector.server.services;

import java.io.IOException;

import javax.ws.rs.ext.ContextResolver;

import io.djigger.mixin.InstrumentSubscriptionMixin;
import io.djigger.monitoring.java.instrumentation.InstrumentSubscription;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;

public class DjiggerJacksonMapperProvider implements ContextResolver<ObjectMapper> {

	private final ObjectMapper mapper;

	public DjiggerJacksonMapperProvider() {
		mapper = createMapper();
	}

	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}

	/**
	 * @return an ObjectMapper for the UI or export layer
	 */
	public static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JSR353Module());
		mapper.registerModule(new JsonOrgModule());
		mapper.registerModule(new SimpleModule("jersey", new Version(1, 0, 0, null,null,null)) //
				.addSerializer(_id, _idSerializer()) //
				.addDeserializer(_id, _idDeserializer()));
		mapper.registerModule(new SimpleModule() {{
				this.setMixInAnnotation(InstrumentSubscription.class, InstrumentSubscriptionMixin.class);
			}});
		return mapper;
	}

	private static Class<ObjectId> _id = ObjectId.class;

	private static JsonDeserializer<ObjectId> _idDeserializer() {
		return new JsonDeserializer<ObjectId>() {
			public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
				return new ObjectId(jp.readValueAs(String.class));
			}
		};
	}

	private static JsonSerializer<Object> _idSerializer() {
		return new JsonSerializer<Object>() {
			public void serialize(Object obj, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonProcessingException {
				jsonGenerator.writeString(obj == null ? null : obj.toString());
			}
		};
	}
}