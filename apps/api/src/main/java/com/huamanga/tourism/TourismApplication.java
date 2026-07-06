package com.huamanga.tourism;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Se excluye UserDetailsServiceAutoConfiguration: la autenticación es JWT
 * puro (el filtro puebla el SecurityContext); sin esta exclusión Spring Boot
 * crea un usuario en memoria con contraseña aleatoria que nadie usa y lo
 * anuncia con un WARN en cada arranque.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class TourismApplication {

	public static void main(String[] args) {
		SpringApplication.run(TourismApplication.class, args);
	}

}
