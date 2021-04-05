package com.teksystems.library;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class LibraryApplicationTests {

    @Test
    void contextLoads() {
    }
    @Test
    public void testSplitListBooks(){
        //given
        List<Book> bookSet= new ArrayList<>();

        bookSet.add(new Book("1984"));
        bookSet.add(new Book("A brave new world"));
        bookSet.add(new Book("Piano"));
        bookSet.add(new Book("Guinea pigs"));
        bookSet.add(new Book("Linux"));
        bookSet.add(new Book("Java for noobs"));
        bookSet.add(new Book("Hamster"));
        bookSet.add(new Book("Goat"));

        //when
        List<List<Book>> fromSplitBooks = utilities.splitBooks(bookSet);

        //then
        assertEquals(4, fromSplitBooks.get(0).size()); //we expect that the first list is 4 in size
        assertEquals("1984", fromSplitBooks.get(0).get(0).getTitle()); //we expect that the order is the same
    }
    @Test
    public void testSplitListUser(){
        //given
        List<userWishlist> bookSet= new ArrayList<>();

        bookSet.add(new userWishlist(new Book("Oracle security"), "Sumo99"));
        bookSet.add(new userWishlist(new Book("MySQL adminstration"), "Sumo99"));
        bookSet.add(new userWishlist(new Book("Creative writing"), "Sumo99"));
        bookSet.add(new userWishlist(new Book("Discord"), "Sumo99"));
        bookSet.add(new userWishlist(new Book("Guinea pigs"), "Sumo99"));
        bookSet.add(new userWishlist(new Book("Regular pigs"), "Sumo99"));
        bookSet.add(new userWishlist(new Book("Spring boot"), "Sumo99"));
        bookSet.add(new userWishlist(new Book("Python flask"), "Sumo99"));

        //when
        List<List<userWishlist>> fromSplitBooks = utilities.splitUsers(bookSet);

        //then
        assertEquals(4, fromSplitBooks.get(0).size()); //we expect that the first list is 4 in size
        assertEquals("MySQL adminstration", fromSplitBooks.get(0).get(1).getTitle()); //same as before
    }
    @Test
    public void testUrlEncode(){
        //given
        String urlToEncode = "This is a sample book description";

        //when and then combined
        assertEquals("This+is+a+sample+book+description", utilities.encodeValue(urlToEncode));
    }
}
