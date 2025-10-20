package org.scoula.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableScheduling
@PropertySource({"classpath:/application-dev.properties"})
@MapperScan(basePackages = {
	"org.scoula.user.mapper",
	"org.scoula.asset.mapper",
	"org.scoula.recommend.mapper",
	"org.scoula.faq.mapper",
	"org.scoula.branch.mapper",
	"org.scoula.booking.mapper",
	"org.scoula.auth.mapper",
	"org.scoula.product.mapper",
	"org.scoula.gift.mapper",
	"org.scoula.news.mapper",
	"org.scoula.statistics.mapper",
	"org.scoula.View.Event.mapper"
})
@ComponentScan(basePackages = {
	"org.scoula.user.service",
	"org.scoula.asset.service",
	"org.scoula.recommend.service",
	"org.scoula.faq.service",
	"org.scoula.branch.service",
	"org.scoula.booking.service",
	"org.scoula.View.codef.util",
	"org.scoula.View.codef.service",
	"org.scoula.View.codef.dto",
	"org.scoula.product.service",
	"org.scoula.View.home.service",
	"org.scoula.auth.service",
	"org.scoula.product.service",
	"org.scoula.sms.service",
	"org.scoula.View.preference.service",
	"org.scoula.gift.service",
	"org.scoula.news.service",
	"org.scoula.gpt.service",
	"org.scoula.statistics.service",
	"org.scoula.statistics.scheduler",
	"org.scoula.news.scheduler",
	"org.scoula.View.Event.Service",
	"org.scoula.news.service",
	"org.scoula.question.service"
})
@Log4j2
@EnableTransactionManagement
/***
 * Mybatis- MapperScan : Mapper인터페이스를 검색할 패키지 목록 지정
 * -> 해당 인터페이스를 빈으로 지정
 * 구현체를 동적으로 자동으로 생성
 */
public class RootConfig {

	@Autowired
	ApplicationContext applicationContext;
	@Value("${jdbc.driver}")
	String driver;
	@Value("${jdbc.url}")
	String url;
	@Value("${jdbc.username}")
	String username;
	@Value("${jdbc.password}")
	String password;

	@Bean
	public DataSource dataSource() {
		HikariConfig config = new HikariConfig();

		config.setDriverClassName(driver);
		config.setJdbcUrl(url);
		config.setUsername(username);
		config.setPassword(password);

		HikariDataSource dataSource = new HikariDataSource(config);
		return dataSource;
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
		sqlSessionFactory.setConfigLocation(
			applicationContext.getResource("classpath:/mybatis-config.xml"));
		sqlSessionFactory.setDataSource(dataSource());
		return sqlSessionFactory.getObject();
	}

	@Bean
	public DataSourceTransactionManager transactionManager() {
		DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource());
		return manager;
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
