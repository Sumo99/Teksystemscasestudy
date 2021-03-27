package com.teksystems.library;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "amazon_comments")
public class Amazon {
    @Id
    public int comment_number;
    public int overall;
    public String asin;

}
