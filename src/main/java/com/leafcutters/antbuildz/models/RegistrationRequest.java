package com.leafcutters.antbuildz.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RegistrationRequest {

    @NotEmpty(message = "The Name cannot be blank!")
    private final String name;

    @NotEmpty(message = "The Email cannot be blank!")
    @Email(message = "Please follow standard email formatting!")
    private final String email;

    @NotEmpty(message = "The Password cannot be blank!")
    private final String password;

    private final String appUserRole;

}
