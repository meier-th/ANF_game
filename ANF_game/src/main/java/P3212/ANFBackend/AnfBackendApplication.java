package P3212.ANFBackend;

import P3212.ANFBackend.Services.ServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SpringBootApplication(scanBasePackages = {"P3212.ANFBackend.Repositories", "P3212.ANFBackend.EntityClasses", "P3212.ANFBackend"})
//@EnableJdbcRepositories("P3212.ANFBackend.Repositories")
@ComponentScan({"P3212.ANFBackend.Repositories", "P3212.ANFBackend.EntityClasses"})
public class AnfBackendApplication {
    public static ApplicationContext cont;
    @Autowired
    static ServiceBean serviceBean;

    public static void main(String[] args) {
        SpringApplication.run(AnfBackendApplication.class, args);
        cont = new ClassPathXmlApplicationContext("beans.xml");
//        serviceBean = (ServiceBean) cont.getBean("serviceBean");
//        serviceBean.userRepository = (UserRepo) (cont.getBean("userRepo"));
        System.out.println(serviceBean.userRepository);
        serviceBean.findAll().forEach(user -> System.out.println(user.getUsername() + " " + user.getEmail()));
        ((ClassPathXmlApplicationContext) cont).close();
    }

}
