package com.teksystems.library;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class LibraryCard {
    @Id
    private String libraryCardId;

    private String county;
}
