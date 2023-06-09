package run.wyatt.oneplatform.user.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.sql.DataSource;

/**
 * @author Wyatt
 * @date 2023/6/9 14:23
 */
@Configuration
public class DruidConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druidDataSource() {
        return new DruidDataSource();
    }

    @Bean
    public ServletRegistrationBean<Servlet> druidStateViewServlet() {
        ServletRegistrationBean<Servlet> bean = new ServletRegistrationBean<Servlet>(new StatViewServlet(), "/druid/*");

        //白名单
        bean.addInitParameter("allow", "127.0.0.1");
        // 黑名单
//        servletRegistrationBean.addInitParameter("deny", "0.0.0.0");
        // 登录Druid监控后台的账号密码
        bean.addInitParameter("loginUsername", "admin");
        bean.addInitParameter("loginPassword", "admin");
        // 是否能够重置数据
        bean.addInitParameter("resetEnable", "true");

        return bean;
    }

    @Bean
    public FilterRegistrationBean<Filter> webStatFilter() {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<Filter>();
        bean.setFilter(new WebStatFilter());
        bean.addUrlPatterns("/*");
        bean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        return bean;
    }
}
