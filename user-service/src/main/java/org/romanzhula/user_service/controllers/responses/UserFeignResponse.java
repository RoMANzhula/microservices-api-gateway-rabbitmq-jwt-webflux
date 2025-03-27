package org.romanzhula.user_service.controllers.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFeignResponse {

    private String id;
    private String username;
    private String password;
    private Set<String> roles;

}
