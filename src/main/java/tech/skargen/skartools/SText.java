package tech.skargen.skartools;

/**
 * The methods are built to offer total accessability to all levels of formatting a text, and focus
 * on being performant and light by using primitive datatypes (char, int..) and benchmark tested
 * methods (StringBuilder.append(), String.join()..).
 * they're classified according to their functionality and basic nature. To use any of the
 * methods it's crucial to understand what each level specialize in:
 * <ol>
 * <li>Low-levels are the foundation of all the higher levels.
 * <li>Mid-levels offer unique/easy ways of formatting texts.
 * <li>High-levels are considered styling components for constructing text/s.
 * </ol>
 * Important info about the levels:
 * <ul>
 * <li>Levels are constructed from the lower methods to them.
 * <li>Low-levels are named format(), {name}Format().
 * <li>Mid-levels are named {name}Style().
 * <li>High-levels are named according to their specific task.
 * </ul>
 *
 * @author github.com/cypherskar
 */
public class SText {
  /** Alignment/Placement of intended text. */
  public final int ALIGN_L = -1;
  /** Alignment/Placement of intended text. */
  public final int ALIGN_C = 0;
  /** Alignment/Placement of intended text. */
  public final int ALIGN_R = 1;

  private static SText instance;
  public final static String NEWLINE;

  static {
    NEWLINE = System.lineSeparator();
  }

  /**
   * Prepare an instance to format texts, use SText.getInstance().
   */
  private SText() {}

  //#region general use
  /**
   * Used to get global instance of this class.
   * @return SText global instance.
   */
  public static SText getInstance() {
    if (instance == null) {
      instance = new SText();
    }
    return instance;
  }

  /**
   * Check if character is acceptable.
   * @param c Character to be checked.
   * @return True if character is accepted.
   */
  public boolean validChar(char c) {
    switch (c) {
      case Character.MIN_VALUE:
      case Character.MIN_SURROGATE:
      case Character.MIN_LOW_SURROGATE:
      // case Character.MIN_HIGH_SURROGATE:
      case Character.MAX_SURROGATE:
      // case Character.MAX_LOW_SURROGATE:
      case Character.MAX_HIGH_SURROGATE:
        return false;

      default:
        return true;
    }
  }

  /**
   * Removes all weird characters from text.
   * @param text To perform action on.
   */
  public void cleanText(StringBuilder text) {
    if (text == null) {
      return;
    }
    final int textlength = text.length();
    StringBuilder result = new StringBuilder(textlength);
    for (int i = 0; i < textlength; i++) {
      char c = text.charAt(i);
      if (this.validChar(c)) {
        result.append(c);
      }
    }
    text.setLength(0);
    text.append(result);
  }

  /**
   * Removes all weird characters from text.
   * @param text To perform action on.
   * @return Cleaned up text.
   */
  public String cleanText(String text) {
    if (text == null) {
      return text;
    }
    char[] arr = text.toCharArray();
    StringBuilder result = new StringBuilder(arr.length);
    text = null;
    for (char c : arr) {
      if (this.validChar(c)) {
        result.append(c);
      }
    }
    return result.toString();
  }

  /**
   * Remove a character from a string.
   * @param text To perform action on.
   * @param character To be removed.
   */
  public void stripOfChar(StringBuilder text, char character) {
    if (text == null || text.indexOf("" + character) == -1) {
      return;
    }
    final int textlength = text.length();
    StringBuilder result = new StringBuilder(textlength);
    for (int i = 0; i < textlength; i++) {
      char c = text.charAt(i);
      if (c != character) {
        result.append(c);
      }
    }
    text.setLength(0);
    text.append(result);
  }

  /**
   * Remove a character from a string.
   * @param text To perform action on.
   * @param character To be removed.
   * @return Text cleaned of the passed character.
   */
  public String stripOfChar(String text, char character) {
    if (text == null || text.indexOf("" + character) == -1) {
      return text;
    }
    char[] arr = text.toCharArray();
    StringBuilder result = new StringBuilder(arr.length);
    text = null;
    for (char c : arr) {
      if (c != character) {
        result.append(c);
      }
    }
    return result.toString();
  }

  /**
   * Calculater of the longest sub-string in a string defined by the splitor.
   * @param splittor String used to separate lines.
   * @param text Text to consider.
   * @param checknonvalid Checks string for non valid characters like Character.MIN_VALUE.
   * @return Longest sub-string's number of characters.
   */
  public int getLongestSubString(StringBuilder text, String splittor, boolean checknonvalid) {
    int longestline = 0;
    int startindex = 0;
    int tempindex = 0;
    int nonvalidchars = 0;
    final int textlength = text.length();
    while (startindex < textlength) {
      nonvalidchars = 0;
      tempindex = text.indexOf(splittor, startindex);
      if (tempindex <= -1) {
        if (checknonvalid) {
          for (int i = startindex; i < textlength; i++) {
            if (this.validChar(text.charAt(i))) {
              nonvalidchars++;
            }
          }
        }
        if (longestline < (textlength - startindex) - nonvalidchars) {
          longestline = textlength - startindex;
          longestline -= nonvalidchars;
        }
        startindex = textlength + 1;
      } else {
        if (checknonvalid) {
          for (int i = startindex; i < tempindex; i++) {
            if (this.validChar(text.charAt(i))) {
              nonvalidchars++;
            }
          }
        }
        if (longestline < (tempindex - startindex) - nonvalidchars) {
          longestline = tempindex - startindex;
          longestline -= nonvalidchars;
        }
        startindex = tempindex + 1;
      }
    }
    return longestline;
  }

  /**
   * Calculater of the longest sub-string in a string defined by the splitor.
   * @param splittor String used to separate lines.
   * @param text Text to consider.
   * @param checknonvalid Checks string for non valid characters like Character.MIN_VALUE.
   * @return Longest sub-string's number of characters.
   */
  public int getLongestSubString(String text, String splittor, boolean checknonvalid) {
    int longestline = 0;
    int startindex = 0;
    int tempindex = 0;
    int nonvalidchars = 0;
    final int textlength = text.length();
    while (startindex < textlength) {
      nonvalidchars = 0;
      tempindex = text.indexOf(splittor, startindex);
      if (tempindex <= -1) {
        if (checknonvalid) {
          for (int i = startindex; i < textlength; i++) {
            if (this.validChar(text.charAt(i))) {
              nonvalidchars++;
            }
          }
        }
        if (longestline < (textlength - startindex) - nonvalidchars) {
          longestline = textlength - startindex;
          longestline -= nonvalidchars;
        }
        startindex = textlength + 1;
      } else {
        if (checknonvalid) {
          for (int i = startindex; i < tempindex; i++) {
            if (this.validChar(text.charAt(i))) {
              nonvalidchars++;
            }
          }
        }
        if (longestline < (tempindex - startindex) - nonvalidchars) {
          longestline = tempindex - startindex;
          longestline -= nonvalidchars;
        }
        startindex = tempindex + 1;
      }
    }
    return longestline;
  }

  /**
   * Transform any object to string, multi-dimensional arrays not supported.
   * @param obj Object to extract as string.
   * @return Object as string.
   */
  public String getObjectAsString(Object obj) {
    if (obj == null) {
      return null;
    }

    if (obj.getClass().isArray()) {
      // supports only 1d arrays
      switch (obj.getClass().getComponentType().toString()) {
        case "boolean":
          return java.util.Arrays.toString(boolean[].class.cast(obj));
        case "char":
          return java.util.Arrays.toString(char[].class.cast(obj));
        case "byte":
          return java.util.Arrays.toString(byte[].class.cast(obj));
        case "short":
          return java.util.Arrays.toString(short[].class.cast(obj));
        case "int":
          return java.util.Arrays.toString(int[].class.cast(obj));
        case "long":
          return java.util.Arrays.toString(long[].class.cast(obj));
        case "float":
          return java.util.Arrays.toString(float[].class.cast(obj));
        case "double":
          return java.util.Arrays.toString(double[].class.cast(obj));
        default:
          return java.util.Arrays.toString(Object[].class.cast(obj));
      }
    }

    return String.valueOf(obj);
  }
  //#endregion

  //#region low-levels
  /**
   * Format object to be within space and fill empty space with preferred character.
   * @param align Alignment of text, values are -1, 0 and 1.
   * @param space Space text should occupy.
   * @param fill Padding/empty-space filler character.
   * @param text Text to be placed.
   * @return Formatted text.
   */
  public StringBuilder format(int align, int space, char fill, Object obj) {
    if (obj == null) {
      return null;
    }

    String text = this.getObjectAsString(obj);
    text = this.stripOfChar(text, Character.MIN_VALUE);
    obj = null;

    final int textlength = text.length();
    if (space <= textlength) {
      space = Math.max(0, space);
      return new StringBuilder(text.subSequence(0, space));
    }

    StringBuilder result = new StringBuilder(space + textlength);
    space -= textlength;
    if (!this.validChar(fill)) {
      fill = ' ';
    }
    for (int i = 0, half = space / 2; i < space; i++) {
      switch (align) {
        case ALIGN_C:
          if (i == half) {
            result.append(text);
            text = null;
          }
          result.append(fill);
          break;

        case ALIGN_R:
          result.append(fill);
          if (i + 1 >= space) {
            result.append(text);
            text = null;
          }
          break;
        case ALIGN_L:
        default:
          if (i == 0) {
            result.append(text);
            text = null;
          }
          result.append(fill);
          break;
      }
    }

    return result;
  }

  /**
   * Format text string to be within space and fill empty space with preferred character.
   * @param align Alignment of text, values are -1, 0 and 1.
   * @param space Space text should occupy.
   * @param fill Padding/empty-space filler character.
   * @param text Text to be placed.
   * @return Formatted text.
   */
  public StringBuilder format(int align, int space, char fill, String text) {
    if (text == null) {
      return null;
    }
    text = this.stripOfChar(text, Character.MIN_VALUE);

    StringBuilder result;
    final int textlength = text.length();
    if (space == textlength) {
      result = new StringBuilder(text);
    } else if (space < textlength) {
      space = Math.max(0, space);
      if (align == ALIGN_L) {
        result = new StringBuilder(text.subSequence(0, space));
      } else {
        result = new StringBuilder(text.subSequence(textlength - space, textlength));
      }
    } else {
      result = new StringBuilder(space + textlength);
      space -= textlength;
      if (!this.validChar(fill)) {
        fill = ' ';
      }
      for (int i = 0, half = space / 2; i < space; i++) {
        switch (align) {
          case ALIGN_C:
            if (i == half) {
              result.append(text);
            }
            result.append(fill);
            break;

          case ALIGN_R:
            result.append(fill);
            if (i + 1 >= space) {
              result.append(text);
            }
            break;
          case ALIGN_L:
          default:
            if (i == 0) {
              result.append(text);
            }
            result.append(fill);
            break;
        }
      }
    }

    return result;
  }

  /**
   * Format text string to be within space and fill empty space with preferred character.
   * @param align Alignment of text, values are -1, 0 and 1.
   * @param space Space text should occupy.
   * @param fill Padding/empty-space filler character.
   * @param text Text to be altered.
   * @return Formatted text.
   */
  public void format(int align, int space, char fill, StringBuilder text) {
    if (text == null) {
      return;
    }
    this.stripOfChar(text, Character.MIN_VALUE);

    final int textlength = text.length();
    if (space == textlength) {
      return;
    } else if (space < textlength) {
      space = Math.max(0, space);
      String toappend;
      if (align == ALIGN_L) {
        toappend = text.substring(0, space);
      } else {
        // text.delete(space / 2, textlength - ((space % 2 == 0) ? space / 2 : (space / 2) + 1));
        toappend = text.substring(textlength - space, textlength);
      }
      text.setLength(0);
      text.append(toappend);
    } else {
      StringBuilder result = null;
      space -= textlength;
      if (!this.validChar(fill)) {
        fill = ' ';
      }
      if (align != ALIGN_L) {
        result = new StringBuilder(space + textlength);
      }
      for (int i = 0, half = space / 2; i < space; i++) {
        switch (align) {
          case ALIGN_C:
            if (i == half) {
              result.append(text);
            }
            result.append(fill);
            break;

          case ALIGN_R:
            result.append(fill);
            break;
          case ALIGN_L:
          default:
            text.append(fill);
            break;
        }
      }
      if (align != ALIGN_L) {
        if (align == ALIGN_R) {
          result.append(text);
        }
        text.setLength(0);
        text.append(result);
      }
    }
  }

  /**
   * Convert objects to a string surrounded by before and after characters and split by the
   * splitor.
   * @param fill To act a separator between array objects.
   * @param objects varargs of Object.
   * @return Formatted text.
   */
  public StringBuilder objectsFormat(char fill, Object... objects) {
    StringBuilder result = new StringBuilder();
    boolean fillisvalid = this.validChar(fill);
    int i = 0;
    for (Object obj : objects) {
      result.append(this.getObjectAsString(obj));
      i++;
      if (i < objects.length && fillisvalid) {
        result.append(fill);
      }
    }
    this.stripOfChar(result, Character.MIN_VALUE);
    return result;
  }

  /**
   * Generates a sequence of repeatable character wraped between corners.
   * @param space Space footer should occupy.
   * @param repeatable Padding/empty-space filler character.
   * @param corner To be placed at start and end of footer.
   * @return Formatted footer.
   */
  public StringBuilder sequenceFormat(int space, char repeatable, char corner) {
    StringBuilder result = new StringBuilder();
    space -= 2;
    if (!this.validChar(repeatable)) {
      repeatable = Character.MIN_HIGH_SURROGATE;
    }
    if (!this.validChar(corner)) {
      corner = repeatable;
    }
    result.append(corner);
    for (int i = 0; i < space; i++) {
      result.append(repeatable);
    }
    result.append(corner);
    return result;
  }

  /**
   * Calculates number division for when a number is too big or too small so it would be better
   * written with the number suffix format for better clarification. So, 1000KB will be 1MB.
   * @param num Number to be considered.
   * @return Formatted number.
   */
  public String storageFormat(int num) {
    // if (count < 1000) return "" + count;
    int exp = (int) (Math.log(num) / Math.log(1000));
    return String.format("%-0.1f %-c", (1.0 * num) / Math.pow(1000, exp), " KMBTPE".charAt(exp));
  }
  //#endregion

  //#region mid-levels
  /**
   * Formats text in space given with padding and wrapping characters with additional info.
   * @param align Alignment of text, values are -1, 0 and 1.
   * @param space Space text should occupy.
   * @param left To be placed before text.
   * @param fill Padding/empty-space filler character.
   * @param split To be placed between info objects.
   * @param right To be placed after text.
   * @param text Text to be placed.
   * @param info Additional info array.
   * @return Formatted text.
   */
  public StringBuilder wrap(int align, int space, char left, char fill, char split, char right,
      String text, Object... info) {
    if (text == null) {
      return null;
    }
    text = this.stripOfChar(text, Character.MIN_VALUE);

    StringBuilder result;
    if (info == null || info.length <= 0) {
      result = this.format(align, space, fill, text);
    } else {
      switch (align) {
        case ALIGN_C:
        case ALIGN_R:
          result = new StringBuilder(text);
          result.append(this.objectsFormat(split, info));
          this.format(align, space, fill, result);
          break;

        case ALIGN_L:
        default:
          StringBuilder infostring = this.objectsFormat(split, info);
          final int textlength = text.length();
          final int infospace = space - textlength;
          if (infospace >= 0) {
            result = new StringBuilder(text);
            this.format(1, infospace, fill, infostring);
            result.append(infostring);
          } else {
            result = this.format(align, textlength - Math.abs(infospace), fill, text);
          }
          break;
      }
    }

    if (this.validChar(left)) {
      result.insert(0, left);
    }

    if (this.validChar(right)) {
      result.append(right);
    }

    return result;
  }

  /**
   * Formats text in space given with padding and wrapping characters with additional info.
   * @param align Alignment of text, values are -1, 0 and 1.
   * @param space Space text should occupy.
   * @param left To be placed before text.
   * @param fill Padding/empty-space filler character.
   * @param split To be placed between info objects.
   * @param right To be placed after text.
   * @param text Text to be altered.
   * @param info Additional info array.
   * @return Formatted text.
   */
  public void wrap(int align, int space, char left, char fill, char split, char right,
      StringBuilder text, Object... info) {
    if (text == null) {
      return;
    }
    this.stripOfChar(text, Character.MIN_VALUE);

    if (info == null || info.length <= 0) {
      this.format(align, space, fill, text);
    } else {
      switch (align) {
        case ALIGN_C:
        case ALIGN_R:
          text.append(this.objectsFormat(split, info));
          this.format(align, space, fill, text);
          break;

        case ALIGN_L:
        default:
          StringBuilder infostring = this.objectsFormat(split, info);
          final int textlength = text.length();
          final int infospace = space - textlength;
          if (infospace >= 0) {
            // result = new StringBuilder(text);
            this.format(align, infospace, fill, infostring);
            text.append(infostring);
          } else {
            this.format(align, textlength - Math.abs(infospace), fill, text);
          }
          break;
      }
    }

    if (this.validChar(left)) {
      text.insert(0, left);
    }

    if (this.validChar(right)) {
      text.append(right);
    }
  }

  /**
   * Wraps text with a sequence of characters according to the longest line in text.
   * @param align Placement of text to sequence, values are -1, 0 and 1.
   * @param repeatable To be placed repeatedly.
   * @param corner To be placed in the corners of the footer.
   * @param text To create sequence around.
   * @return Text beautified by the characters provided.
   */
  public StringBuilder sequenceWrap(int align, char repeatable, char corner, String text) {
    if (text == null) {
      return null;
    }
    text = this.stripOfChar(text, Character.MIN_VALUE);

    final int longestline = getLongestSubString(text, NEWLINE, false);
    StringBuilder result;

    switch (align) {
      case ALIGN_C:
        result = sequenceFormat(longestline, repeatable, corner);
        result.append(NEWLINE);
        result.append(text);
        result.append(NEWLINE);
        result.append(result.subSequence(0, longestline));
        break;

      case ALIGN_R:
        result = sequenceFormat(longestline, repeatable, corner);
        result.append(NEWLINE);
        result.append(text);
        break;

      case ALIGN_L:
      default:
        result = new StringBuilder(text);
        result.append(NEWLINE);
        result.append(sequenceFormat(longestline, repeatable, corner));
        break;
    }

    return result;
  }

  /**
   * Wraps text with a sequence of characters according to the longest line in text.
   * @param align Placement of text to sequence, values are -1, 0 and 1.
   * @param repeatable To be placed repeatedly.
   * @param corner To be placed in the corners of the footer.
   * @param text To add sequence to.
   * @return Text beautified by the characters provided.
   */
  public void sequenceWrap(int align, char repeatable, char corner, StringBuilder text) {
    if (text == null) {
      return;
    }
    this.stripOfChar(text, Character.MIN_VALUE);

    final int longestline = getLongestSubString(text, NEWLINE, false);
    StringBuilder result;

    switch (align) {
      case ALIGN_C:
        result = sequenceFormat(longestline, repeatable, corner);
        result.append(NEWLINE);
        result.append(text);
        result.append(NEWLINE);
        result.append(result.subSequence(0, longestline));
        break;

      case ALIGN_R:
        result = sequenceFormat(longestline, repeatable, corner);
        result.append(NEWLINE);
        result.append(text);
        break;

      case ALIGN_L:
      default:
        result = new StringBuilder(text);
        result.append(NEWLINE);
        result.append(sequenceFormat(longestline, repeatable, corner));
        break;
    }

    text.setLength(0);
    text.append(result);
  }
  //#endregion
}
