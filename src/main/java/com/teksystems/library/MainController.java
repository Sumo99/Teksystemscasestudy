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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;


import javax.validation.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MainController {

    private final BookRepository bookRepository;
    private final AmazonRepository amz;
    private final UserRepository userRepository;
    private final userWishlistRepository userWishlistRepository;
    private final LibraryCardRepository libraryCardRepository;

    @Autowired
    MainController(BookRepository br, AmazonRepository amz, UserRepository userRepository,
                   userWishlistRepository userWishlistRepository, LibraryCardRepository libraryCardRepository){
        this.userRepository = userRepository;
        this.bookRepository = br;
        this.amz = amz;
        this.userWishlistRepository = userWishlistRepository;
        this.libraryCardRepository = libraryCardRepository;
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

        List<Book> allBooks = bookRepository.findAll(Sort.by(Sort.Direction.DESC, "rating"));
        List<List<Book>> booksByRow = utilities.splitBooks(allBooks);

        model.addAttribute("Books",booksByRow);
        model.addAttribute("fullString","C.GIF&client=hennp&type=xw12&oclc=");

        return "index";

    }

    @RequestMapping(value = "/alteruser", method = RequestMethod.POST)
    public RedirectView alterUser(Principal principal, @RequestParam String email, @RequestParam String password){
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        Users usertoEdit = userRepository.findByUsername(principal.getName());
        System.out.println("The password is " + password);

        if(email.equals("") && password.equals(""))
            return new RedirectView("settings");
        if(!email.equals("")){
            usertoEdit.setEmail(email);
        }
        if(!password.equals("")) {
            usertoEdit.setPassword(bCryptPasswordEncoder.encode(password));
        }

        userRepository.save(usertoEdit);
        return new RedirectView("settings");
    }

    @RequestMapping(value = "/addbook", method = RequestMethod.POST)
    public RedirectView addBook(@RequestParam String title, @RequestParam String isbn, @RequestParam(required = false) String link,
                                @RequestParam String cover, @RequestParam String description, @RequestParam(required = false) String collection,
                                @RequestParam Float rating, @RequestParam(required = false) Integer num_ratings,
                                @RequestParam(required = false) Integer page_num, @RequestParam String publisher, Principal principal){
        System.out.println("I have been invoked for the title " + title);
        Book userBook = new Book();
        userBook.setLink(link);
        userBook.setRating(rating);
        userBook.setCollection(collection);
        userBook.setNum_ratings(num_ratings);
        userBook.setDescription(description);
        userBook.setTitle(title);
        userBook.setIsbn(isbn);
        userBook.setPublisher(publisher);
        userBook.setPage_num(page_num);
        userBook.setCover(cover);
        System.out.println("The book that has been constructed is "+ userBook.toString());
        System.out.println("The username is ");
        System.out.println(principal.getName());
        userWishlistRepository.save(new userWishlist(userBook, principal.getName()));
        return new RedirectView("wishlist");
    }

    @RequestMapping(value = "/addcard", method = RequestMethod.POST)
    public RedirectView addCard(Principal principal, @RequestParam String id, @RequestParam String county, @RequestParam String password){
        Users requester = userRepository.findByUsername(principal.getName());
        libraryCardRepository.save(new LibraryCard(id,requester,password, county));
        return new RedirectView("settings");
    }

    @RequestMapping(value = "/wishlist", method = RequestMethod.POST)
    public String addBookToWishList(@RequestParam String book, Principal principal, Model model){
        Configuration configuration = Configuration.defaultConfiguration()
                .addOptions(Option.SUPPRESS_EXCEPTIONS);
        RestTemplate restTemplate = new RestTemplate();
        String apiResult = restTemplate.getForObject("https://www.googleapis.com/books/v1/volumes?q="+book,String.class);
        List<Book> apiBooks = new ArrayList<>();

        List<userWishlist> matchingBooks = userWishlistRepository.getAllBy(principal.getName());
        List<userWishlist> allBooks = userWishlistRepository.findAllByUsername(principal.getName());
        List<userWishlist> allUserMatch = userWishlistRepository.getAllBy(principal.getName());
        List<List<userWishlist>> splitBooks = utilities.splitUsers(allUserMatch);

        renderWishlist(model, matchingBooks, allBooks, splitBooks);

        int length = JsonPath.parse(apiResult).read("$.items.length()");
        for(int i = 0; i< length; i++){

            Book bookApiResponse = new Book();
            bookApiResponse.setTitle(JsonPath.using(configuration).parse(apiResult).read("$.items[" + i +"].volumeInfo.title"));
            bookApiResponse.setDescription(JsonPath.using(configuration).parse(apiResult).read("$.items[" + i + "].volumeInfo.description"));
            bookApiResponse.setPublisher(JsonPath.using(configuration).parse(apiResult).read("$.items[" + i +"].volumeInfo.publisher"));
            bookApiResponse.setCover(JsonPath.using(configuration).parse(apiResult).read("$.items["+ i + "].volumeInfo.imageLinks.smallThumbnail"));
            bookApiResponse.setIsbn(JsonPath.using(configuration).parse(apiResult).read("$.items["+ i + "].volumeInfo.industryIdentifiers[0].identifier"));
            bookApiResponse.setCollection(JsonPath.using(configuration).parse(apiResult).read("$.items[" + i +"].volumeInfo.categories[0]"));
            bookApiResponse.setPage_num(JsonPath.using(configuration).parse(apiResult).read("$.items[" + i +"].volumeInfo.pageCount"));
            bookApiResponse.setRating(JsonPath.using(configuration).parse(apiResult).read("$.items[" + i +"].volumeInfo.averageRating") == null ?  Float.valueOf(0) :
                    JsonPath.using(configuration).parse(apiResult).read("$.items[" + i +"].volumeInfo.averageRating", Float.class));
            bookApiResponse.setNum_ratings(JsonPath.using(configuration).parse(apiResult).read("$.items[" + i + "].volumeInfo.ratingsCount"));
            bookApiResponse.setLink(JsonPath.using(configuration).parse(apiResult).read("$.items["+ i + "].volumeInfo.previewLink"));

            apiBooks.add(bookApiResponse);
        }

        Map<Boolean, List<Book>> results = apiBooks.stream().collect(Collectors.partitioningBy(book1 ->  bookRepository.findBookByTitle(book1.getTitle()).size() > 0));
        System.out.println("The list of matching books is "+results.get(Boolean.TRUE));
        model.addAttribute("apiMatchingBooks",utilities.splitBooks(results.get(Boolean.TRUE)));
        model.addAttribute("restBooks",utilities.splitBooks(results.get(Boolean.FALSE)));

        model.addAttribute("username",true);

        return "wishlist_post";
    }

    @RequestMapping("/wishlist")
    public String wishlist(Principal principal, Model model){
        if(principal == null){
            model.addAttribute("username", false);
            return "wishlist";
        }

        List<userWishlist> matchingBooks = userWishlistRepository.getAllBy(principal.getName());
        List<userWishlist> allBooks = userWishlistRepository.findAllByUsername(principal.getName());
        List<userWishlist> allUserMatch = userWishlistRepository.getAllBy(principal.getName());
        List<List<userWishlist>> splitBooks = utilities.splitUsers(allUserMatch);

        model.addAttribute("username",true);
        renderWishlist(model, matchingBooks, allBooks, splitBooks);

        return "wishlist";
    }

    private void renderWishlist(Model model, List<userWishlist> matchingBooks, List<userWishlist> allBooks, List<List<userWishlist>> splitBooks) {
        model.addAttribute("book", splitBooks);
        model.addAttribute("fullString","C.GIF&client=hennp&type=xw12&oclc=");

        Map<Boolean, List<userWishlist>> results = allBooks.stream().collect(Collectors.partitioningBy(matchingBooks::contains));
        List<List<userWishlist>> splitWishlist = utilities.splitUsers(results.get(false));
        model.addAttribute("matchingBooks", splitWishlist);
    }

    @RequestMapping(value = "/delete_book", method = RequestMethod.POST)
    public RedirectView deleteBook(@RequestParam Integer id){
        userWishlistRepository.deleteById(id);
        return new RedirectView("wishlist");
    }

    @RequestMapping("/Recomended")
    public String recomended(Principal principal, Model model){
       List<userWishlist> books = userWishlistRepository.findAllByUsername(principal.getName());
        RestTemplate restTemplate = new RestTemplate();

        for (userWishlist book : books){
            String url = "http://localhost:5000/book/"+utilities.encodeValue(book.getDescription() == null ? "" : book.getDescription());
            System.out.println(url);
           String apiResult = restTemplate.getForObject(url, String.class);
           System.out.println(apiResult);
        }
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
    public String login(){
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
    public String settings(Model model){
        System.out.println("There are this many library cards " + libraryCardRepository.findAll().size());
        model.addAttribute("allCards",libraryCardRepository.findAll());

        return "setting";
    }

    @RequestMapping("/register")
    public String register(){return "register";}

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String addUser(@Valid Users users, BindingResult bindingResult, Model model){
        StringBuilder errorMsg = new StringBuilder();
        if(bindingResult.hasErrors()){
            System.out.println("The user has entered invalid data!");
            for (Object object: bindingResult.getAllErrors()){
                if(object instanceof FieldError) {
                    FieldError fieldError = (FieldError) object;

                    errorMsg.append(fieldError.getDefaultMessage()).append("\n");
                }

            }
            System.out.println("The user validation errors are "+errorMsg);
            model.addAttribute("error", errorMsg.toString());
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
