package com.termp.wherewego.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentID implements Serializable {
    private String user;
    private String location;
}