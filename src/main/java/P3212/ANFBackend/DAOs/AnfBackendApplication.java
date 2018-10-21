package P3212.ANFBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SpringBootApplication
public class AnfBackendApplication {
public static ApplicationContext cont;
	public static void main(String[] args) {
            SpringApplication.run(AnfBackendApplication.class, args);
            cont = new ClassPathXmlApplicationContext("beans.xml");
            // do sth
            ((ClassPathXmlApplicationContext)cont).close();
	}
  
}
