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
    private final UserWishlistRepository userWishlistRepository;
    private final LibraryCardRepository libraryCardRepository;

    /**
     * Intialize the controller
     * @param br, the set of all books
     * @param amz, as a stretch goal, display all comments for a book
     * @param userRepository, all users
     * @param userWishlistRepository, all user books
     * @param libraryCardRepository, all library cards, as a stretch goal also allow the user to order their library cards
     */
    @Autowired
    MainController(BookRepository br, AmazonRepository amz, UserRepository userRepository,
                   UserWishlistRepository userWishlistRepository, LibraryCardRepository libraryCardRepository){
        this.userRepository = userRepository;
        this.bookRepository = br;
        this.amz = amz;
        this.userWishlistRepository = userWishlistRepository;
        this.libraryCardRepository = libraryCardRepository;
    }

    /**
     * Rest template for getting data from an api
     * @param builder
     * @return RestTemplate, access the data
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    /**
     * return all of the books in the library by rating, limit 100
     * @param principal
     * @param model
     * @return the index page
     */
    @RequestMapping(value = {"/", "/home"})
    public String index(Principal principal, Model model) {
        if(principal == null){
            model.addAttribute("username", false);
            return "index";
        }
        model.addAttribute("username",true);

        List<Book> allBooks = bookRepository.findAll(Sort.by(Sort.Direction.DESC, "rating"));
        List<Book> allBooksLimited = allBooks.subList(0, 100); //limit to 100 books to make page load faster
        List<List<Book>> booksByRow = Utilities.splitBooks(allBooksLimited);

        model.addAttribute("Books",booksByRow);
        model.addAttribute("fullString","C.GIF&client=hennp&type=xw12&oclc=");

        return "index";

    }

    /**
     * Alter the user as an update operation
     * @param principal
     * @param email
     * @param password
     * @return
     */
    @RequestMapping(value = "/alteruser", method = RequestMethod.POST)
    public RedirectView alterUser(Principal principal, @RequestParam String email, @RequestParam String password){
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        Users usertoEdit = userRepository.findByUsername(principal.getName());
        System.out.println("The password is " + password);

        if(email.equals("") && password.equals(""))
            return new RedirectView("settings");
        else if(!email.equals("")){
            usertoEdit.setEmail(email);
        }
        else if(!password.equals("")) {
            usertoEdit.setPassword(bCryptPasswordEncoder.encode(password));
        }

        userRepository.save(usertoEdit);
        return new RedirectView("settings");
    }

    /**
     * Add a book from the google books api
     * @param title
     * @param isbn
     * @param link
     * @param cover
     * @param description
     * @param collection
     * @param rating
     * @param num_ratings
     * @param page_num
     * @param publisher
     * @param principal
     * @return a redirect to the wishlist page
     */
    @RequestMapping(value = "/addbook", method = RequestMethod.POST)
    public RedirectView addBook(@RequestParam String title, @RequestParam String isbn, @RequestParam(required = false) String link,
                                @RequestParam String cover, @RequestParam String description, @RequestParam(required = false) String collection,
                                @RequestParam Float rating, @RequestParam(required = false) Integer num_ratings,
                                @RequestParam(required = false) Integer page_num, @RequestParam String publisher, Principal principal){
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
        userWishlistRepository.save(new UserWishlist(userBook, principal.getName()));
        return new RedirectView("wishlist");
    }

    /**
     * Add a library card with id,county, password. No verification is added
     * @param principal
     * @param id
     * @param county
     * @param password
     * @return
     */
    @RequestMapping(value = "/addcard", method = RequestMethod.POST)
    public RedirectView addCard(Principal principal, @RequestParam String id, @RequestParam String county, @RequestParam String password){
        Users requester = userRepository.findByUsername(principal.getName());
        libraryCardRepository.save(new LibraryCard(id,requester,password, county));
        return new RedirectView("settings");
    }

    /**
     * Add a book from the google api
     * @param book
     * @param principal
     * @param model
     * @return the wishlist page again
     */

    @RequestMapping(value = "/wishlist", method = RequestMethod.POST)
    public String addBookToWishList(@RequestParam String book, Principal principal, Model model){
        Configuration configuration = Configuration.defaultConfiguration()
                .addOptions(Option.SUPPRESS_EXCEPTIONS); //this is to avoid the code breaking if a property is not, ie if the schema changes
        RestTemplate restTemplate = new RestTemplate();
        String apiResult = restTemplate.getForObject("https://www.googleapis.com/books/v1/volumes?q="+book,String.class);
        List<Book> apiBooks = new ArrayList<>();

        List<UserWishlist> matchingBooks = userWishlistRepository.getAllBy(principal.getName());
        List<UserWishlist> allBooks = userWishlistRepository.findAllByUsername(principal.getName());
        List<UserWishlist> allUserMatch = userWishlistRepository.getAllBy(principal.getName());
        List<List<UserWishlist>> splitBooks = Utilities.splitUsers(allUserMatch);

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
        model.addAttribute("apiMatchingBooks", Utilities.splitBooks(results.get(Boolean.TRUE)));
        model.addAttribute("restBooks", Utilities.splitBooks(results.get(Boolean.FALSE)));

        model.addAttribute("username",true);

        return "wishlist_post";
    }

    /**
     * The wishlist page
     * @param principal
     * @param model
     * @return the wishlist page
     */

    @RequestMapping("/wishlist")
    public String wishlist(Principal principal, Model model){
        if(principal == null){
            model.addAttribute("username", false);
            return "wishlist";
        }

        List<UserWishlist> matchingBooks = userWishlistRepository.getAllBy(principal.getName());
        List<UserWishlist> allBooks = userWishlistRepository.findAllByUsername(principal.getName());
        List<UserWishlist> allUserMatch = userWishlistRepository.getAllBy(principal.getName());
        List<List<UserWishlist>> splitBooks = Utilities.splitUsers(allUserMatch);

        model.addAttribute("username",true);
        renderWishlist(model, matchingBooks, allBooks, splitBooks);

        return "wishlist";
    }

    /**
     * Private method to refactor out commonalities between the wishlist
     * @param model
     * @param matchingBooks
     * @param allBooks
     * @param splitBooks
     */
    private void renderWishlist(Model model, List<UserWishlist> matchingBooks, List<UserWishlist> allBooks, List<List<UserWishlist>> splitBooks) {
        model.addAttribute("book", splitBooks);
        model.addAttribute("fullString","C.GIF&client=hennp&type=xw12&oclc=");

        Map<Boolean, List<UserWishlist>> results = allBooks.stream().collect(Collectors.partitioningBy(matchingBooks::contains));
        List<List<UserWishlist>> splitWishlist = Utilities.splitUsers(results.get(false));
        model.addAttribute("matchingBooks", splitWishlist);
    }

    /**
     * Delete a book by id to support full crud operations
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete_book", method = RequestMethod.POST)
    public RedirectView deleteBook(@RequestParam Integer id){
        userWishlistRepository.deleteById(id);
        return new RedirectView("wishlist");
    }

    /**
     * Reccomend a book in the library that has a similar description
     * @param principal
     * @param model
     * @return
     */
    @RequestMapping("/Recomended")
    public String recomended(Principal principal, Model model){

        List<UserWishlist> books = userWishlistRepository.findAllByUsername(principal.getName());
        Map<Boolean, List<UserWishlist>> results = books.stream().collect(Collectors.partitioningBy(book1 ->  bookRepository.findBookByTitle(book1.getTitle()).size() > 0));
        books = results.get(Boolean.FALSE);

        RestTemplate restTemplate = new RestTemplate();
        List<Book> reccomendedBooks = new ArrayList<>();

        for (UserWishlist book : books){
            System.out.println("The loop has been invoked!");
            String url = "http://localhost:5000/book/"+ Utilities.encodeValue(book.getDescription() == null ? "" : book.getDescription());
            System.out.println(url);
            String apiResult = restTemplate.getForObject(url, String.class);
            System.out.println("The recommended book is " + apiResult);
            if(apiResult == null){
                continue; // this means that there is no description, hence we cannot reccomend anything
            }
            String[] books_strings = apiResult.split("<br>");
            for(String actualBook : books_strings){
                  reccomendedBooks.add(bookRepository.findBookByTitle(actualBook).get(0));
            }
        }

        List<List<Book>> booksToDisplay = Utilities.splitBooks(reccomendedBooks);
        model.addAttribute("Books",booksToDisplay);
        model.addAttribute("fullString","C.GIF&client=hennp&type=xw12&oclc=");

        if(principal == null){
            model.addAttribute("username", false);
        }
        model.addAttribute("username",true);
        return "recomended";
    }

    /**
     * Add a book that the user has checked out. This displays in the /books page
     * @param principal
     * @param title
     * @return
     */
    @RequestMapping(value = "/addbooksforuser", method = RequestMethod.POST)
    public RedirectView addBooksToUser(Principal principal, @RequestParam String title){
        Users users = userRepository.findByUsername(principal.getName());
        List<Book> usersBooks = users.getBooks();
        if(bookRepository.findBookByTitle(title).size() == 0){
            return new RedirectView("/books");
        }
        usersBooks.add(bookRepository.findBookByTitle(title).get(0));
        users.setBooks(usersBooks);
        userRepository.save(users);
        return new RedirectView("/books");
    }

    /**
     * The books page, all of the library books that a user has checked out. Not to be confused with the user wishlist
     * which can include books not in the library
     * @param principal
     * @param model
     * @return
     */

    @RequestMapping(value = {"/books","/current"})
    public String books(Principal principal,Model model){

        if(principal == null){
            model.addAttribute("username", false);
        }
        model.addAttribute("username",true);

        List<Book> allBooks = bookRepository.findBooksForUser(principal.getName());
        List<List<Book>> booksByRow = Utilities.splitBooks(allBooks);

        model.addAttribute("Books",booksByRow);
        model.addAttribute("fullString","C.GIF&client=hennp&type=xw12&oclc=");

        return "books";
    }

    /**
     * Log the user in
     * @return
     */
    @RequestMapping("/login")
    public String login(){
        return "login";
    }

    /**
     * Search the books by description containing a query
     * @param search_query
     * @param model
     * @param principal
     * @return
     */
    @RequestMapping("/search")
    public String search(@RequestParam String search_query, Model model, Principal principal){
        if(principal == null){
            model.addAttribute("username", false);
        }
        model.addAttribute("username",true);

        List<Book> matchingBooks = bookRepository.findBookByDescriptionContains(search_query);
        List<List<Book>> booksByRow = Utilities.splitBooks(matchingBooks);

        model.addAttribute("Search_query",search_query);
        model.addAttribute("Books", booksByRow);
        model.addAttribute("fullString","C.GIF&client=hennp&type=xw12&oclc=");

        return "books";
    }

    /**
     * Render the settings
     * @param model
     * @param principal
     * @return
     */
    @RequestMapping("/settings")
    public String settings(Model model, Principal principal){
        model.addAttribute("allCards",libraryCardRepository.findAllByusers_username(principal.getName()));

        return "setting";
    }

    /**
     * Show the registration page
     * @return
     */
    @RequestMapping("/register")
    public String register(){return "register";}

    /**
     * Add a user with a bcrypted password for security.
     * @param users
     * @param bindingResult
     * @param model
     * @return
     */
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
