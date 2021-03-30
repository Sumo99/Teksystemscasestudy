package com.teksystems.library;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findBookByDescriptionContains(String query);
    @Query("SELECT b from Book b join userWishlist u on b.title = u.title")
    List<Book> getAllBy();

}
