package com.termp.wherewego.repository;


import com.termp.wherewego.model.Bookmark;
import com.termp.wherewego.model.BookmarkID;
import com.termp.wherewego.model.Comment;
import com.termp.wherewego.model.CommentID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkID> {
}
