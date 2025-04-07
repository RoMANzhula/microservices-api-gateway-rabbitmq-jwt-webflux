package org.romanzhula.microservices_common.security.jwt.interfaces;

import org.springframework.security.core.userdetails.UserDetails;


public interface TokenProvider {

    String generateToken(UserDetails userDetails);

}
