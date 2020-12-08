package com.termp.wherewego.controller;

import com.termp.wherewego.model.Bookmark;
import com.termp.wherewego.model.BookmarkID;
import com.termp.wherewego.model.Location;
import com.termp.wherewego.model.User;
import com.termp.wherewego.repository.BookmarkRepository;
import com.termp.wherewego.repository.LocationRepository;
import com.termp.wherewego.repository.UserRepository;

import com.termp.wherewego.service.UserService;
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
    @Autowired
    private UserService userService;
    @Autowired
    private BookmarkRepository bookmarkRepository;

    @PostMapping("/location/form")
    public String hihi(){
        return "view";
    }

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
        BookmarkID bookmarkID = new BookmarkID();
        bookmarkID.setLocation(location.getId());
        bookmarkID.setUser(user.getUser_id());
        Bookmark bookmark = bookmarkRepository.findById(bookmarkID).orElse(null);
        if (bookmark != null){
            model.addAttribute("bookmark","true");
        }else {
            model.addAttribute("bookmark","false");
        }
        return "location/view";
    }

    @GetMapping("/view2")
    public String view(Model model, String id){
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String idu = a.getName();
        User user = userRepository.findById(idu).orElse(null);
        Location location = locationRepository.findById(id).orElse(null);
        model.addAttribute("user",user);
        String temp=null;
        for(int i = 0; i < Objects.requireNonNull(user).getComments().size(); i++){
            assert location != null;
            if (user.getComments().get(i).getLocation().getId().equals(location.getId())){
                temp = user.getComments().get(i).getRating();
            }
        }
        model.addAttribute("rating",temp);
        model.addAttribute("location",location);
        BookmarkID bookmarkID = new BookmarkID();
        assert location != null;
        bookmarkID.setLocation(location.getId());
        bookmarkID.setUser(user.getUser_id());
        Bookmark bookmark = bookmarkRepository.findById(bookmarkID).orElse(null);
        if (bookmark != null){
            model.addAttribute("bookmark","true");
        }else {
            model.addAttribute("bookmark","false");
        }
        return "location/view";
    }

    @GetMapping("/mypage")
    public String test(Model model){
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String idu = a.getName();
        User user = userRepository.findById(idu).orElse(null);
        model.addAttribute("user",user);
        return "/mypage";
    }

    @PostMapping("/mypage")
    public String userupdate(User user){
        String id = user.getUser_id();
        userService.update(user);
        return "redirect:/mypage";
    }

    @PostMapping("/mypage2")
    public String userupdate2(User user){
        userRepository.updateChoice(user.getUser_choice(),user.getUser_id());
        return "redirect:/mypage";
    }

    @GetMapping("/myloc")
    public String myloc(Model model){
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String idu = a.getName();
        User user = userRepository.findById(idu).orElse(null);
        model.addAttribute("user",user);
        return "/myloc";
    }

    @GetMapping("/mybmark")
    public String myBmark(Model model){
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String idu = a.getName();
        User user = userRepository.findById(idu).orElse(null);
        model.addAttribute("user",user);
        return "/mybmark";
    }

    @GetMapping("/bookmark")
    public String bookMark(Model model, String id){
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String idu = a.getName();

        User user = userRepository.findById(idu).orElse(null);
        Location location = locationRepository.findById(id).orElse(null);
        Bookmark bookmark = new Bookmark();
        bookmark.setLocation(location);
        bookmark.setUser(user);
        bookmarkRepository.save(bookmark);
        String temp=null;
        for(int i = 0; i < Objects.requireNonNull(user).getComments().size(); i++){
            assert location != null;
            if (user.getComments().get(i).getLocation().getId().equals(location.getId())){
                temp = user.getComments().get(i).getRating();
            }
        }
        model.addAttribute("user",user);
        model.addAttribute("rating",temp);
        model.addAttribute("location",location);
        model.addAttribute("bookmark","true");
        return "location/view";
    }

    @GetMapping("/bookmarkdelete")
    public String bookMarkDel(Model model, String id){
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String idu = a.getName();

        User user = userRepository.findById(idu).orElse(null);
        Location location = locationRepository.findById(id).orElse(null);
        BookmarkID bookmarkID = new BookmarkID();
        assert location != null;
        bookmarkID.setLocation(location.getId());
        bookmarkID.setUser(user.getUser_id());
        bookmarkRepository.deleteById(bookmarkID);
        String temp=null;
        for(int i = 0; i < Objects.requireNonNull(user).getComments().size(); i++){
            assert location != null;
            if (user.getComments().get(i).getLocation().getId().equals(location.getId())){
                temp = user.getComments().get(i).getRating();
            }
        }
        model.addAttribute("user",user);
        model.addAttribute("rating",temp);
        model.addAttribute("location",location);
        model.addAttribute("bookmark","false");
        return "location/view";
    }
}
