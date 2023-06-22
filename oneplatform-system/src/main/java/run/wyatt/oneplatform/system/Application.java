package run.wyatt.oneplatform.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import run.wyatt.oneplatform.common.util.LogUtil;

/**
 * @author Wyatt
 * @date 2023/5/27 11:33
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        log.info(LogUtil.divider("Oneplatform System 已启动"));
    }
}
