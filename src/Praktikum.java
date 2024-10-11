import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private Users user;
    private String text;
    private String date;

    public Comment(Users user, String text) {
        this.user = user;
        this.text = text;
        this.date = String.valueOf(LocalDateTime.now());
    }

    public Users getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

//    public String getDate() {return date; }
}

class Users {
    private String name;
    private int age;
    private int rating;


    public Users(String name, int age, int rating) {
        this.name = name;
        this.age = age;
        this.rating = rating;
    }

    public String getName() {return name;}

    public int getRating() {return rating;}

    public int getAge() {return age;}
}

public class Praktikum {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Gson gson = new Gson();
    private static final List<Post> posts = new ArrayList<>();



    static {
        Post post1 = new Post(1, "Это первый пост, который я здесь написал.");
        post1.addComment(new Comment(new Users("Петр Первый", 25, 10), "Я успел откомментировать первым!"));
        posts.add(post1);

        Post post2 = new Post(22, "Это будет второй пост. Тоже короткий.");
        post2.addComment(new Comment(new Users("Иван Иванов", 35, 5), "Я успел откомментировать вторым!"));
        posts.add(post2);

        Post post3 = new Post(333, "Это пока последний пост.");
        posts.add(post3);
    }


    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create();

        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/posts", new PostsHandler());
        httpServer.start(); // запускаем сервер

        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    static class PostsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

            switch (endpoint) {
                case GET_POSTS: {
                    handleGetPosts(exchange);
                    break;
                }
                case GET_COMMENTS: {
                    handleGetComments(exchange);
                    break;
                }
                case POST_COMMENT: {
                    handlePostComments(exchange);
                    break;
                }
                default:
                    writeResponse(exchange, "Такого эндпоинта не существует", 404);
            }
        }

        private void handlePostComments(HttpExchange exchange) throws IOException {
            // реализуйте обработку добавления комментария
            Optional<Integer> postIdOpt = getPostId(exchange);
            // извлеките идентификатор поста и обработайте исключительные ситуации
            if (postIdOpt.isEmpty()) {
                writeResponse(exchange,"Некорректный идентификатор поста", 400);
                return;
            }

            /* Получите тело запроса в виде текста в формате JSON и преобразуйте его в объект Comment.
            Учтите, что может быть передан некоректный JSON — эту ситуацию нужно обработать.
            Подумайте, какие ещё ситуации требуют обработки. */

            int postId = postIdOpt.get();
            Comment comment = null;
            try {
                String str = new String(exchange.getRequestBody().readAllBytes(), Charset.defaultCharset());
                comment = gson.fromJson(str, Comment.class);
            } catch (JsonParseException e) {
                writeResponse(exchange, "Получен некорректный JSON", 400);
            }

            // найдите пост с указанным идентификатором и добавьте в него комментарий
            for (Post post: posts) {
                if (post.getId() == postId) {
//                    String commentsJson = gson.toJson(post.getCommentaries());
                    post.addComment(comment);
                    writeResponse(exchange, "Комментарий добавлен", 201);
                    return;
                }
            }
            writeResponse(exchange, "Пост с идентификатором " + postId + " не найден", 404);
        }

        private Endpoint getEndpoint(String requestPath, String requestMethod) {
            String[] pathParts = requestPath.split("/");

            if (pathParts.length == 2 && pathParts[1].equals("posts")) {
                return Endpoint.GET_POSTS;
            }
            if (pathParts.length == 4 && pathParts[1].equals("posts") && pathParts[3].equals("comments")) {
                if (requestMethod.equals("GET")) {
                    return Endpoint.GET_COMMENTS;
                }
                if (requestMethod.equals("POST")) {
                    return Endpoint.POST_COMMENT;
                }
            }
            return Endpoint.UNKNOWN;
        }

        private void handleGetPosts(HttpExchange exchange) throws IOException {
            writeResponse(exchange, gson.toJson(posts), 200);
        }

        private void handleGetComments(HttpExchange exchange) throws IOException {
            Optional<Integer> postIdOpt = getPostId(exchange);
            if(postIdOpt.isEmpty()) {
                writeResponse(exchange, "Некорректный идентификатор поста", 400);
                return;
            }
            int postId = postIdOpt.get();

            for (Post post : posts) {
                if (post.getId() == postId) {
                    String commentsJson = gson.toJson(post.getCommentaries());
                    writeResponse(exchange, commentsJson, 200);
                    return;
                }
            }

            writeResponse(exchange, "Пост с идентификатором " + postId + " не найден", 404);
        }

        private Optional<Integer> getPostId(HttpExchange exchange) {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            try {
                return Optional.of(Integer.parseInt(pathParts[2]));
            } catch (NumberFormatException exception) {
                return Optional.empty();
            }
        }

        private void writeResponse(HttpExchange exchange,
                                   String responseString,
                                   int responseCode) throws IOException {
            if(responseString.isBlank()) {
                exchange.sendResponseHeaders(responseCode, 0);
            } else {
                byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
                exchange.sendResponseHeaders(responseCode, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
            exchange.close();
        }

        enum Endpoint {GET_POSTS, GET_COMMENTS, POST_COMMENT, UNKNOWN}
    }
}