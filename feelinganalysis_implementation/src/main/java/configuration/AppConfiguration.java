package configuration;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "wordsembeddings", "preprocess.filter.pckg", "sentimentanalysis" })
public class AppConfiguration {

	private static final Logger LOGGER = Logger.getLogger(AppConfiguration.class);

	public AppConfiguration() {
		
		BasicConfigurator.configure();
		LOGGER.debug(" configuration init...");

	}

}
