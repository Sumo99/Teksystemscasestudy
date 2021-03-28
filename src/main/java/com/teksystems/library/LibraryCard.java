package com.teksystems.library;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Data
@Entity

public class LibraryCard {
    @Id
    private String libraryCardId;
    @ManyToOne
    private Users users;
    private String county;
}
