package mongodb;

import org.springframework.data.mongodb.core.MongoTemplate;

import javax.inject.Inject;

/**
 * This is the main class of the MongoServiceProvider.
 * This must be overwritten by you and can than hold some spring stuff.
 */
public class MongoServiceProvider {

    @Inject
    public MongoTemplate template;
}
