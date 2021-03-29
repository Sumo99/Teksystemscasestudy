package com.teksystems.library;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;

@Data
@Entity

public class userWishlist {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private int id;
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

    private String username;

}
