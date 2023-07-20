package run.wyatt.oneplatform.todo.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * @author Wyatt
 * @date 2023/6/9 14:23
 */
@Configuration
public class DruidConfig {
    private static final String url = "jdbc:mysql://{0}/db_oneplatform?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8";
    @Value("${sys.mysql.domain}")
    private String domain;
    @Value("${sys.mysql.username}")
    private String username;
    @Value("${sys.mysql.password}")
    private String password;
    @Value("${sys.mysql.public-key}")
    private String publicKey;

    @Bean
    public DataSource druidDateSource() {
        DruidDataSource ds = new DruidDataSource();
        // 基础配置
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(MessageFormat.format(url, domain));
        ds.setUsername(username);
        ds.setPassword(password);
        // Druid配置
        ds.setInitialSize(1);
        ds.setMaxActive(100);
        ds.setMinIdle(1);
        ds.setMaxWait(60000);
        ds.setTimeBetweenEvictionRunsMillis(60000);
        ds.setMinEvictableIdleTimeMillis(300000);
        ds.setValidationQuery("select 'x'");
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);
        ds.setPoolPreparedStatements(true);
        ds.setMaxOpenPreparedStatements(50);
        ds.setMaxPoolPreparedStatementPerConnectionSize(20);
        try {
            ds.setFilters("config,stat,wall,log4j");
            Properties connectProperties = new Properties();
            connectProperties.put("config.decrypt", "true");    // 这里必须使用字符串
            connectProperties.put("config.decrypt.key", publicKey);
            ds.setConnectProperties(connectProperties);
            ds.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    @Bean
    public ServletRegistrationBean<Servlet> druidStateViewServlet() {
        ServletRegistrationBean<Servlet> bean = new ServletRegistrationBean<Servlet>(new StatViewServlet(), "/druid/*");

//        //白名单
//        bean.addInitParameter("allow", "0.0.0.0");
//        // 黑名单
//        bean.addInitParameter("deny", "0.0.0.0");
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
