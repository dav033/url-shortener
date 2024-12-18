package io.github.dav033.config_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class UrlShortenerConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UrlShortenerConfigServerApplication.class, args);
	}


}
