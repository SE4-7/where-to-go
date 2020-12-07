package com.termp.wherewego.controller;

import com.termp.wherewego.model.Location;
import com.termp.wherewego.model.User;
import com.termp.wherewego.repository.LocationRepository;
import com.termp.wherewego.repository.UserRepository;

import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.Objects;

@Controller
public class LocationController {
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/location/form")
    public String hihi(){
        return "view";
    }

    private static PythonInterpreter intPre;

    @GetMapping("/location/view")
    public String hihi2(Model model, String id, String place_name, String category_name, String category_group_name, String phone,
                        String address_name, String road_address_name, String x, String y) throws IOException {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String idu = a.getName();
        User user = userRepository.findById(idu).orElse(null);
        Location location = locationRepository.findById(id).orElse(null);
        if(location==null){
            location = new Location();
            location.setId(id);
            location.setPlace_name(place_name);
            location.setCategory_name(category_name);
            location.setCategory_group_name(category_group_name);
            location.setPhone(phone);
            location.setAddress_name(address_name);
            location.setRoad_address_name(road_address_name);
            location.setX(x);
            location.setY(y);
            locationRepository.save(location);
        }
        model.addAttribute("user",user);
        String temp=null;
        for(int i = 0; i < Objects.requireNonNull(user).getComments().size(); i++){
            if (user.getComments().get(i).getLocation().getId().equals(location.getId())){
                temp = user.getComments().get(i).getRating();
            }
        }
        model.addAttribute("rating",temp);
        model.addAttribute("location",location);
        return "location/view";
    }

    @GetMapping("/test")
    public String test(Model model){
        String result = CallMain.mainfunc("1","2","3");
        model.addAttribute("result",result);
        return "/test";
    }
}
