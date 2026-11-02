package shopflow.api.w11;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

/**
 * SF-07, spec first: the OpenAPI document is the contract. This test reads
 * src/main/resources/openapi/catalog.openapi.yaml and checks it describes the API the other tests demand.
 */
@Tag("w11")
@DisplayName("SF-07 catalog.openapi.yaml describes the Catalog API")
class Sf07SpecContractTest {

    static Map<String, Object> spec;

    @BeforeAll
    static void load() throws Exception {
        try (InputStream in = new ClassPathResource("openapi/catalog.openapi.yaml").getInputStream()) {
            spec = new Yaml().load(in);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object o) {
        return o instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    private static Map<String, Object> operation(String path, String method) {
        return map(map(map(spec.get("paths")).get(path)).get(method));
    }

    private static void hasResponses(String path, String method, String... codes) {
        Map<String, Object> responses = map(operation(path, method).get("responses"));
        assertThat(responses.keySet().stream().map(String::valueOf).toList())
                .as("response codes of %s %s", method.toUpperCase(), path)
                .contains(codes);
    }

    @Test
    @DisplayName("is an OpenAPI 3 document")
    void version() {
        assertThat(String.valueOf(spec.get("openapi"))).startsWith("3.");
    }

    @Test
    @DisplayName("/api/products has GET (list) and POST (create)")
    void collectionOperations() {
        assertThat(operation("/api/products", "get")).as("GET /api/products").isNotEmpty();
        assertThat(operation("/api/products", "post")).as("POST /api/products").isNotEmpty();
        hasResponses("/api/products", "get", "200");
        hasResponses("/api/products", "post", "201", "400", "409");
    }

    @Test
    @DisplayName("/api/products/{id} has GET, PUT and DELETE with the documented status codes")
    void itemOperations() {
        assertThat(operation("/api/products/{id}", "put")).as("PUT /api/products/{id}").isNotEmpty();
        assertThat(operation("/api/products/{id}", "delete")).as("DELETE /api/products/{id}").isNotEmpty();
        hasResponses("/api/products/{id}", "get", "200", "404");
        hasResponses("/api/products/{id}", "put", "200", "400", "404", "409");
        hasResponses("/api/products/{id}", "delete", "204", "404");
    }

    @Test
    @DisplayName("error responses are declared as application/problem+json")
    void problemMediaType() {
        Map<String, Object> notFound = map(map(operation("/api/products/{id}", "get").get("responses")).get("404"));
        assertThat(map(notFound.get("content")).keySet()).contains("application/problem+json");
        Map<String, Object> badRequest = map(map(operation("/api/products", "post").get("responses")).get("400"));
        assertThat(map(badRequest.get("content")).keySet()).as("400 of POST").contains("application/problem+json");
    }

    @Test
    @DisplayName("list operation documents the page and size query parameters")
    void paging() {
        Object params = operation("/api/products", "get").get("parameters");
        assertThat(params).isInstanceOf(List.class);
        List<String> names = ((List<?>) params).stream().map(p -> String.valueOf(map(p).get("name"))).toList();
        assertThat(names).contains("page", "size");
    }

    @Test
    @DisplayName("schemas ProductRequest and ProductResponse exist and name their fields")
    void schemas() {
        Map<String, Object> schemas = map(map(spec.get("components")).get("schemas"));
        assertThat(schemas.keySet()).contains("ProductRequest", "ProductResponse");
        assertThat(map(map(schemas.get("ProductRequest")).get("properties")).keySet()).contains("sku", "name", "price");
        assertThat(map(map(schemas.get("ProductResponse")).get("properties")).keySet()).contains("id", "sku", "name", "price");
    }
}
