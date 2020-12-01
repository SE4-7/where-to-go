package com.termp.wherewego.model;

import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data // lombok 적용
public class Location {
    @Id // PK 인것을 알려줌
    private String id;

    private String place_name;
    private String category_name;
    private String category_group_name;
    private String phone;
    private String address_name;
    private String road_address_name;
    private String x;
    private String y;

    @OneToMany(mappedBy = "location") // orphanRemoval=true 연관된 테이블의 데이터도 함께 삭제가능
    private List<Comment> comments = new ArrayList<>();

}
