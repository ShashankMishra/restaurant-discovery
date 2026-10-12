package com.apex.biteexpress.discovery.config;

import java.util.Map;

/** Zomato country codes from the Kaggle dump. Unknown codes stay as "Country {code}". */
public final class CountryNames {

  private static final Map<Integer, String> NAMES =
      Map.ofEntries(
          Map.entry(1, "India"),
          Map.entry(14, "Australia"),
          Map.entry(30, "Brazil"),
          Map.entry(37, "Canada"),
          Map.entry(94, "Indonesia"),
          Map.entry(148, "New Zealand"),
          Map.entry(162, "Philippines"),
          Map.entry(166, "Qatar"),
          Map.entry(184, "Singapore"),
          Map.entry(189, "South Africa"),
          Map.entry(191, "Sri Lanka"),
          Map.entry(208, "Turkey"),
          Map.entry(214, "UAE"),
          Map.entry(215, "United Kingdom"),
          Map.entry(216, "United States"));

  private CountryNames() {}

  public static String of(Integer code) {
    if (code == null) {
      return null;
    }
    return NAMES.getOrDefault(code, "Country " + code);
  }
}
