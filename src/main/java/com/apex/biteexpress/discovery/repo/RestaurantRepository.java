package com.apex.biteexpress.discovery.repo;

import com.apex.biteexpress.discovery.domain.Restaurant;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RestaurantRepository implements PanacheMongoRepositoryBase<Restaurant, Long> {

  @Inject MongoClient mongoClient;

  @ConfigProperty(name = "quarkus.mongodb.database")
  String database;

  public MongoCollection<Document> documents() {
    return mongoClient.getDatabase(database).getCollection("restaurants");
  }

  public long nextId() {
    Document updated =
        mongoClient
            .getDatabase(database)
            .getCollection("counters")
            .findOneAndUpdate(
                Filters.eq("_id", "restaurantId"),
                Updates.inc("seq", 1L),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
    return updated.get("seq", Number.class).longValue();
  }

  public List<Document> aggregate(List<Document> pipeline) {
    return documents().aggregate(pipeline).into(new java.util.ArrayList<>());
  }

  public long countDocuments(Document filter) {
    return documents().countDocuments(filter);
  }

  public void bumpCounterToAtLeast(long minSeq) {
    mongoClient
        .getDatabase(database)
        .getCollection("counters")
        .updateOne(
            Filters.eq("_id", "restaurantId"),
            Updates.max("seq", minSeq),
            new com.mongodb.client.model.UpdateOptions().upsert(true));
  }
}
