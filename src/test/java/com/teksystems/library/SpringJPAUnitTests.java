package com.teksystems.library;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class) //this sets up @Autowire.
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
public class SpringJPAUnitTests {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LibraryCardRepository libraryCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserWishlistRepository userWishlistRepository;

    @ParameterizedTest
    @ValueSource(strings = {"Testing 101", "Loki","Odin"})
    public void createBook(String bookTitle){
        Book book = new Book(bookTitle);
        bookRepository.save(book);
        assertEquals(bookRepository.findBookByTitle(bookTitle).size(), 1);
    }

    @Test
    public void readBook(){
        List<Book> book = bookRepository.findBookByTitle("Hidden Figures");
        assertEquals(book.get(0).getTitle(), "Hidden Figures");
    }

    @Test
    public void findBookByDescription(){
        List<Book> book = bookRepository.findBookByDescriptionContains("Computer");
        assertEquals(book.get(0).getTitle(), "Hidden Figures");
    }

    @Test
    public void getLibraryCards(){
        List<LibraryCard> allCards = libraryCardRepository.findAllByusers_username("test");
        assertEquals(allCards.get(0).getLibraryCardId(), "test");
    }

    @Test
    public void getUser(){
        Users allUsers = userRepository.findByUsername("sumo99");
        assertEquals(allUsers.getUsername(), "sumo99");
    }

    @Test
    public void getAllByUserName(){
        List<UserWishlist> allBooks = userWishlistRepository.findAllByUsername("test");
        assertEquals(allBooks.get(0).getTitle(), "Shelter in Place");
    }

    @Test
    public void testDelete(){
        userWishlistRepository.deleteById(108);
        assertEquals(userWishlistRepository.findAllByUsername("test").size() , 1);
    }

    @Test
    public void testGetAllBy(){
        List<UserWishlist> allbooks = userWishlistRepository.getAllBy("test");
        assertEquals(allbooks.get(0).getTitle(), "Shelter in Place");
        assertEquals(allbooks.size() , 1);
    }
}
