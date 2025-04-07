package org.romanzhula.user_service.controllers.requests;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LoginRequest {

    private String username;
    private String password;

}
