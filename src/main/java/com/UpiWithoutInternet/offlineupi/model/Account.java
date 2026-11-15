package com.UpiWithoutInternet.offlineupi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Account {

    @Id
    private String vpa;

    @Column(nullable = false)
    private String holdername;

    @Column(nullable = false, precision = 19,scale =2)
    private BigDecimal balance;

    @Version
    private Long version;

    public Account(String vpa, String holdername, BigDecimal amount) {
        this.vpa = vpa;
        this.holdername = holdername;
        this.balance = amount;
    }
}
