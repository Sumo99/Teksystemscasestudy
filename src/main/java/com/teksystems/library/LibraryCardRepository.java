package com.teksystems.library;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryCardRepository extends JpaRepository<LibraryCard, String> {
    /**
     * Find all library cards for a given username
     * @param users
     * @return
     */
    List<LibraryCard> findAllByusers_username (String users);
}
