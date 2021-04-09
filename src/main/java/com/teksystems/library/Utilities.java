package com.teksystems.library;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.List;

public class Utilities {
    /*
    For some reason generics don't work so that is why this whole mess is here
     */

    /**
     * Partition a list into sublists of length four. Four displays best, so it was chosen
     * @param allBooks
     * @return the split list
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

    /**
     * Same as the aboce, but for user wishlist
     * @param allBooks
     * @return
     */
    public static List<List<UserWishlist>> splitUsers(List<UserWishlist> allBooks){
        int rowSize = 4;
        List<List<UserWishlist>> booksByRows = new ArrayList<>();

        //this code splits the list into sublists of size for easier rendering of bootstrap.
        for(int i =0; i < allBooks.size(); i += rowSize){
            int end = Math.min(allBooks.size(), i + rowSize);
            booksByRows.add(allBooks.subList(i,end));
        }
        return booksByRows;
    }

    /**
     * Encode a string to url
     * @param value
     * @return
     */
    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

}
