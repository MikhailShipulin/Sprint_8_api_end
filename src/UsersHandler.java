import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class UsersHandler implements HttpHandler {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Gson gson = new Gson();

    ManagerUser managerUser = new ManagerUser();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

            switch (endpoint){
                case GET_USERS: {
                    handleGetUser(exchange);
                    break;
                }
                case POST_USERS: {
                    handlePostUser(exchange);
                    break;
                }
                default:
                    writeResponse(exchange, "Такого эндпоинта не существует", 404);
            }
        }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("users")) {
            return Endpoint.GET_USERS;
        }

      if (pathParts.length == 5 && pathParts[1].equals("users") && pathParts[4].equals("add")) {
          if (requestMethod.equals("POST")) {
              return Endpoint.POST_USERS;
          }
      }

        return Endpoint.UNKNOWN;
    }

    private void handleGetUser(HttpExchange exchange) throws IOException {
        writeResponse(exchange, gson.toJson(managerUser.getUsers()), 200);
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

    private void handlePostUser(HttpExchange exchange) throws IOException {
        // реализуйте обработку добавления комментария
        Optional<Integer> postIdOpt = PostsHandler.getPostId(exchange);
        Optional<Integer> userIdOpt = getUserId(exchange);
        // извлеките идентификатор поста и обработайте исключительные ситуации
        if (userIdOpt.isEmpty()) {
            writeResponse(exchange,"Некорректный идентификатор пользователя", 400);
            return;
        }

            /* Получите тело запроса в виде текста в формате JSON и преобразуйте его в объект Comment.
            Учтите, что может быть передан некоректный JSON — эту ситуацию нужно обработать.
            Подумайте, какие ещё ситуации требуют обработки. */

        int postId = postIdOpt.get();
        int userId = userIdOpt.get();


        // найдите пост с указанным идентификатором и добавьте в него комментарий
        for (Post post: PostsHandler.posts) {
            if (post.getId() == postId) {
                post.addComment(new Comment(managerUser.getUser(userId)));
                writeResponse(exchange, "Добавлен пользователь в пост ", 201);
                return;
            }
        }
        writeResponse(exchange, "Пост с идентификатором " + postId + " не найден", 404);
    }

    private Optional<Integer> getUserId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[3]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }


    }

}

