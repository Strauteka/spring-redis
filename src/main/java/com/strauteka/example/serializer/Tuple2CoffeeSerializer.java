package com.strauteka.example.serializer;

import com.strauteka.example.entity.Coffee;
import org.springframework.boot.jackson.JsonComponent;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import reactor.util.function.Tuple2;

import java.io.IOException;

@JsonComponent
public class Tuple2CoffeeSerializer extends StdSerializer<Tuple2<Coffee, Boolean>> {

    private enum SerializerKeys {
        coffee, isSuccess
    }

    public Tuple2CoffeeSerializer() {
        this(null);
    }

    protected Tuple2CoffeeSerializer(Class<Tuple2<Coffee, Boolean>> t) {
        super(t);
    }

    @Override
    public void serialize(Tuple2<Coffee, Boolean> objects, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField(SerializerKeys.coffee.name(), objects.getT1());
        jsonGenerator.writeBooleanField(SerializerKeys.isSuccess.name(), objects.getT2());
        jsonGenerator.writeEndObject();
    }
}
