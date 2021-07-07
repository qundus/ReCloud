package tech.skargen.recloud.components.gui;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import tech.skargen.recloud.components.gui.interfaces.IConsoleToSwing;
import tech.skargen.recloud.components.gui.interfaces.IConsoleToSwingSet;

/**
 * Creates an outputstream that is attached to a swing element.
 *
 * @see javax.swing.JTextArea
 * @see javax.swing.JScrollPane
 */
public class ConsoleToSwing extends OutputStream implements IConsoleToSwing, IConsoleToSwingSet {
  private Color fgColor;
  private Color bgColor;

  protected JTextArea textArea;
  protected JScrollPane scrollPane;

  /**
   * Constructor.
   */
  public ConsoleToSwing() {
    this.fgColor = Color.BLACK;
    this.bgColor = Color.WHITE;

    this.textArea = new JTextArea();
    this.textArea.setFont(new Font(Font.DIALOG_INPUT, Font.PLAIN, 30));
    this.textArea.setAutoscrolls(true);
    this.textArea.setEditable(false);
    this.textArea.setBackground(this.bgColor);
    this.textArea.setForeground(this.fgColor);
    this.scrollPane = new JScrollPane(this.textArea);
    this.scrollPane.setAutoscrolls(true);
    this.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    this.scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    this.scrollPane.getVerticalScrollBar().setUnitIncrement(16);
  }

  @Override
  public JScrollPane getScrollPane() {
    return this.scrollPane;
  }

  @Override
  public String getTextArea() {
    return this.textArea.getText();
  }

  @Override
  public IConsoleToSwingSet colors(Color background, Color foreground) {
    this.bgColor = background;
    this.fgColor = foreground;
    return this;
  }

  /**
   * Writes the specified byte as a character to the javax.swing.JTextArea.
   *
   * @param b The byte to be written as character to the JTextArea.
   */
  public void write(int b) throws IOException {
    // append the data as characters to the JTextArea control
    this.textArea.append(String.valueOf((char) b));
    this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
  }
}
