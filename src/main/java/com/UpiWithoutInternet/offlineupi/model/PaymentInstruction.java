package com.UpiWithoutInternet.offlineupi.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor

public class PaymentInstruction {
    private String senderVpa;
    private String receiverVpa;
    private BigDecimal amount;
    private String pinHash;
    private String nonce;     // UUID, unique per payment intent universal uniquely identityfier (128 bit value used to identify anything imaginable on internet)
    private Long signedAt;    // epoch millis, when sender signed

    public PaymentInstruction(String senderVpa, String receiverVpa, BigDecimal amount, String pinHash, String nonce, Long signedAt) {
        this.senderVpa = senderVpa;
        this.receiverVpa = receiverVpa;
        this.amount = amount;
        this.pinHash = pinHash;
        this.nonce = nonce;
        this.signedAt = signedAt;
    }
}
