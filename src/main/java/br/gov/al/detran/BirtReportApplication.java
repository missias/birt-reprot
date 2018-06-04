package br.gov.al.detran;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
@SpringBootApplication
@ComponentScan("br.gov.al.detran, br.gov.al.detran.birtreport")
@EnableAutoConfiguration(exclude = MongoAutoConfiguration.class)
public class BirtReportApplication  extends SpringBootServletInitializer implements EmbeddedServletContainerCustomizer {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(BirtReportApplication.class, args);
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer arg0) {
		// TODO Auto-generated method stub

	}


}
