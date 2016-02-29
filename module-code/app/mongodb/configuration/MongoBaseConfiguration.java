package mongodb.configuration;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is the base configuration for the Mongo Plugin.
 */
public abstract class MongoBaseConfiguration implements DisposableBean {

    public abstract MongoDbFactory mongoDbFactory();

    public abstract MongoTemplate mongoTemplate();

}
