package com.teksystems.library;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

@Data
@Entity
@Table(name = " mroonga_books" )

public class Book {
    @Id
    public int id;

    public String title;
    public String isbn;
    public String cover;
    public String description;
    public String collection;
    public String link;
    public Float rating;
    public int page_num;
    public String publisher;
    public Date published_date;
    public int num_ratings;


}
