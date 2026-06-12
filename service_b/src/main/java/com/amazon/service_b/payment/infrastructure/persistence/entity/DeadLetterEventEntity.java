package com.amazon.service_b.payment.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dead_letter_events")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeadLetterEventEntity {

    @Id
    private UUID id;

    private String topic;

    @Column(columnDefinition = "BYTEA")
    private byte[] payload;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String exceptionMessage;

    private Integer originalPartition;

    private Long originalOffset;

    private Instant occurredAt;
}
