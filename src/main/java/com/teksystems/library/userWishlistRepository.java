package com.teksystems.library;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface userWishlistRepository extends JpaRepository<userWishlist, Integer> {

    @Query("SELECT u from userWishlist u join Book b on b.title = u.title WHERE u.username = ?1")
    List<userWishlist> getAllBy(String username);
    List<userWishlist> findAllByUsername(String username);
    void deleteById(@NotNull Integer id);
}
