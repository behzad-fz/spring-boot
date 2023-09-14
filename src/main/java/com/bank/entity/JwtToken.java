package com.bank.entity;

public interface JwtToken {
    void setExpired(boolean b);
    void setRevoked(boolean b);
}
