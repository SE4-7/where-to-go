package com.termp.wherewego.controller;

import com.termp.wherewego.model.Location;
import com.termp.wherewego.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

@Controller
public class LocationController {
    @Autowired
    private LocationRepository locationRepository;

    @PostMapping("/location/form")
    public String hihi(){
        return "view";
    }

    @GetMapping("/location/view")
    public String hihi2(Model model, String id, String place_name, String category_name, String category_group_name, String phone,
                        String address_name, String road_address_name, String x, String y) throws IOException {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String idu = a.getName();
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
        model.addAttribute("userid",idu);
        model.addAttribute("location",location);
        return "location/view";
    }
}
