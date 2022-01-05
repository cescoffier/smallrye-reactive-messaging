package quickstart;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.se.SeContainerInitializer;

@ApplicationScoped
public class Main {

    public static void main(String[] args) {
        SeContainerInitializer.newInstance().initialize();
    }

}
