package com.teksystems.library;

import lombok.Data;

import javax.persistence.*;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Data
@Entity

/**
 * The users class with validations requirments, expect for password which breaks it every time
 */
public class Users {
    @Id

    @NotBlank(message = "Username is mandatory")
    @Size(min = 4, max = 250, message = "Enter a username between 4 and 250 characters")
    @Pattern(regexp = "^[A-Za-z0-9-.@_]*$", message="No special characters for the username")

    private String username;

    @Size(min = 8, max = 250, message = "Enter an email address between 8 and 250 characters")
    @Email(message = "Enter a valid email address", regexp=".+@.+\\..+")
    @Pattern(regexp = "^[A-Za-z0-9-.@_]*$",message="No special characters for the email")

    private String email;
    //no need for password validations, since there is less to go wrong.
    private String password;

    @OneToMany
    private List<LibraryCard> cards;
    @OneToMany
    @JoinTable(name = "user_books")
    private List<Book> books;

}
