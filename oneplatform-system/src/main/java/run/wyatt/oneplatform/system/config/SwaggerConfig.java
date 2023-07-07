package run.wyatt.oneplatform.system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/22 17:34
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Value("${sys.env}")
    private String env;

    // 创建RESTful API摘要(Docket)对象
    @Bean
    public Docket createRestApi() {
        boolean enable = !env.equals("run");
        return new Docket(DocumentationType.SWAGGER_2)  // 定义是Swagger2文档
                .enable(enable)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("run.wyatt.oneplatform.system.controller"))   // 扫描制定包下的接口
                .paths(PathSelectors.ant("/api/sys/**"))    // 筛选制定的请求路径
                .build()
                .globalOperationParameters(getOperationParameters());
    }

    // 创建Swagger2页面的API信息对象
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Oneplatform System接口文档")
                .version("1.0")
                .build();
    }

    // 生成全局通用参数
    private List<Parameter> getOperationParameters() {
        List<Parameter> params = new ArrayList<>();
        params.add(new ParameterBuilder()
                .name("token")
                .description("登录认证成功后服务器发放的token令牌")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build());
        return params;
    }
}
