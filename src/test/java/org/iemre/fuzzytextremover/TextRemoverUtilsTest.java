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
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertTrue;

public class TextRemoverUtilsTest {
  @Test
  public void shouldGetMatchingStringsByJaroWinklerDistance() {
    Set<String> matches = TextRemoverUtils.getMatchingStringsByJwd("This is dummy text helloo worlld.", "hello world", 0.8f);

    assertThat(matches.size(), equalTo(1));
    assertThat(matches.contains("helloo worlld"), equalTo(true));
  }

  @Test
  public void shouldGetApproximatelyMatchingStrings() {
    String string = "Ismail Emre Kartoglu. Ismai Emre. Ismal. My name is Is mail.";

    Set<String> strings = TextRemoverUtils.getMatchingStringsByEditDistance(string, "Ismail", 1);

    assertThat(strings.size(), equalTo(4));
    assertTrue(strings.contains("ismail"));
    assertTrue(strings.contains("ismai"));
    assertTrue(strings.contains("ismal"));
    assertTrue(strings.contains("is mail"));
  }

  @Test
  public void shouldCompletePartialString() {
    String string = "This is a dummy sentence. Hello world. Ismail Emre Kartoglu. Ismai Emre. Ismal. My name is Is mail.";

    String result = TextRemoverUtils.getCompletingString(string, 8, 11);

    assertThat(result, equalTo("a dummy"));
  }

  @Test
  public void shouldNotIncludeParenthesesWhenCompletingPartialString() {
    String string = "This is (a dummy) sentence.";

    String result = TextRemoverUtils.getCompletingString(string, 9, 11);

    assertThat(result, equalTo("a dummy"));
    assertThat(result, not(equalTo("(a dummy")));
  }

  @Test
  public void shouldCheckIfStringIsAlphaNumeric() {
    assertThat(StringUtils.isAlphanumeric("(hello"), equalTo(false));
    assertThat(StringUtils.isAlphanumeric("123hello"), equalTo(true));
  }

  @Test
  public void shouldReturnEmptyCollectionIfSearchWordIsBlank() {
    String string = "Ismail Emre Kartoglu. Ismai Emre. Ismal. My name is Is mail.";

    Set<String> strings = TextRemoverUtils.getMatchingStringsByEditDistance(string, "", 1);

    assertThat(strings.size(), equalTo(0));
  }

  @Test
  public void shouldAvoidOpeningParenthesisAsTheBeginningCharacter() {
    String string = "Ismail (Emre Kartoglu. Ismai Emre. Ismal. My name is Is mail.";

    String result = TextRemoverUtils.getCompletingString(string, 6, 11);

    assertThat(result, equalTo("Emre"));
  }


  @Test
  public void shouldIncludeMiddleParanthesis() {
    String string = "Ismail (07881) 618299. Ismai Emre. Ismal. My name is Is mail.";

    String result = TextRemoverUtils.getCompletingString(string, 8, 16);

    assertThat(result, equalTo("07881) 618299"));
  }

  @Test
  public void shouldFindWindowOfGivenText() {
    String string = "I am Ismail Emre Kartoglu. My address changes. It is now 33 Marmora Road, SE22 0RX, London, UK." +
            " This is some extra text.";

    String address = "33, London, Marmora Road, SE22 0RX";

    MatchingWindow window = TextRemoverUtils.getMatchingWindowsAboveThresholdByEditDistance(string, address, 0.5f, 1).get(0);

    assertThat(window.getMatchingText(), equalTo("33 Marmora Road, SE22 0RX, London,"));

    assertThat(string.substring(window.getBegin(), window.getEnd()), equalTo("33 Marmora Road, SE22 0RX, London,"));
  }

  @Test
  public void shouldFindWindowOfGivenTextWhenWindowIsAtTheBorder() {
    String string = "I am Ismail Emre Kartoglu. My address changes. It is now 33 Marmora Road, SE22 0RX, London";

    String address = "33, London, Marmora Road, SE22 0RX";

    MatchingWindow window = TextRemoverUtils.getMatchingWindowsAboveThresholdByEditDistance(string, address, 0.5f, 1).get(0);

    assertThat(window.getMatchingText(), equalTo("33 Marmora Road, SE22 0RX, London"));

    assertThat(string.substring(window.getBegin(), window.getEnd()), equalTo("33 Marmora Road, SE22 0RX, London"));

    assertThat(window.isScoreAboveThreshold(0.6f), equalTo(true));
  }



}
