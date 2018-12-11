package com.nettechinternational.melissa;

import com.nettechinternational.melissa.store.Token;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class Client {

    private String socketId;
    private Token token;
    private Optional<String> tokenCode;

    public Client(String socketId, String tokenCode) {
        this.socketId = socketId;
        this.tokenCode = Optional.ofNullable(tokenCode);
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public Optional<String> getTokenCode() {
        return tokenCode;
    }

    public void setTokenCode(String tokenCode) {
        this.tokenCode = Optional.of(tokenCode);
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.socketId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Client other = (Client) obj;
        return Objects.equals(this.socketId, other.socketId);
    }

}
