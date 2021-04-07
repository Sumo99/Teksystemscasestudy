package com.teksystems.library;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NamedNativeQueries;
import org.hibernate.annotations.NamedNativeQuery;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Date;

@Data
@Entity
@Table(name = "mroonga_books" )
@NoArgsConstructor
@NamedNativeQueries({
        @NamedNativeQuery(name = "Book.findBooksForUser", query = "select * from user_books u JOIN mroonga_books m on u.books_id = m.id where users_username = ?1", resultClass = Book.class)
})

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
    public Integer page_num;
    public String publisher;
    public Date published_date;
    public Integer num_ratings;
    public String username;

    public Book(String title){
        this.title = title;
    }

    @ManyToOne
    public Users user;
}
