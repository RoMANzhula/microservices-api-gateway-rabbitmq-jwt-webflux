package org.romanzhula.microservices_common.security.jwt;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;


@Getter
class JWToken extends AbstractAuthenticationToken {

    private final String token;

    JWToken(String token) {
        super(null);
        this.token = token;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JWToken test)) {
            return false;
        }

        if (this.getToken() == null && test.getToken() != null) {
            return false;
        }

        if (this.getToken() != null && !this.getToken().equals(test.getToken())) {
            return false;
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();

        if (this.getToken() != null) {
            code ^= this.getToken().hashCode();
        }

        return code;
    }

}
