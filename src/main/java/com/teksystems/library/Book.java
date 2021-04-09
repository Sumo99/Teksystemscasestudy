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
/**
 * Represents a library book.
 */
public class Book {
    @Id
    private int id;

    private String title;
    private String isbn;
    private String cover;
    private String description;
    private String collection;
    private String link;
    private Float rating;
    private Integer page_num;
    private String publisher;
    private Date published_date;
    private Integer num_ratings;
    private String username;

    public Book(String title){
        this.title = title;
    }

    @ManyToOne
    public Users user;
}
