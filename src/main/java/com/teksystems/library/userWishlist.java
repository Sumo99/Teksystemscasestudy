package com.teksystems.library;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Date;

@Data
@Entity
@Table(name = "userwishlist")
@AllArgsConstructor
@NoArgsConstructor

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
    public Integer page_num;
    public String publisher;
    public Date published_date;
    public Integer num_ratings;

    public String username;

    public userWishlist(Book book, String username){
        this.id = book.getId();
        this.title = book.getTitle();
        this.isbn = book.getIsbn();
        this.cover = book.getCover();
        this.description = book.getDescription();
        this.collection = book.getCollection();
        this.link = book.getLink();
        this.rating = book.getRating();
        this.page_num = book.getPage_num();
        this.publisher = book.getPublisher();
        this.published_date = book.getPublished_date();
        this.num_ratings = book.getNum_ratings();
        this.username = username;
    }
}

