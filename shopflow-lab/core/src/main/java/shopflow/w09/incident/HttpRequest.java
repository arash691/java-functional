package shopflow.w09.incident;

import java.util.Map;

public record HttpRequest(String method, String path, Map<String, String> headers, String body) {}
