package com.termp.wherewego.repository;

import com.termp.wherewego.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, String> {
    @Query(value = "UPDATE `wherewego`.`user` SET `user_choice`= ?1 WHERE `user_id`= ?2",nativeQuery = true)
    void updateChoice(String choice, String id);
}
