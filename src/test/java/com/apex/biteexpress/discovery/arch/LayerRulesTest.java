package com.apex.biteexpress.discovery.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.apex.biteexpress.discovery",
    importOptions = ImportOption.DoNotIncludeTests.class)
class LayerRulesTest {

  @ArchTest
  static final ArchRule domainDoesNotDependOnOuterLayers =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..api..", "..service..", "..repo..", "..ingest..");

  @ArchTest
  static final ArchRule apiDoesNotSkipServiceToTalkToMongo =
      noClasses()
          .that()
          .resideInAPackage("..api..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..repo..");

  @ArchTest
  static final ArchRule persistenceDoesNotDependOnHttp =
      noClasses()
          .that()
          .resideInAnyPackage("..repo..", "..ingest..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..api..");
}
