package run.wyatt.oneplatform.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Wyatt
 * @date 2023/5/27 11:33
 */
@EnableDiscoveryClient
@SpringBootApplication
public class OneplatformUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(OneplatformUserApplication.class, args);
    }
}
