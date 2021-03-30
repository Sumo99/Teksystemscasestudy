package com.teksystems.library;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface userWishlistRepository extends JpaRepository<userWishlist, Integer> {

    List<userWishlist> findAllByUsername(String username);

}
