/*
 * SonarQube
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.api.config.internal;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;

import static java.util.function.UnaryOperator.identity;

public class MultivalueProperty {
  private MultivalueProperty() {
    // prevents instantiation
  }

  public static String[] parseAsCsv(String key, String value) {
    return parseAsCsv(key, value, identity());
  }

  public static String[] parseAsCsv(String key, String value, UnaryOperator<String> valueProcessor) {
    String cleanValue = MultivalueProperty.trimFieldsAndRemoveEmptyFields(value);
    List<String> result = new ArrayList<>();
    try (CSVParser csvParser = CSVFormat.RFC4180.builder()
        .setSkipHeaderRecord(true)
        .setIgnoreEmptyLines(true)
        .setIgnoreSurroundingSpaces(true)
        .build()
      .parse(new StringReader(cleanValue))) {
      List<CSVRecord> records = csvParser.getRecords();
      if (records.isEmpty()) {
        return ArrayUtils.EMPTY_STRING_ARRAY;
      }
      processRecords(result, records, valueProcessor);
      return result.toArray(new String[result.size()]);
    } catch (IOException | UncheckedIOException e) {
      throw new IllegalStateException("Property: '" + key + "' doesn't contain a valid CSV value: '" + value + "'", e);
    }
  }

  /**
   * In most cases we expect a single record. <br>Having multiple records means the input value was splitted over multiple lines (this is common in Maven).
   * For example:
   * <pre>
   *   &lt;sonar.exclusions&gt;
   *     src/foo,
   *     src/bar,
   *     src/biz
   *   &lt;sonar.exclusions&gt;
   * </pre>
   * In this case records will be merged to form a single list of items. Last item of a record is appended to first item of next record.
   * <p>
   * This is a very curious case, but we try to preserve line break in the middle of an item:
   * <pre>
   *   &lt;sonar.exclusions&gt;
   *     a
   *     b,
   *     c
   *   &lt;sonar.exclusions&gt;
   * </pre>
   * will produce ['a\nb', 'c']
   */
  private static void processRecords(List<String> result, List<CSVRecord> records, Function<String, String> valueProcessor) {
    for (CSVRecord csvRecord : records) {
      Iterator<String> it = csvRecord.iterator();
      if (!result.isEmpty()) {
        String next = it.next();
        if (!next.isEmpty()) {
          int lastItemIdx = result.size() - 1;
          String previous = result.get(lastItemIdx);
          if (previous.isEmpty()) {
            result.set(lastItemIdx, valueProcessor.apply(next));
          } else {
            result.set(lastItemIdx, valueProcessor.apply(previous + "\n" + next));
          }
        }
      }
      it.forEachRemaining(s -> {
        String apply = valueProcessor.apply(s);
        result.add(apply);
      });
    }
  }

  /**
   * Removes the empty fields from the value of a multi-value property from empty fields, including trimming each field.
   * <p>
   * Quotes can be used to prevent an empty field to be removed (as it is used to preserve empty spaces).
   * <ul>
   *    <li>{@code "" => ""}</li>
   *    <li>{@code " " => ""}</li>
   *    <li>{@code "," => ""}</li>
   *    <li>{@code ",," => ""}</li>
   *    <li>{@code ",,," => ""}</li>
   *    <li>{@code ",a" => "a"}</li>
   *    <li>{@code "a," => "a"}</li>
   *    <li>{@code ",a," => "a"}</li>
   *    <li>{@code "a,,b" => "a,b"}</li>
   *    <li>{@code "a,   ,b" => "a,b"}</li>
   *    <li>{@code "a,\"\",b" => "a,b"}</li>
   *    <li>{@code "\"a\",\"b\"" => "\"a\",\"b\""}</li>
   *    <li>{@code "\"  a  \",\"b \"" => "\"  a  \",\"b \""}</li>
   *    <li>{@code "\"a\",\"\",\"b\"" => "\"a\",\"\",\"b\""}</li>
   *    <li>{@code "\"a\",\"  \",\"b\"" => "\"a\",\"  \",\"b\""}</li>
   *    <li>{@code "\"  a,,b,c  \",\"d \"" => "\"  a,,b,c  \",\"d \""}</li>
   *    <li>{@code "a,\"  \",b" => "ab"]}</li>
   * </ul>
   */
  static String trimFieldsAndRemoveEmptyFields(String str) {
    char[] chars = str.toCharArray();
    char[] res = new char[chars.length];
    ParsingState state = new ParsingState();
    for (int i = 0; i < chars.length; i++) {
      boolean isSeparator = chars[i] == ',';
      if (!state.inQuotes && isSeparator) {
        handleSeparator(state, res);
      } else {
        handleFieldChar(state, res, chars[i]);
      }
    }
    // inQuotes can only be true at this point if quotes are unbalanced
    if (!state.inQuotes) {
      // trim end of str
      state.resI = retroTrim(res, state.resI);
    }
    return new String(res, 0, state.resI);
  }

  private static void handleSeparator(ParsingState state, char[] res) {
    // exiting field (may already be unset)
    state.inField = false;
    if (state.resI > 0) {
      state.resI = retroTrim(res, state.resI);
    }
  }

  private static void handleFieldChar(ParsingState state, char[] res, char c) {
    boolean isTrimmed = !state.inQuotes && istrimmable(c);
    if (isTrimmed && !state.inField) {
      // we haven't meet any non trimmable char since the last separator yet
      return;
    }

    if (isEscapeChar(c)) {
      state.inQuotes = !state.inQuotes;
    }

    // add separator as we already had one field
    if (!state.inField && state.resI > 0) {
      res[state.resI] = ',';
      state.resI++;
    }

    // register in field (may already be set)
    state.inField = true;
    // copy current char
    res[state.resI] = c;
    state.resI++;
  }

  private static boolean isEscapeChar(char aChar) {
    return aChar == '"';
  }

  private static boolean istrimmable(char aChar) {
    return aChar <= ' ';
  }

  /**
   * Mutable parsing state shared by {@link #trimFieldsAndRemoveEmptyFields(String)} and its helper methods.
   */
  private static class ParsingState {
    /*
     * set when reading the first non trimmable char after a separator char (or the beginning of the string)
     * unset when reading a separator
     */
    boolean inField = false;
    boolean inQuotes = false;
    int resI = 0;
  }

  /**
   * Reads from index {@code resI} to the beginning into {@code res} looking up the location of the trimmable char with
   * the lowest index before encountering a non-trimmable char.
   * <p>
   * This basically trims {@code res} from any trimmable char at its end.
   *
   * @return index of next location to put new char in res
   */
  private static int retroTrim(char[] res, int resI) {
    int i = resI;
    while (i >= 1) {
      if (!istrimmable(res[i - 1])) {
        return i;
      }
      i--;
    }
    return i;
  }

}
