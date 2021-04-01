package com.teksystems.library;

import lombok.Data;

import javax.persistence.*;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
@Entity

public class Users {
    @Id
    @NotBlank(message = "Username is mandatory")
    @Size(min = 4, max = 250, message = "Enter a username between 4 and 250 characters")
    private String username;

    @Size(min = 8, max = 250, message = "Enter an email address between 8 and 250 characters")
    @Email(message = "Enter a valid email address", regexp=".+@.+\\..+")
    private String email;

    @Size(min = 8, max = 56, message = "Enter a password between 8 and 56 characters")
    private String password;

    @OneToMany
    private List<LibraryCard> cards;
    @OneToMany
    @JoinTable(name = "user_books")
    private List<Book> books;

}
