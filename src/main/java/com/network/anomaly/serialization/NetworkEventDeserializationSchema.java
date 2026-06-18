package com.network.anomaly.serialization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.network.anomaly.model.NetworkEvent;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class NetworkEventDeserializationSchema implements KafkaRecordDeserializationSchema<NetworkEvent> {

    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<NetworkEvent> out) {
        if (record.value() == null || record.value().length == 0) return;
        try {
            NetworkEvent event = mapper.readValue(record.value(), NetworkEvent.class);
            out.collect(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize network event", e);
        }
    }

    @Override
    public TypeInformation<NetworkEvent> getProducedType() {
        return TypeInformation.of(NetworkEvent.class);
    }
}
