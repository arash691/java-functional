package shopflow.api.w11.ping;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Worked example for S1: the smallest useful REST controller. */
@RestController
@RequestMapping("/api/ping")
public class PingController {

    @GetMapping
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }
}
