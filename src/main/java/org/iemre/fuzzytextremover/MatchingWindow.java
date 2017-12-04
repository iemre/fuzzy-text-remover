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

import java.util.ArrayList;
import java.util.List;

public class MatchingWindow implements Comparable<MatchingWindow> {

  private float score = 0;
  private int begin;
  private int end;
  private int maxDistance;
  private String matchingText;
  private List<String> wordSet = new ArrayList<>();

  public MatchingWindow() {
  }

  public String getMatchingText() {
    return matchingText.replace("\"", "\\\"");
  }

  public void addWord(String word) {
    wordSet.add(word);
  }

  public List<String> getWordSet() {
    return wordSet;
  }

  public void setWordSet(List<String> wordSet) {
    this.wordSet = wordSet;
  }

  public void setScoreAccordingTo(String[] wordsToMatch) {
    int match = 0;
    for (String word : wordSet) {
      for (String addressWord : wordsToMatch) {
        if (StringUtils.getLevenshteinDistance(word, addressWord) <= maxDistance) {
          match += 1;
          break;
        }
      }
    }
    score = (float) match / wordsToMatch.length;
  }

  @Override
  public int compareTo(MatchingWindow o) {
    if (score < o.score) {
      return 1;
    }
    if (score > o.score) {
      return -1;
    }
    return 0;
  }

  public int getBegin() {
    return begin;
  }

  public void setBegin(int begin) {
    this.begin = begin;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public void setScore(float score) {
    this.score = score;
  }

  public boolean isScoreAboveThreshold(double threshold) {
    return score >= threshold;
  }

  public void setMatchingText(String matchingText) {
    this.matchingText = matchingText;
  }

  public int getMaxDistance() {
    return maxDistance;
  }

  public void setMaxDistance(int maxDistance) {
    this.maxDistance = maxDistance;
  }
}
