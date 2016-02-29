package mongodb.configuration;

import com.mongodb.MongoClient;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuration which is used to connect via rest to the database.
 */
@Configuration
@EnableMongoRepositories(basePackages = "mongodb.repositories")
public class RemoteMongoConfiguration extends MongoBaseConfiguration {

    private MongoClient client;

    {
        Config config = ConfigFactory.load();
        try {
            client = new MongoClient(config.getString("mongodb.remoteHost"), config.getInt("mongodb.remotePort"));
        } catch (Exception o_O) {
            throw new RuntimeException("Spring Configuration RemoteMongoConfiguration could not be initialized", o_O);
        }
    }

    @Bean
    @Override
    public MongoDbFactory mongoDbFactory() {
        return new SimpleMongoDbFactory(client, ConfigFactory.load().getString("mongodb.dbName"));
    }

    @Bean
    @Override
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDbFactory());
    }

    @Override
    public void destroy() throws Exception {
        client.close();
    }
}
