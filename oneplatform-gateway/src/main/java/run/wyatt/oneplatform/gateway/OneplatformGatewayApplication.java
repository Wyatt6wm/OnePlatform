package run.wyatt.oneplatform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Wyatt
 * @date 2023/5/25 14:07
 */
@EnableDiscoveryClient
@SpringBootApplication
public class OneplatformGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(OneplatformGatewayApplication.class, args);
    }
}
