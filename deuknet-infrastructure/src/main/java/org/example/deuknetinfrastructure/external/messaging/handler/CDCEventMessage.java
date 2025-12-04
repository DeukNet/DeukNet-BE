package org.example.deuknetinfrastructure.external.messaging.handler;

import org.example.deuknetapplication.messaging.EventType;

public record CDCEventMessage(EventType eventType, String aggregateId, String payloadJson) {

}
