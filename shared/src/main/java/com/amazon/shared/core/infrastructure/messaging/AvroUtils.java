package com.amazon.shared.core.infrastructure.messaging;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroUtils {

    private AvroUtils() {}

    public static byte[] toBytes(SpecificRecord event) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            var encoder = EncoderFactory.get().binaryEncoder(baos, null);

            new SpecificDatumWriter<>(event.getSchema()).write(event, encoder);
            encoder.flush();

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize Avro record", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static SpecificRecord fromBytes(byte[] bytes, String eventType) {
        try {
            Class<? extends SpecificRecord> clazz = (Class<? extends SpecificRecord>) Class.forName(eventType);

            var schema = SpecificData.get().getSchema(clazz);
            var reader = new SpecificDatumReader<SpecificRecord>(schema);
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);

            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize Avro record", e);
        }
    }
}
