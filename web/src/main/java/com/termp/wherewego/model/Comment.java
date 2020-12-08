package com.termp.wherewego.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data // lombok 적용
@IdClass(CommentID.class)
public class Comment {
    @Id
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "loc_id")
    private Location location;

    @Basic(optional = false)
    @Column(insertable = false, updatable = false)
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE_TIME)
    private Date date;

    private String rating;
    private String content;


}
