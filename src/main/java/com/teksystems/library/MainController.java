package com.teksystems.library;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;


import javax.validation.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
public class MainController<T> {
    private static Validator validator;

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

    @RequestMapping(value = "/addbook", method = RequestMethod.POST)
    public String addBook(){
        return "";
    }

    @RequestMapping(value = "/wishlist", method = RequestMethod.POST)
    public String addBookToWishList(@RequestParam String book, Principal principal, Model model){
        Configuration configuration = Configuration.defaultConfiguration()
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
        RestTemplate restTemplate = new RestTemplate();
        String apiResult = restTemplate.getForObject("https://www.googleapis.com/books/v1/volumes?q="+book,String.class);
        List<Book> apiBooks = new ArrayList<>();

        List<Book> matchingBooks = bookRepository.getAllBy();
        List<userWishlist> allBooks = userWishlistRepository.findAllByUsername(principal.getName());
        List<List<Book>> splitBooks = utilities.splitBooks(matchingBooks);
        renderWishlist(model, matchingBooks, allBooks, splitBooks);

        int length = JsonPath.parse(apiResult).read("");
        for(int i = 0; i< length; i++){

            Book bookApiResponse = new Book();
            bookApiResponse.setTitle(JsonPath.using(configuration).parse(apiResult).read("$.items[" + String.valueOf(i) +"].volumeInfo.title"));
            bookApiResponse.setDescription(JsonPath.using(configuration).parse(apiResult).read("$.items[" + String.valueOf(i)+ "].volumeInfo.description"));
            bookApiResponse.setPublisher(JsonPath.using(configuration).parse(apiResult).read("$.items[" + String.valueOf(i) +"].volumeInfo.publisher"));
            bookApiResponse.setCover(JsonPath.using(configuration).parse(apiResult).read("$.items["+ String.valueOf(i) + "].volumeInfo.imageLinks.smallThumbnail"));
            bookApiResponse.setIsbn(JsonPath.using(configuration).parse(apiResult).read("$.items["+ String.valueOf(i) + "].volumeInfo.industryIdentifiers[0].identifier"));
            bookApiResponse.setCollection(JsonPath.using(configuration).parse(apiResult).read("$.items[" + String.valueOf(i) +"].volumeInfo.categories[0]"));
            bookApiResponse.setPage_num(JsonPath.using(configuration).parse(apiResult).read("$.items[" + String.valueOf(i) +"].volumeInfo.pageCount"));

            bookApiResponse.setRating(Float.valueOf(JsonPath.using(configuration).parse(apiResult).read("$.items[" + String.valueOf(i) +"].volumeInfo.averageRating") == null ? 0 :
                     JsonPath.using(configuration).parse(apiResult).read("$.items[" + String.valueOf(i) +"].volumeInfo.averageRating", Float.class)));
            bookApiResponse.setNum_ratings(JsonPath.using(configuration).parse(apiResult).read("$.items[" + String.valueOf(i) + "].volumeInfo.ratingsCount"));
            bookApiResponse.setLink(JsonPath.using(configuration).parse(apiResult).read("$.items["+ String.valueOf(i) + "].volumeInfo.previewLink"));

            System.out.println(bookApiResponse.toString());
            apiBooks.add(bookApiResponse);
        }

        System.out.println("The total length of the book list is "+apiBooks.size());
        System.out.println("The first book is " + apiBooks.get(0));
        Book book1 = apiBooks.get(0);

        if(principal == null){
            model.addAttribute("username", false);
        }
        model.addAttribute("username",true);
        model.addAttribute("book", utilities.splitBooks(apiBooks));
        System.out.println(book1.toString());
        System.out.println("The user we are saving for is "+principal.getName());

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
        renderWishlist(model, matchingBooks, allBooks, splitBooks);

        return "wishlist";
    }

    private void renderWishlist(Model model, List<Book> matchingBooks, List<userWishlist> allBooks, List<List<Book>> splitBooks) {
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
    public String addUser(@Valid Users users, BindingResult bindingResult, Model model){
        String errorMsg = "";
        if(bindingResult.hasErrors()){
            System.out.println("The user has entered invalid data!");
            for (Object object: bindingResult.getAllErrors()){
                if(object instanceof FieldError) {
                    FieldError fieldError = (FieldError) object;

                    errorMsg += fieldError.getDefaultMessage() +"\n";
                }

            }
            System.out.println("The user validation errors are "+errorMsg);
            model.addAttribute("error",errorMsg);
            return "register";
        }
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