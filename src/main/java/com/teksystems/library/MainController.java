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
import java.util.stream.Collectors;

@Controller
public class MainController<T> {

    private BookRepository bookRepository;
    private AmazonRepository amz;
    private UserRepository userRepository;
    private userWishlistRepository userWishlistRepository;

    @Autowired
    MainController(BookRepository br, AmazonRepository amz, UserRepository userRepository,
                   userWishlistRepository userWishlistRepository){
        this.userRepository = userRepository;
        this.bookRepository = br;
        this.amz = amz;
        this.userWishlistRepository = userWishlistRepository;
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
    public String addBookToWishList(@RequestParam String book, Principal principal, Model model){

        System.out.println("The user supplied book is "+ book);
        RestTemplate restTemplate = new RestTemplate();
        String apiResult = restTemplate.getForObject("https://www.googleapis.com/books/v1/volumes?q="+book+"&maxResults=1",String.class);
        System.out.println("The output string is "+apiResult);

        Map<String, Object> map = JsonFlattener.flattenAsMap(apiResult);

        Book book1 = new Book();

        book1.setIsbn((String) map.get("items[0].volumeInfo.industryIdentifiers[1].identifier"));
        book1.setCover((String) map.getOrDefault("items[0].volumeInfo.imageLinks.thumbnail",""));
        book1.setDescription((String) map.getOrDefault("items[0].volumeInfo.description",""));
        book1.setCollection((String) map.getOrDefault("items[0].volumeInfo.categories[0]",""));
        book1.setTitle((String) map.get("items[0].volumeInfo.title"));
        book1.setLink((String) map.getOrDefault("items[0].accessInfo.webReaderLink", ""));
        book1.setPage_num((Integer) map.getOrDefault("items[0].volumeInfo.pageCount",0));
        book1.setRating( Float.valueOf(map.getOrDefault("items[0].volumeInfo.averageRating",0).toString()));
        book1.setNum_ratings((Integer) map.getOrDefault("items[0].volumeInfo.ratingsCount",0));
        book1.setPublisher((String) map.getOrDefault("items[0].volumeInfo.publisher","None"));

        System.out.println(book1);
        if(principal == null){
            model.addAttribute("username", false);
        }
        model.addAttribute("username",true);
        model.addAttribute("book", book1);
        userWishlist wish = new userWishlist(book1, principal.getName());
        userWishlistRepository.save(wish);
        return "wishlist";
    }

    @RequestMapping("/wishlist")
    public String wishlist(Principal principal, Model model){
        if(principal == null){
            model.addAttribute("username", false);
        }
        List<Book> matchingBooks = bookRepository.getAllBy();
        List<userWishlist> allBooks = userWishlistRepository.findAllByUsername(principal.getName());

        List<List<Book>> splitBooks = utilities.splitBooks(matchingBooks);
        model.addAttribute("username",true);
        model.addAttribute("book", splitBooks);
        model.addAttribute("fullString","C.GIF&client=hennp&type=xw12&oclc=");

        for(Book book : matchingBooks){
            List<userWishlist> titleMatch =
                    allBooks.stream()
                    .filter(a -> a.getTitle().equals(book.getTitle()))
                    .collect(Collectors.toList());
            allBooks.removeAll(titleMatch);
        }
        List<List<userWishlist>> splitWishlist = utilities.splitUsers(allBooks);
        model.addAttribute("matchingBooks", splitWishlist);

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
        List<List<Book>> booksByRow = utilities.splitBooks(allBooks);

        model.addAttribute("Books",booksByRow);
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
        List<List<Book>> booksByRow = utilities.splitBooks(matchingBooks);

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