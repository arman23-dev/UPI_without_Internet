package com.UpiWithoutInternet.offlineupi.service;

import com.UpiWithoutInternet.offlineupi.crypto.HybridCryptoService;
import com.UpiWithoutInternet.offlineupi.model.MeshPacket;
import com.UpiWithoutInternet.offlineupi.model.PaymentInstruction;
import com.UpiWithoutInternet.offlineupi.model.Transaction;
import com.UpiWithoutInternet.offlineupi.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import java.time.Instant;

@Service
public class BridgeIngestionService {


    private final HybridCryptoService hybridCryptoService;
    private final IdempotencyService idempotencyService;
    private final SettlementService settlementService;

    @Value("${upi.mesh.packet-max-age-seconds:86400}")
    private long maxAgeSeconds;

    BridgeIngestionService (HybridCryptoService hybridCryptoService,
                            IdempotencyService idempotencyService,
                            SettlementService settlementService){

        this.hybridCryptoService=hybridCryptoService;
        this.idempotencyService=idempotencyService;
        this.settlementService = settlementService;
    }
    public IngestResult ingest(MeshPacket packet, String bridgeNodeId, int hopCount){
        try {
            String packetHash = hybridCryptoService.hashCiphertext(packet.getCiphertext());
            if (!idempotencyService.claim(packetHash)) {
                return IngestResult.duplicate(packetHash);
            }

            PaymentInstruction paymentInstruction;

            try {
                paymentInstruction = hybridCryptoService.decrypt(packet.getCiphertext());
            } catch (Exception e) {
                return IngestResult.invalid(packetHash, "decryption_failed");
            }
            long ageSeconds = (Instant.now().toEpochMilli() - paymentInstruction.getSignedAt()) / 1000;
            if (ageSeconds > maxAgeSeconds) {
                return IngestResult.invalid(packetHash, "stale_packet");
            }
            if (ageSeconds < -300) {
                return IngestResult.invalid(packetHash, "future_dated");
            }
            Transaction tx = settlementService.settle(paymentInstruction, packetHash, bridgeNodeId, hopCount);
            return IngestResult.settled(packetHash, tx);
        }
        catch (Exception e) {
            return IngestResult.invalid("?", "internal_error: " + e.getMessage());
        }



    }

    public record IngestResult(String outcome, String packetHash, String reason, Long transactionId) {
        public static IngestResult settled(String hash, Transaction tx) {
            return new IngestResult("SETTLED", hash, null, tx.getId());
        }
        public static IngestResult duplicate(String hash) {
            return new IngestResult("DUPLICATE_DROPPED", hash, null, null);
        }
        public static IngestResult invalid(String hash, String reason) {
            return new IngestResult("INVALID", hash, reason, null);
        }
    }



}
