package tech.skargen.skartools;

import java.util.Arrays;

/**
 * Tables styler and builder by github.com/cypherskar.
 */
public class STable {
  public enum EntryStyle { Verticle, Horizontle }
  private EntryStyle tableMode;

  private char left;
  private char fill;
  private char right;
  private SText stext;
  private StringBuilder[] entries;
  private int[] entriesspaces;
  private int fallbackspace;
  private int entriesIndex;
  private int cellIndex;
  private int rowIndex;

  /**
   * Creates a new instance to build tables.
   */
  public STable() {
    this.stext = SText.getInstance();
    this.clearTable();
  }

  //#region low-level
  /**
   * Get the formatter used for all texts by this class.
   * @return Formatter element.
   */
  public SText stext() {
    return this.stext;
  }

  /** Clears table's attributes. */
  public void clearTable() {
    this.tableMode = null;
    this.entries = null;
    this.entriesspaces = null;
  }

  @Override
  protected void finalize() throws Throwable {
    this.clearTable();
    this.stext = null;
    super.finalize();
  }
  //#endregion

  //#region mid-level
  /**
   * Initiates this instance to work on a new table, horizontal entry style refers to the first
   * entry style.
   * @param mode Preferable table style.
   * @param entries Initial number of main table headers.
   * @param fallbackspace Text space in case non is provided.
   * @param left Placed before entry/cell.
   * @param fill Placed in empty spaces.
   * @param right Placed after entry/cell.
   */
  public void newTable(
      EntryStyle mode, int entries, int fallbackspace, char left, char fill, char right) {
    this.tableMode = mode;
    switch (mode) {
      case Horizontle:
        this.entries = new StringBuilder[entries];
        this.entriesspaces = null;
        break;

      case Verticle:
      default:
        this.entries = new StringBuilder[2];
        this.entriesspaces = new int[entries];
        break;
    }
    this.left = left;
    this.fill = fill;
    this.right = right;
    this.fallbackspace = fallbackspace;
    this.entriesIndex = -1;
    this.cellIndex = -1;
    this.rowIndex = 1;
  }

  /**
   * Append a new table header that will have the sub cells follow its style, use {@link
   * #style()} to customize entry/cells looks.
   * @param align Alignment of text, values are - 1, 0 and 1.
   * @param text Main info to be placed.
   */
  public void addEntry(int align, String text) {
    this.addEntry(align, this.fallbackspace, text);
  }

  /**
   * Append a new table header that will have the sub cells follow its style, use {@link
   * #style()} to customize entry/cells looks.
   * @param align Alignment of text, values are - 1, 0 and 1.
   * @param infoleft To be placed before info text.
   * @param infofill To be placed between info text.
   * @param inforight To be placed after info text.
   * @param text Main info to be placed.
   * @param info Additional info.
   */
  public void addEntry(int align, String text, Object... info) {
    this.addEntry(align, this.fallbackspace, text, info);
  }

  /**
   * Append a new table header that will have the sub cells follow its style, use {@link
   * #style()} to customize entry/cells looks.
   * @param align Alignment of text, values are - 1, 0 and 1.
   * @param spce Space of that enry allows for text.
   * @param infoleft To be placed before info text.
   * @param infofill To be placed between info text.
   * @param inforight To be placed after info text.
   * @param text Main info to be placed.
   * @param info Additional info.
   */
  public void addEntry(int align, int space, String text, Object... info) {
    if (this.tableMode == null) {
      return;
    }

    // check if next entry is within array bounds, if not expand
    this.entriesIndex++;
    switch (this.tableMode) {
      case Horizontle:
        if (this.entriesIndex >= this.entries.length) {
          this.entries = Arrays.copyOf(this.entries, this.entriesIndex + 1);
        }
        // add new entry style to list; in case of horizontle, ensure they all have same text space
        space = fallbackspace;
        break;

      case Verticle:
      default:
        if (this.entriesIndex >= this.entriesspaces.length) {
          this.entriesspaces = Arrays.copyOf(this.entriesspaces, this.entriesspaces.length + 1);
        }
        this.entriesspaces[this.entriesIndex] = space;
        break;
    }

    // create beautified text
    StringBuilder result =
        this.stext.wrap(align, space, this.left, this.fill, '\0', this.right, text, info);

    // store entry syle
    switch (this.tableMode) {
      case Horizontle:
        this.entries[this.entriesIndex] = result;
        break;

      case Verticle:
      default:
        if (this.entries[0] == null) {
          this.entries[0] = result;
        } else {
          this.entries[0].append(result);
        }
        break;
    }
  }

  /**
   * Append a new cell that follows the corrosponding entry's style.
   * @param align Alignment of text, values are -1, 0 and 1.
   * @param text Main info to be placed.
   * @param info Additional info.
   */
  public void addCell(int align, String text, Object... info) {
    if (this.tableMode == null) {
      return;
    }
    if (this.entries == null || this.entries.length <= 0) {
      return;
    }

    // adding next cell kto table and ensure that in verticle a new row is created
    int space;
    this.cellIndex++;
    switch (this.tableMode) {
      case Horizontle:
        if (this.cellIndex >= this.entries.length) {
          this.rowIndex++;
          this.cellIndex = 0;
        }
        space = this.fallbackspace;
        break;

      case Verticle:
      default:
        if (this.cellIndex >= this.entriesspaces.length) {
          this.rowIndex++;
          this.cellIndex = 0;
        }
        if (this.rowIndex >= this.entries.length) {
          this.entries = Arrays.copyOf(this.entries, this.rowIndex + 1);
        }
        space = this.entriesspaces[this.cellIndex];
        break;
    }

    // create beautified text
    StringBuilder result =
        this.stext.wrap(align, space, this.left, this.fill, '\0', this.right, text, info);

    switch (this.tableMode) {
      case Horizontle:
        this.entries[this.cellIndex].append(result);
        break;

      case Verticle:
      default:
        if (this.entries[this.rowIndex] == null) {
          this.entries[this.rowIndex] = result;
        } else {
          this.entries[this.rowIndex].append(result);
        }
        break;
    }
  }

  /**
   * Simplest string carrying resulting table.
   * @param align Placement of text to sequence, values are -1, 0 and 1.
   * @param repeatable To be placed repeatedly.
   * @param corner To be placed in the corners of the footer.
   * @param text To create sequence around.
   * @return Table string.
   */
  public StringBuilder endTable(char repeatable, char corner) {
    if (this.entries == null || this.entries.length <= 0) {
      return null;
    }

    StringBuilder result = new StringBuilder();
    int space = this.stext.getLongestSubString(this.entries[0], SText.NEWLINE, false);
    result.append(this.stext.sequenceFormat(space, repeatable, corner));
    result.append(SText.NEWLINE);
    for (int i = 0; i < entries.length; i++) {
      if (this.entries[i] == null) {
        continue;
      }
      result.append(this.entries[i]);
      if (i + 1 < this.entries.length) {
        if (this.entries[i + 1] != null) {
          result.append(SText.NEWLINE);
        }
      }
    }
    return result;
  }

  /**
   * finalize the table and add a title with simple beautification characters to it depending on
   * the style.
   * @param align Placement of text to sequence, values are -1, 0 and 1.
   * @param repeatable To be placed repeatedly.
   * @param corner To be placed in the corners of the footer.
   * @param title To place at top of table.
   * @param info Extra title information.
   * @return Formatted table.
   */
  public StringBuilder endTable(
      int align, char repeatable, char corner, String title, Object... info) {
    if (this.tableMode == null) {
      return null;
    }
    title = this.stext.stripOfChar(title, Character.MIN_VALUE);
    boolean appendleft = this.stext.validChar(this.left);
    boolean appendright = this.stext.validChar(this.right);

    int space = this.stext.getLongestSubString(this.entries[0], SText.NEWLINE, false);
    int footerspace = space;
    if (appendleft) {
      footerspace++;
    }
    if (appendright) {
      footerspace++;
    }

    StringBuilder footer = this.stext.sequenceFormat(footerspace, repeatable, corner);
    StringBuilder lasttable =
        this.stext.wrap(align, space, this.left, this.fill, '\0', this.right, title, info);
    lasttable.append(SText.NEWLINE);
    lasttable.append(footer);
    lasttable.append(SText.NEWLINE);

    for (int i = 0; i < entries.length; i++) {
      if (this.entries[i] == null) {
        continue;
      }
      if (appendleft) {
        lasttable.append(this.left);
      }
      lasttable.append(this.entries[i]);
      if (appendright) {
        lasttable.append(this.right);
      }
      if (i + 1 < this.entries.length) {
        if (this.entries[i + 1] != null) {
          lasttable.append(SText.NEWLINE);
        }
      }
    }
    lasttable.append(SText.NEWLINE);
    lasttable.append(footer);
    lasttable.append(SText.NEWLINE);

    return lasttable;
  }

  //#endregion
}
