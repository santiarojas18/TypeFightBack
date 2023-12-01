package edu.eci.arsw.typefight;

import edu.eci.arsw.typefight.model.Player;
import edu.eci.arsw.typefight.model.TypeFight;
import edu.eci.arsw.typefight.service.PlayerService;
import edu.eci.arsw.typefight.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;

@SpringBootApplication
@ComponentScan(basePackages = {"edu.eci.arsw.typefight"})
public class TypeFightApplication {


	public static void main(String[] args) {
		SpringApplication.run(TypeFightApplication.class, args);
	}

}
