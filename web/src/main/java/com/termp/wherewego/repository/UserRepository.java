package com.termp.wherewego.repository;

import com.termp.wherewego.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
