package shopflow.api.w10;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import shopflow.api.w10.config.ShopProperties;

/** EX-10-02: typed configuration with @ConfigurationProperties. */
@Tag("w10")
@SpringBootTest(properties = "shopflow.shop.name=TestShop")
@DisplayName("EX-10-02 ShopProperties binds shopflow.shop.*")
class Ex2ShopPropertiesTest {

    @Autowired(required = false)
    ShopProperties props;

    @Test
    @DisplayName("ShopProperties is registered as a bean")
    void registered() {
        assertThat(props)
                .as("no ShopProperties bean: register it with @ConfigurationPropertiesScan on W10App "
                        + "(or @EnableConfigurationProperties)")
                .isNotNull();
    }

    @Test
    @DisplayName("values come from shopflow-w10.yml")
    void valuesFromYaml() {
        assertThat(props).isNotNull();
        assertThat(props.currency()).isEqualTo("EUR");
        assertThat(props.freeShippingThreshold()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("a test property overrides the yaml value")
    void overridden() {
        assertThat(props).isNotNull();
        assertThat(props.name()).isEqualTo("TestShop");
    }
}
