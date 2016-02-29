package mongodb;

import mongodb.configuration.RemoteMongoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

/**
 *  This is the main singleton handling MongoDB
 */
@Singleton
public class SpringMongo {

    private static Logger.ALogger LOGGER = Logger.of(SpringMongo.class);

    private static ThreadLocal<MongoServiceProvider> mongoProvider = new ThreadLocal<>();

    private static AnnotationConfigApplicationContext springContext = null;

    private final static String SERVICE_PROVIDER_NAME_CFG = "mongodb.serviceProviderClass";

    private static Class<?> serviceProviderClass = null;

    private final Environment environment;

    private final Configuration configuration;

    @Inject
    public SpringMongo(Environment environment, Configuration configuration, ApplicationLifecycle lifecycle) {

        this.environment = environment;
        this.configuration = configuration;

        initialize();

        lifecycle.addStopHook(() -> {
            springContext.close();

            // TODO clean the application lifecycle here
            return F.Promise.pure(null);
        });
    }

    /**
     * Get the implementation of ServiceProvider all your repositories.
     * This is configured in your application configuration with the key: mongodb.serviceProviderClass.
     *
     * @param <E> the implementation class of your ServiceProvider.
     * @return the configured implementation of the ServiceProvider.
     */
    public static <E extends MongoServiceProvider> E get() {
        mongoProvider.set((MongoServiceProvider) springContext.getBean(serviceProviderClass));
        return (E) mongoProvider.get();
    }

    /**
     * This does the initialization of the plugin by wiring up the springcontext according to the configuration settings.
     */
    private void initialize() {

        springContext = new AnnotationConfigApplicationContext();

        String serviceProviderClassName = configuration.getString(SERVICE_PROVIDER_NAME_CFG);
        if (StringUtils.isEmpty(serviceProviderClassName) == true) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("No configuration for the mongo ServiceProvider found: " + SERVICE_PROVIDER_NAME_CFG + " must be" + " set for this plugin.");
            }
            return;
        }
        final ClassLoader classLoader = environment.classLoader();
        try {
            serviceProviderClass = Class.forName(serviceProviderClassName, false, classLoader);
            Annotation annotation = serviceProviderClass.getAnnotation(Component.class);
            if (annotation == null) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Class : " + serviceProviderClassName + " must be annotated with: " + Component.class
                            .getCanonicalName());
                }
                return;
            }
        } catch (ClassNotFoundException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while getting mongo class from configuration: " + SERVICE_PROVIDER_NAME_CFG + " = " + serviceProviderClassName, e);
            }
            return;
        }


        final String mode = configuration.getString("mongodb.mode");

        if (mode.equals("remote")) {
            if (LOGGER.isDebugEnabled() == true) {
                LOGGER.debug("Loading remote configuration");
            }
            springContext.register(RemoteMongoConfiguration.class);
        }

        if (mode.equals("own")) {

            if(LOGGER.isDebugEnabled() == true) {
                LOGGER.debug("Loading own configuration");
            }

            String configurationClassName = configuration.getString("mongodb.ownConfigurationClass");
            if (StringUtils.isEmpty(configurationClassName) == true) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("if you use mongodb.mode = own, you must provide a configuration class mongodb.ownConfigurationClass");
                    return;
                }
            }

            try {
                Class<?> configurationClass = Class.forName(configurationClassName, false, classLoader);
                springContext.register(configurationClass);
            } catch (ClassNotFoundException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while getting mongo class for configuration: " + configurationClassName, e);
                }
            }
        }


        if (springContext == null) {
            if (LOGGER.isErrorEnabled() == true) {
                LOGGER.error("Could not load config must be: embedded or embeddedWithWebServer");
            }
        }

        springContext.scan("mongo", "mongodb.repositories");
        springContext.refresh();
        springContext.start();
        springContext.getAutowireCapableBeanFactory().autowireBean(serviceProviderClass);
        springContext.registerShutdownHook();
    }
}
