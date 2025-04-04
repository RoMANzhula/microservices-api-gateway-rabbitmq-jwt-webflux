package org.romanzhula.user_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.romanzhula.user_service.models.enums.UserRole;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("users")
public class User {

    @Id
    @Column("user_id")
    private UUID id;

    @Column("nickname")
    private String username;

    private String firstName;

    private String lastName;

    private String email;

    @Column("phone_number")
    private String phone;

    @JsonIgnore
    @Column("password_crypt")
    private String password;

    private UserRole role;

}
