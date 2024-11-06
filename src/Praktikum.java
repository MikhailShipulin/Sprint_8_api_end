import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class Post {
    private int id;
    private String text;
    private List<Comment> commentaries = new ArrayList<>();

    private Post() {}

    public Post(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public void addComment(Comment comment) {
        commentaries.add(comment);
    }

    public List<Comment> getCommentaries() {
        return commentaries;
    }

    public int getId() {
        return id;
    }
}

class Comment {

    private User user;
    private String text;
    private String date;

    public Comment(User user, String text) {
        this.user = user;
        this.text = text;
        this.date = String.valueOf(LocalDateTime.now());
    }

    public Comment(User user) {
    }

    public User getUser() {
        return new ManagerUser().getUser(2);
    }

    public String getText() {
        return text;
    }

//    public String getDate() {return date; }
}

class User {
    private Integer id;
    private String name;
    private int age;
    private int rating;

    public User(String name, int age, int rating) {
        this.name = name;
        this.age = age;
        this.rating = rating;
    }

    public String getName() {return name;}

    public int getRating() {return rating;}

    public int getAge() {return age;}

    public int getId() {
        return id;
    }
    public void setId(Integer id) {this.id = id;}

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", rating=" + rating +
                '}';
    }
}

public class Praktikum {
    private static final int PORT = 8080;


    static {

        ManagerUser manager = new ManagerUser();
        PostsHandler postsHandler = new PostsHandler();

        User user1 = new User("Николай Попов", 30, 4);
        manager.createNewUser(user1);
        User user2 = new User("Владимир Волков", 20, 7);
        manager.createNewUser(user2);
        User user3 = new User("Иван Жаров", 15, 3);
        manager.createNewUser(user3);

        Post post1 = new Post(1, "Это первый пост, который я здесь написал.");
        postsHandler.addPostForList(post1);
        post1.addComment(new Comment(user1, "Я успел откомментировать первым!"));

        Post post2 = new Post(22, "Это будет второй пост. Тоже короткий.");
        post2.addComment(new Comment(user2, "Я успел откомменuтировать вторым!"));
        postsHandler.addPostForList(post2);

        Post post3 = new Post(333, "Это пока последний пост.");
        postsHandler.addPostForList(post3);

    }


    public static void main(String[] args) throws IOException {

        HttpServer httpServer = HttpServer.create();

        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/posts", new PostsHandler());
        httpServer.createContext("/users", new UsersHandler());
        httpServer.start(); // запускаем сервер

        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");

    }
}
