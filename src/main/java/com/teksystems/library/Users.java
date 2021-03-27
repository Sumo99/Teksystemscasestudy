package com.teksystems.library;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity

public class Users {
    @Id
    private String username;
    private String email;
    private String password;
}
