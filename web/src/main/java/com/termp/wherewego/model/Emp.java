package com.termp.wherewego.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data // 롬복사용
@NoArgsConstructor // 생성자
@AllArgsConstructor // 모든 파라미터로 생성자
@Entity
public class Emp {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer empno;
    private String ename;
    private Integer sal;
}
