package com.termp.wherewego.model;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;


@Entity
@Data
public class User {
    @Id // PK 인것을 알려줌
    private String user_id;

    private String user_pw;
    private String user_name;
    private String user_gender;


    private String user_birth;
    private String user_choice;
    private Boolean enabled;

    @ManyToMany()
    @JoinTable(
            name = "authorities",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user") // orphanRemoval=true 연관된 테이블의 데이터도 함께 삭제가능
    private List<Comment> comments = new ArrayList<>();

}
