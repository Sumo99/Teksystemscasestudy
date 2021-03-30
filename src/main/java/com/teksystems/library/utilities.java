package com.teksystems.library;

import java.util.ArrayList;
import java.util.List;

public class utilities {
    /*
    For some reason generics don't work so that is why this whole mess is here
     */
    public static List<List<Book>> splitBooks(List<Book> allBooks){
        int rowSize = 4;
        List<List<Book>> booksByRows = new ArrayList<>();

        //this code splits the list into sublists of size for easier rendering of bootstrap.
        for(int i =0; i < allBooks.size(); i += rowSize){
            int end = Math.min(allBooks.size(), i + rowSize);
            booksByRows.add(allBooks.subList(i,end));
        }
        return booksByRows;
    }
    public static List<List<userWishlist>> splitUsers(List<userWishlist> allBooks){
        int rowSize = 4;
        List<List<userWishlist>> booksByRows = new ArrayList<>();

        //this code splits the list into sublists of size for easier rendering of bootstrap.
        for(int i =0; i < allBooks.size(); i += rowSize){
            int end = Math.min(allBooks.size(), i + rowSize);
            booksByRows.add(allBooks.subList(i,end));
        }
        return booksByRows;
    }

}
