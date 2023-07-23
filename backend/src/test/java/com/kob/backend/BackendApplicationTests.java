package com.kob.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("pc"));
//        System.out.println(encoder.matches("plihui", "$2a$10$BQ8eRXT0BRUBCEz5maDSzuA2CQzhTzCfd.Z.xKHB6COtBptho31lm"));
    }

}
