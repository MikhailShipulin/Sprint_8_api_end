import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class UsersHandler implements HttpHandler {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Gson gson = new Gson();

    ManagerUser managerUser = new ManagerUser();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

            switch (endpoint){
                case GET_USERS: {
                    handleGetPosts(exchange);
                    break;
                }
            }

        }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("users")) {
            return Endpoint.GET_USERS;
        }

        return Endpoint.UNKNOWN;
    }

    private void handleGetPosts(HttpExchange exchange) throws IOException {
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

}

