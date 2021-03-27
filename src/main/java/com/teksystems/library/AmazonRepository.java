package com.teksystems.library;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmazonRepository extends JpaRepository<Amazon, Integer> {

}
