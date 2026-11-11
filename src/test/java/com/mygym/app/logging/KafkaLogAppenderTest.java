package com.mygym.app.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;

class KafkaLogAppenderTest {

    private final MockProducer<byte[], byte[]> producer = new MockProducer<>(true, null, new ByteArraySerializer(),
            new ByteArraySerializer());
    private final LoggerContext context = new LoggerContext();
    private KafkaLogAppender appender;

    @BeforeEach
    void setUp() {
        appender = new KafkaLogAppender() {
            @Override
            Producer<byte[], byte[]> createProducer() {
                return producer;
            }
        };
        appender.setContext(context);
        appender.setName("kafka");
        appender.setTopic("mga-logs");
        appender.setBootstrapServers("broker:9092");
        appender.setEncoder(new EncoderBase<>() {
            @Override
            public byte[] headerBytes() {
                return new byte[0];
            }

            @Override
            public byte[] encode(ILoggingEvent event) {
                return event.getFormattedMessage().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public byte[] footerBytes() {
                return new byte[0];
            }
        });
        appender.start();
    }

    @Test
    @DisplayName("Should publish encoded application log events to the log topic")
    void testAppendPublishesEvent() {
        appender.doAppend(event("com.mygym.app.service.SquatAnalysisService", "squat analysed"));

        assertEquals(1, producer.history().size());
        assertEquals("mga-logs", producer.history().get(0).topic());
        assertEquals("squat analysed", new String(producer.history().get(0).value(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Should skip Kafka client logs to avoid feeding back into itself")
    void testKafkaClientLogsAreSkipped() {
        appender.doAppend(event("org.apache.kafka.clients.NetworkClient", "connection lost"));

        assertTrue(producer.history().isEmpty());
    }

    @Test
    @DisplayName("Should stay disabled without bootstrap servers")
    void testDisabledWithoutBootstrapServers() {
        KafkaLogAppender disabled = new KafkaLogAppender();
        disabled.setContext(context);
        disabled.setEncoder(new EncoderBase<>() {
            @Override
            public byte[] headerBytes() {
                return new byte[0];
            }

            @Override
            public byte[] encode(ILoggingEvent event) {
                return new byte[0];
            }

            @Override
            public byte[] footerBytes() {
                return new byte[0];
            }
        });
        disabled.start();

        assertFalse(disabled.isStarted());
    }

    private ILoggingEvent event(String logger, String message) {
        LoggingEvent event = new LoggingEvent();
        event.setLoggerName(logger);
        event.setLevel(Level.INFO);
        event.setMessage(message);
        event.setLoggerContext(context);
        return event;
    }
}
