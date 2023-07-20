package run.wyatt.oneplatform.todo.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author Wyatt
 * @date 2023/6/9 14:37
 */
@Configuration
@MapperScan("run.wyatt.oneplatform.**.dao")
public class MybatisConfig {
    @Autowired
    private DataSource dataSource;

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTypeAliasesPackage("run.wyatt.oneplatform.**.model.entity");
        sessionFactory.setMapperLocations(resolver.getResources("classpath:/mapper/*Mapper.xml"));
        sessionFactory.setConfigLocation(resolver.getResource("classpath:/mybatis-config.xml"));

        return sessionFactory.getObject();
    }
}
