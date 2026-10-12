# Disclosure

Public docs and the Kaggle dataset.

- Quarkus 3, the MongoDB Java driver, and ArchUnit docs for the APIs I don't keep in my head (`$geoNear` stage order, Panache `MongoEntity`, ArchUnit `DoNotIncludeTests`).
- Kaggle Zomato Restaurants Data. The file in git is a reduced sample, not a re-host of the full dump.

Search does not call out to a model. A sentence is parsed into `RestaurantQuery`, then the same list pipeline runs.
