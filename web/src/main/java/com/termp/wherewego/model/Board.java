package com.termp.wherewego.model;

import com.sun.istack.NotNull;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity
@Data // lombok 적용
public class Board {
    @Id // PK 인것을 알려줌
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment 설정
    private long id;
    @NotNull
    @Size(min=2, max=30)
    private String title;
    private String content;
}
