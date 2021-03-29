package com.teksystems.library;

import com.github.wnameless.json.flattener.JsonFlattener;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
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
    @RequestMapping(value = "/wishlist", method = RequestMethod.POST)
    public String addBookToWishList(){
        return "wishlist";
    }

    @RequestMapping("/wishlist")
    public String wishlist(Principal principal, Model model){

        //        RestTemplate restTemplate = new RestTemplate();
//        String book = restTemplate.getForObject("https://www.googleapis.com/books/v1/volumes?q=1984&maxResults=1",String.class);
//
//        Map<String, Object> map = JsonFlattener.flattenAsMap(book);
//
//        Book book1 = new Book();
//
//        book1.setIsbn((String) map.get("items[0].volumeInfo.industryIdentifiers[1].identifier"));
//        book1.setCover((String) map.get("items[0].volumeInfo.imageLinks.thumbnail"));
//        book1.setDescription((String) map.get("items[0].volumeInfo.description"));
//        book1.setCollection((String) map.get("items[0].volumeInfo.categories[0]"));
//        book1.setTitle((String) map.get("items[0].volumeInfo.title"));
//        book1.setLink((String) map.get("items[0].accessInfo.webReaderLink"));
//        book1.setPage_num((Integer) map.get("items[0].volumeInfo.pageCount"));
//        book1.setRating( Float.valueOf(map.get("items[0].volumeInfo.averageRating").toString()));
//        book1.setNum_ratings((Integer) map.get("items[0].volumeInfo.ratingsCount"));
//        book1.setPublisher((String) map.get("items[0].volumeInfo.publisher"));
//        book1.setPublished_date( new Date(Integer.valueOf((String)map.get("items[0].volumeInfo.publishedDate"))) );
//
//        System.out.println(book1);
//        if(principal == null){
//            model.addAttribute("username", false);
//        }
//
//        model.addAttribute("username",true);
//        model.addAttribute("book", book1);

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
        List<List<Book>> booksByRow = splitBooks(allBooks);
        model.addAttribute("Books",booksByRow);
        model.addAttribute("fullString","C.GIF&client=hennp&type=xw12&oclc=");
        return "books";
    }



    private List<List<Book>> splitBooks(List<Book> allBooks){
        int rowSize = 4;
        List<List<Book>> booksByRows = new ArrayList<>();

        //this code splits the list into sublists of size for easier rendering of bootstrap.
        for(int i =0; i < allBooks.size(); i += rowSize){
            int end = Math.min(allBooks.size(), i + rowSize);
            booksByRows.add(allBooks.subList(i,end));
        }
        return booksByRows;
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
        List<List<Book>> booksByRow = splitBooks(matchingBooks);

        model.addAttribute("Search_query",search_query);
        model.addAttribute("Books", booksByRow);
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