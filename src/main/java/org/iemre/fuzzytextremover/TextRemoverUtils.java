/*
    Written by Ismail E. Kartoglu
    Copyright 2016 Ismail E. Kartoglu
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package org.iemre.fuzzytextremover;


import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextRemoverUtils {


  /**
   *
   * @param sourceString a string that potentially contains (approximately) the searchedString
   * @param searchedString a string to search in sourceString
   * @param replacement a string to replace the matched string, when there is a match
   * @param minSimilarity the minimum Jaro-Winkler similarity (between 0 and 1, inclusive) that must occur between the strings for a replacement to take place.
   * @return a new string that should be similar to sourceString but that replaces approximately matching "searchedString"s
   */
  public static String replaceStringByJwd(String sourceString, String searchedString, String replacement, float minSimilarity) {
    if (minSimilarity < 0 || minSimilarity > 1) {
      throw new IllegalArgumentException("mimSimilarity must be in range 0 <= minSimilarity <= 1");
    }
    Set<String> list = getMatchingStringsByJwd(sourceString, searchedString, minSimilarity);
    String result = sourceString;
    for (String string : list) {
      result = result.replace(string, replacement);
    }
    return result;
  }

  /**
   *
   * @param sourceString a string that potentially contains (approximately) the searchedString
   * @param searchedString a string to search in sourceString
   * @param replacement a string to replace the matched string, when there is a match
   * @param maxDistance maximum edit distance to match, e.g. when maxDistance is 1, the words 'apple' and 'a.pple' match.
   * @return a new string that should be similar to sourceString but that replaces approximately matching "searchedString"s
   */
  public static String replaceStringByEditDistance(String sourceString, String searchedString, String replacement, int maxDistance) {
    Set<String> list = getMatchingStringsByEditDistance(sourceString, searchedString, maxDistance);
    String result = sourceString;
    for (String string : list) {
      result = result.replace(string, replacement);
    }
    return result;
  }

  /**
   * @param sourceString Source string to searchedString for approximately matching segments.
   * @param searchedString       String to searchedString in {@code sourceString}.
   * @param maxDistance  Maximum edit distance that should be satisfied.
   * @return A list of substrings from the @sourceString each of which approximately matches {@code searchedString}.
   */
  public static Set<String> getMatchingStringsByEditDistance(String sourceString, String searchedString, int maxDistance) {
    Set<String> matches = new HashSet<>();
    if (StringUtils.isBlank(searchedString)) {
      return matches;
    }
    searchedString = searchedString.trim();

    int searchLength = searchedString.length();

    sourceString = sourceString.toLowerCase().trim();
    searchedString = searchedString.toLowerCase().trim();
    for (int i = 0; i < sourceString.length(); i++) {
      int endIndex = i + searchLength;
      if (endIndex >= sourceString.length()) {
        endIndex = sourceString.length();
      }
      String completingString = getCompletingString(sourceString, i, endIndex);
      if (matches.contains(completingString)) {
        continue;
      }
      if (StringUtils.getLevenshteinDistance(completingString, searchedString) <= maxDistance) {
        matches.add(completingString.replace("\"", "\\\""));
        i = endIndex;
      }
    }
    return matches;
  }

  /**
   * @param sourceString Source string to searchedString for approximately matching segments.
   * @param searchedString String to searchedString in {@code sourceString}.
   * @param minSimilarity Minimum JWD similarity
   * @return A list of substrings from the @sourceString each of which approximately matches {@code searchedString}.
   */
  public static Set<String> getMatchingStringsByJwd(String sourceString, String searchedString, float minSimilarity) {
    if (minSimilarity < 0 | minSimilarity > 1) {
      throw new IllegalArgumentException("mimSimilarity must be in range 0 <= minSimilarity <= 1");
    }
    Set<String> matches = new HashSet<>();
    if (StringUtils.isBlank(searchedString)) {
      return matches;
    }
    searchedString = searchedString.trim();

    int searchLength = searchedString.length();

    sourceString = sourceString.toLowerCase().trim();
    searchedString = searchedString.toLowerCase().trim();
    for (int i = 0; i < sourceString.length(); i++) {
      int endIndex = i + searchLength;
      if (endIndex >= sourceString.length()) {
        endIndex = sourceString.length();
      }
      String completingString = getCompletingString(sourceString, i, endIndex);
      if (matches.contains(completingString)) {
        continue;
      }
      if (StringUtils.getJaroWinklerDistance(completingString, searchedString) >= minSimilarity) {
        matches.add(completingString.replace("\"", "\\\""));
        i = endIndex;
      }
    }
    return matches;
  }

  public static String getCompletingString(String string, int begin, int end) {
    while (begin > 0 && StringUtils.isAlphanumeric(string.substring(begin, begin + 1))) {
      begin -= 1;
    }
    if (begin != 0)
      begin += 1;

    while (end < string.length() - 1 && StringUtils.isAlphanumeric(string.substring(end, end + 1))) {
      end += 1;
    }

    String regex = "\\w+(\\(?\\)?\\s+\\w+)*";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(string.substring(begin, end));

    if (matcher.find()) {
      return matcher.group();
    }

    return StringUtils.EMPTY;
  }

  /**
   *
   * @param sourceString a source string that potentially partially contains searchedString
   * @param searchedString potentially a long piece of string, such as an address,
   *                       e.g. 8, Sweetpea Way, CA4 2ZA, Cambridge, UK
   * @param replacement The replacement for searchedString
   * @param threshold between 0 and 1 (inclusive), 0.9 means replacement will occur when
   *                  90% of the searchedString matches any part of the sourceString (sentence level match)
   * @param maxDistance word-level edit distance, e.g. CA4 2ZA will match CA42ZA when maxDistance is 1
   * @return a new version of sourceString that has replaced matching searchedStrings in sourceString
   *         with the given replacement
   */
  public static String replaceMatchingWindowsByEditDistance(String sourceString,
                                                            String searchedString,
                                                            String replacement,
                                                            double threshold,
                                                            int maxDistance) {

    List<MatchingWindow> windows = getMatchingWindowsAboveThresholdByEditDistance(sourceString,
            searchedString, threshold, maxDistance);

    String result = sourceString;
    for (MatchingWindow window : windows) {
      result = result.replace(window.getMatchingText(), replacement);
    }

    return result;
  }

  /**
   * @param text      The source text.
   * @param search    The text to be searched in {@code text}.
   * @param threshold The threshold value between 0.0 - 1.0.
   * @return A list of MatchingWindow objects.
   */
  public static List<MatchingWindow> getMatchingWindowsAboveThresholdByEditDistance(String text,
                                                                                    String search,
                                                                                    double threshold,
                                                                                    int maxDistance) {
    if (StringUtils.isBlank(text)) {
      return new ArrayList<>();
    }
    if (StringUtils.isBlank(search)) {
      return new ArrayList<>();
    }
    String[] addressWords = search.split(" ");
    int bagSize = addressWords.length;
    String[] textWords = text.split(" ");
    int textWordCount = textWords.length;
    List<MatchingWindow> windows = new ArrayList<>();
    for (int i = 0; i < textWordCount; i++) {
      MatchingWindow window = takeBag(textWords, i, bagSize, maxDistance);
      window.setScoreAccordingTo(addressWords);
      window.setMatchingText(text.substring(window.getBegin(), window.getEnd()));
      windows.add(window);
    }

    Collections.sort(windows);
    windows = windows.stream().filter(window -> window.isScoreAboveThreshold(threshold)).collect(Collectors.toList());

    return windows;
  }

  private static MatchingWindow takeBag(String[] textWords, int startWordIndex, int bagSize, int maxDistance) {
    MatchingWindow window = new MatchingWindow();
    window.setMaxDistance(maxDistance);
    int offset = 0;
    for (int i = startWordIndex; i < startWordIndex + bagSize; i++) {
      if (i >= textWords.length) {
        break;
      }
      offset += textWords[i].length() + 1;
      window.addWord(textWords[i]);
    }
    offset -= 1;
    int begin = 0;
    for (int i = 0; i < startWordIndex; i++) {
      begin += textWords[i].length() + 1;
    }
    window.setBegin(begin);
    window.setEnd(begin + offset);

    return window;
  }


}
