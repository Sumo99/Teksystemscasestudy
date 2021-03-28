package com.teksystems.library;

import lombok.Data;

import javax.naming.Name;
import javax.persistence.*;
import java.util.List;

@Data
@Entity

public class Users {
    @Id
    private String username;
    private String email;
    private String password;

    @OneToMany
    private List<LibraryCard> cards;
    @OneToMany
    @JoinTable(name = "user_books")
    private List<Book> books;

}
