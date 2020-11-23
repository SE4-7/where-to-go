package com.termp.wherewego.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Role {
    @Id // PK 인것을 알려줌
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment 설정
    private Long role_id;

    private String name;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;
}
