package com.enigma.superwallet;

import com.enigma.superwallet.config.CorsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CorsConfig.class)
public class SuperWalletApplication {

	public static void main(String[] args) {
		SpringApplication.run(SuperWalletApplication.class, args);
	}

}
