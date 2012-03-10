/**
 * 
 */
package net.avail.main;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import recng.recommendations.RecommendationService;

/**
 * @author adamskogman
 * 
 */
public class RecommendationServiceMain {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        int i = 0;
        String graph = args[i++];
        String productDataFile = args[i++];
        String productFormatFile = args[i++];

        long t0 = System.currentTimeMillis();

        // TODO Configure Context using arguments

        // Boot using Spring
        ClassPathXmlApplicationContext applicationContext =
            new ClassPathXmlApplicationContext(
                                               "/spring/recommendation-context.xml");

        RecommendationService service = applicationContext
            .getBean(RecommendationService.class);

        long t1 = System.currentTimeMillis();
        System.out.println(service);
        System.out.println("Done in " + (t1 - t0) + " ms.");
    }

}
