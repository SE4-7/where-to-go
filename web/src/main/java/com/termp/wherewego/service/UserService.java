package com.termp.wherewego.service;


import com.termp.wherewego.model.Role;
import com.termp.wherewego.model.User;
import com.termp.wherewego.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User save(User user) {
        String pw = passwordEncoder.encode(user.getUser_pw());
        user.setUser_pw(pw);
        user.setEnabled(true);
        Role role = new Role();
        role.setRole_id(1l);
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public void update(User user) {
        String id = user.getUser_id();
        User u = userRepository.findById(id).orElse(null);
        if (u != null){
            u.setUser_name(user.getUser_name());
            user.setEnabled(true);
            String pw = passwordEncoder.encode(user.getUser_pw());
            u.setUser_pw(pw);
        }
        userRepository.save(u);
    }
}
