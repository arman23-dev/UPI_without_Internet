package com.UpiWithoutInternet.offlineupi.service;

import com.UpiWithoutInternet.offlineupi.model.Account;
import com.UpiWithoutInternet.offlineupi.model.PaymentInstruction;
import com.UpiWithoutInternet.offlineupi.model.Transaction;
import com.UpiWithoutInternet.offlineupi.repository.AccountRepository;
import com.UpiWithoutInternet.offlineupi.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class SettlementService {
    private  AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    SettlementService(AccountRepository accountRepository,
                      TransactionRepository transactionRepository){
        this.accountRepository = accountRepository;
        this.transactionRepository=transactionRepository;
    }

    public Transaction settle(PaymentInstruction paymentInstruction,
                              String packetHash,
                              String bridgeNodeId,
                              int hopCount){

        Account senderAccount= accountRepository.findById(paymentInstruction.getSenderVpa())
                .orElseThrow(()-> new IllegalArgumentException("Unknown Sender vpa : " +paymentInstruction.getSenderVpa()));

        Account recieverAccount = accountRepository.findById(paymentInstruction.getReceiverVpa())
                .orElseThrow(()-> new IllegalArgumentException("Unknown Reciever vpa : " + paymentInstruction.getReceiverVpa()));

        BigDecimal amount = paymentInstruction.getAmount();
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (senderAccount.getBalance().compareTo(amount) < 0) {
//            log.warn("Insufficient balance: {} has ₹{}, tried to send ₹{}",
//                    senderAccount.getVpa(), senderAccount.getBalance(), balance);
            return recordRejected(paymentInstruction, packetHash, bridgeNodeId, hopCount);
        }
        senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
        recieverAccount.setBalance(recieverAccount.getBalance().add(amount));
        accountRepository.save(senderAccount);
        accountRepository.save(recieverAccount);

        Transaction tx = new Transaction();
        tx.setPacketHash(packetHash);
        tx.setSenderVpa(paymentInstruction.getSenderVpa());
        tx.setReceiverVpa(paymentInstruction.getReceiverVpa());
        tx.setAmount(amount);
        tx.setSignedAt(Instant.ofEpochMilli(paymentInstruction.getSignedAt()));
        tx.setSettledAt(Instant.now());
        tx.setBridgeNodeId(bridgeNodeId);
        tx.setHopCount(hopCount);
        tx.setStatus(Transaction.Status.SETTLED);
        transactionRepository.save(tx);

        return tx;
    }
    private Transaction recordRejected(PaymentInstruction paymentInstruction,String packetHash,String bridgeNodeId, int hopCount){

            Transaction tx = new Transaction();
            tx.setPacketHash(packetHash);
            tx.setSenderVpa(paymentInstruction.getSenderVpa());
            tx.setReceiverVpa(paymentInstruction.getReceiverVpa());
            tx.setAmount(paymentInstruction.getAmount());
            tx.setSignedAt(Instant.ofEpochMilli(paymentInstruction.getSignedAt()));
            tx.setSettledAt(Instant.now());
            tx.setBridgeNodeId(bridgeNodeId);
            tx.setHopCount(hopCount);
            tx.setStatus(Transaction.Status.REJECTED);
            return transactionRepository.save(tx);
        }

}
