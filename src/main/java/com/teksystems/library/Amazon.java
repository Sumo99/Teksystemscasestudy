package com.teksystems.library;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This class is the amazon comments class, stretch goal
 */
@Data
@Entity
@Table(name = "amazon_comments")
public class Amazon {
    @Id
    private int comment_number;
    private int overall;
    private String asin;

}
