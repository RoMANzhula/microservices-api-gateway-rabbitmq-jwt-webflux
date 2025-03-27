package org.romanzhula.user_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.romanzhula.user_service.models.enums.UserRole;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private UUID id;

    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    @JsonIgnore
    private String password;

    private UserRole role;

}
