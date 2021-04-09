package com.teksystems.library;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserWishlistRepository extends JpaRepository<UserWishlist, Integer> {

    @Query("SELECT u from UserWishlist u join Book b on b.title = u.title WHERE u.username = ?1")
    List<UserWishlist> getAllBy(String username);
    List<UserWishlist> findAllByUsername(String username);
    void deleteById(@NotNull Integer id);
}
