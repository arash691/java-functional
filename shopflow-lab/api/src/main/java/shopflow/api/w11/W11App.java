package shopflow.api.w11;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import shopflow.api.common.NoDbApp;

@NoDbApp
@ConfigurationPropertiesScan
public class W11App {

    public static void main(String[] args) {
        SpringApplication.run(W11App.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
