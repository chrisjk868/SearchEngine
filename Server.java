import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

import com.sun.net.httpserver.*;

public class Server {
    // Port number used to connect to this server
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8000"));
    // Maximum number of matches returned in response
    private static final int MAX_MATCHES = 10;
    // Maximum number of terms to display in preview
    private static final int MAX_TERMS = 50;
    // JSON endpoint structure
    private static final String QUERY_TEMPLATE = "{\"items\":[%s]}";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("java Server [files]");
        }
        SearchEngine engine = new SearchEngine();
        for (String filename : args) {
            engine.index(Files.readString(Paths.get(filename)));
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", (HttpExchange t) -> {
            String html = Files.readString(Paths.get("index.html"));
            send(t, "text/html; charset=utf-8", html);
        });
        server.createContext("/query", (HttpExchange t) -> {
            String s = parse("s", t.getRequestURI().getQuery().split("&"));
            if (s.equals("")) {
                send(t, "application/json", String.format(QUERY_TEMPLATE, ""));
                return;
            }
            send(t, "application/json", String.format(QUERY_TEMPLATE, json(engine.search(s))));
        });
        server.setExecutor(null);
        server.start();
    }

    private static String parse(String key, String... params) {
        for (String param : params) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(key)) {
                return pair[1];
            }
        }
        return "";
    }

    private static void send(HttpExchange t, String contentType, String data)
            throws IOException, UnsupportedEncodingException {
        t.getResponseHeaders().set("Content-Type", contentType);
        byte[] response = data.getBytes("UTF-8");
        t.sendResponseHeaders(200, response.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(response);
        }
    }

    private static String json(Iterable<String> matches) {
        Iterator<String> iter = matches.iterator();
        StringBuilder results = new StringBuilder();
        for (int i = 0; i < MAX_MATCHES && iter.hasNext(); i += 1) {
            if (results.length() > 0) {
                results.append(',');
            }
            String[] pair = iter.next().split("\n", 2);
            String title = pair[0].replace("\"", "\\\"");
            String body = preview(pair[1]).replace("\"", "\\\"");
            results.append('{')
                   .append("\"title\":")
                   .append('"').append(title).append('"')
                   .append(',')
                   .append("\"body\":")
                   .append('"').append(body).append('"')
                   .append('}');
        }
        return results.toString();
    }

    private static String preview(String text) {
        List<String> terms = Arrays.asList(text.split("\\s+"));
        if (terms.size() > MAX_TERMS) {
            return String.join(" ", terms.subList(0, MAX_TERMS)) + "...";
        }
        return text;
    }
}
