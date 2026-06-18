package com.network.anomaly.sink;

import com.network.anomaly.model.AnomalyEvent;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.util.concurrent.TimeUnit;

public class AnomalyCsvSink {

    private AnomalyCsvSink() {}

    public static StreamingFileSink<String> create(String outputPath) {
        return StreamingFileSink
            .forRowFormat(new Path(outputPath), new SimpleStringEncoder<String>("UTF-8"))
            .withRollingPolicy(
                DefaultRollingPolicy.builder()
                    .withRolloverInterval(TimeUnit.MINUTES.toMillis(5))
                    .withInactivityInterval(TimeUnit.MINUTES.toMillis(1))
                    .withMaxPartSize(1024 * 1024 * 10)
                    .build())
            .build();
    }
}
