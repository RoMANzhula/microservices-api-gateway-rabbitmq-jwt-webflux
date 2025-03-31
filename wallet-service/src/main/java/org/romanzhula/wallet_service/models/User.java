package org.romanzhula.wallet_service.models;

import lombok.Data;

import java.util.UUID;

/**
 * This class is used to retrieve user data via RabbitMQ.
 * It is not connected to a database and is not used to store data (it is dto).
 */
@Data
public class User {

    private UUID id;

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

}
