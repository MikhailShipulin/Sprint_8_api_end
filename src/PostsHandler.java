import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostsHandler implements HttpHandler {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Gson gson = new Gson();
    public static final List<Post> posts = new ArrayList<>();

    public void addPostForList(Post post) {
        posts.add(post);
    }

    public List<Post> getPosts() {
        return posts;
    }

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
//                comment = gson.fromJson(str, Comment.class);
            Comment rawComment = gson.fromJson(str, Comment.class);
            if (rawComment.getText() == null || rawComment.getUser() == null) {
                throw new JsonParseException("Отсутствует обязательное поле user или text");
            }
            comment = new Comment(rawComment.getUser(), rawComment.getText());
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

    protected static Optional<Integer> getPostId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }


}