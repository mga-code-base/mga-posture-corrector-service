package com.mygym.app.logging;

import java.time.Duration;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;

public class KafkaLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final String KAFKA_LOGGER_PREFIX = "org.apache.kafka";

    private Encoder<ILoggingEvent> encoder;
    private String topic = "mga-logs";
    private String bootstrapServers;
    private String securityProtocol = "SSL";
    private String truststoreLocation;
    private String truststorePassword;
    private String keystoreLocation;
    private String keystorePassword;
    private Producer<byte[], byte[]> producer;

    @Override
    public void start() {
        if (encoder == null) {
            addError("No encoder configured for " + getName());
            return;
        }
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            addWarn("No Kafka bootstrap servers configured, " + getName() + " stays disabled");
            return;
        }
        try {
            producer = createProducer();
            super.start();
        } catch (RuntimeException e) {
            addError("Could not start Kafka log appender", e);
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (producer == null || event.getLoggerName().startsWith(KAFKA_LOGGER_PREFIX)) {
            return;
        }
        byte[] payload = encoder.encode(event);
        producer.send(new ProducerRecord<>(topic, payload), (metadata, exception) -> {
            if (exception != null) {
                addWarn("Dropped log event: " + exception.getMessage());
            }
        });
    }

    @Override
    public void stop() {
        if (producer != null) {
            producer.close(Duration.ofSeconds(5));
            producer = null;
        }
        super.stop();
    }

    Producer<byte[], byte[]> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "50");
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "2000");
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "30000");
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, getName());
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        putIfPresent(props, SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreLocation);
        putIfPresent(props, SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
        putIfPresent(props, SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystoreLocation);
        putIfPresent(props, SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
        if (keystoreLocation != null && !keystoreLocation.isBlank()) {
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
        }
        return new KafkaProducer<>(props);
    }

    private static void putIfPresent(Properties props, String key, String value) {
        if (value != null && !value.isBlank()) {
            props.put(key, value);
        }
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public void setTruststoreLocation(String truststoreLocation) {
        this.truststoreLocation = truststoreLocation;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public void setKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }
}
