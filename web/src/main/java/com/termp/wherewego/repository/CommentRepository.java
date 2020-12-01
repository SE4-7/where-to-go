package com.termp.wherewego.repository;


import com.termp.wherewego.model.Comment;
import com.termp.wherewego.model.CommentID;
import com.termp.wherewego.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, CommentID> {
}
