package com.teksystems.library;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class LibraryCard {
    @Id
    private String libraryCardId;
    @ManyToOne
    private Users users;
    private String county;
    private String password;
}
