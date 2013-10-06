import org.ayfaar.game.controllers.GameController;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = GameTest.class)
@Configuration
@PropertySource({"classpath:database.properties"})
@ImportResource("classpath:hibernate.xml")
@ComponentScan(basePackages = "org.ayfaar.game")
public class GameTest {
    @Autowired GameController gameController;

    @Bean // for @PropertySource work
    public static PropertySourcesPlaceholderConfigurer pspc(){
        return new PropertySourcesPlaceholderConfigurer();
    }
}
