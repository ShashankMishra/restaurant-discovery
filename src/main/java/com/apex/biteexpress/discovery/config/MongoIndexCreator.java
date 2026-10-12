package com.apex.biteexpress.discovery.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MongoIndexCreator {

  private static final Logger LOG = Logger.getLogger(MongoIndexCreator.class);

  @Inject MongoClient mongoClient;

  @ConfigProperty(name = "quarkus.mongodb.database")
  String database;

  void onStart(@Observes @Priority(1) StartupEvent event) {
    MongoCollection<Document> restaurants =
        mongoClient.getDatabase(database).getCollection("restaurants");

    // 2dsphere is required for $geoNear; create it before any geo query runs.
    restaurants.createIndex(Indexes.geo2dsphere("location"));
    restaurants.createIndex(
        Indexes.compoundIndex(Indexes.ascending("city"), Indexes.descending("rating.aggregate")));
    restaurants.createIndex(
        Indexes.compoundIndex(Indexes.ascending("cuisines"), Indexes.ascending("city")));
    restaurants.createIndex(Indexes.ascending("nameLower"));
    restaurants.createIndex(
        Indexes.compoundIndex(
            Indexes.text("name"), Indexes.text("cuisines"), Indexes.text("locality")),
        new IndexOptions().name("restaurants_text"));
    LOG.info("restaurant indexes ensured");
  }
}
