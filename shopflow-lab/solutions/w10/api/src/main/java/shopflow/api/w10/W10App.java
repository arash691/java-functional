package shopflow.api.w10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import shopflow.api.common.NoDbApp;

@NoDbApp
@ConfigurationPropertiesScan
public class W10App {

    public static void main(String[] args) {
        SpringApplication.run(W10App.class, args);
    }
}
