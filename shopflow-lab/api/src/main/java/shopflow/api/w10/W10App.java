package shopflow.api.w10;

import org.springframework.boot.SpringApplication;

import shopflow.api.common.NoDbApp;

/** Week 10 application. Run with: ./mvnw -pl api spring-boot:run -Dspring-boot.run.main-class=shopflow.api.w10.W10App (see lesson). */
@NoDbApp
public class W10App {

    public static void main(String[] args) {
        SpringApplication.run(W10App.class, args);
    }
}
