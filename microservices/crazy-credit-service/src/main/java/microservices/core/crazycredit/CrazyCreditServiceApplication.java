package microservices.core.crazycredit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class CrazyCreditServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrazyCreditServiceApplication.class, args);
	}

}
