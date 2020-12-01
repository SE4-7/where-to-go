package com.termp.wherewego.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data // lombok 적용
@IdClass(CommentID.class)
public class Comment {
    @Id
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "loc_id")
    private Location location;

    private String rating;
    private String content;


}
