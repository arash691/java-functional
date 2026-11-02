package shopflow.api.w11.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code shopflow.catalog.*}. pageSize is the default page size of GET /api/products. */
@ConfigurationProperties("shopflow.catalog")
public record CatalogProperties(int pageSize) {
}
