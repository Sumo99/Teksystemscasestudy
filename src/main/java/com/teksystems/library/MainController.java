package com.teksystems.library;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class MainController {

    private BookRepository bookRepository;
    private AmazonRepository amz;
    private UserRepository userRepository;

    @Autowired
    MainController(BookRepository br, AmazonRepository amz, UserRepository userRepository){
        this.userRepository = userRepository;
        this.bookRepository = br;
        this.amz = amz;
    }

    @RequestMapping(value = {"/", "/home"})
    public String index(Principal principal,Model model) {
        if(principal == null){
            model.addAttribute("username", false);
            return "index";
        }
        model.addAttribute("username",true);
        return "index";
    }

    @RequestMapping("/wishlist")
    public String wishlist(Principal principal, Model model){
        if(principal == null){
            model.addAttribute("username", false);
        }
        model.addAttribute("username",true);
        return "wishlist";
    }

    @RequestMapping("/Recomended")
    public String recomended(Principal principal, Model model){
        if(principal == null){
            model.addAttribute("username", false);
        }
        model.addAttribute("username",true);
        return "recomended";
    }
    @RequestMapping(value = {"/books","current"})
    public String books(Principal principal,Model model){
        if(principal == null){
            model.addAttribute("username", false);
        }
        model.addAttribute("username",true);

        List<Book> allBooks = bookRepository.findAll(Sort.by(Sort.Direction.DESC, "rating"));
        model.addAttribute("Books",allBooks);
        model.addAttribute("fullString","C.GIF&client=hennp&type=xw12&oclc=");
        return "books";
    }

    @RequestMapping("/login")
    public String login(String error,Model model){
        return "login";
    }

    @RequestMapping("/search")
    public String search(@RequestParam String search_query, Model model, Principal principal){
        if(principal == null){
            model.addAttribute("username", false);
        }
        model.addAttribute("username",true);

        List<Book> matchingBooks = bookRepository.findBookByDescriptionContains(search_query);
        model.addAttribute("Books", matchingBooks);
        model.addAttribute("fullString","C.GIF&client=hennp&type=xw12&oclc=");
        return "books";
    }

    @RequestMapping("/settings")
    public String settings(){
        return "setting";
    }

    @RequestMapping("/register")
    public String register(){return "register";}

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String addUser(Users users, Model model){
        if(userRepository.findByUsername(users.getUsername()) != null){
            String usernameError = String.format("The username %s already exists, please try another one", users.getUsername());
            model.addAttribute("error", usernameError);
            return "register";
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));
        userRepository.save(users);
        model.addAttribute("success", "The user was successfully saved to the database");
        return "register";
    }

}