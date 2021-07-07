package tech.skargen.recloud.components.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XChartPanel;
import tech.skargen.recloud.components.gui.interfaces.ICharts;
import tech.skargen.recloud.components.gui.interfaces.IExports;
import tech.skargen.recloud.components.gui.interfaces.IExportsSet;
import tech.skargen.recloud.components.gui.interfaces.ITables;
import tech.skargen.recloud.controllers.interfaces.IExperiment;
import tech.skargen.recloud.controllers.interfaces.IRecloud;
import tech.skargen.recloud.controllers.interfaces.IRecloudSequence;

/** Responsible for Exporting charts to files and creating save folder. */
public class Exports implements IRecloudSequence, IExports, IExportsSet {
  private StringBuilder pathtofolder;
  private BitmapFormat imageFormat;
  private int imageDpi;

  private static Logger _LOG;
  static {
    _LOG = LogManager.getLogger();
  }

  /** Initial fields values. */
  public Exports() {
    this.imageFormat = BitmapFormat.JPG;
    this.imageDpi = 72;
    this.pathtofolder = null;
  }

  /**
   * Choose the preferred exportation image format.
   *
   * @param format Export image format of charts.
   * @param dpi    Image quality value.
   * @return This charts for further configuration.
   */
  @Override
  public IExportsSet image(BitmapFormat format, int dpi) {
    this.imageFormat = format;
    return this;
  }

  /**
   * Location to exports experiment files in.
   *
   * @param pathtofolder Path to folder without the '/' and '\' chars to avoid OS
   *                     restrictions.
   */
  @Override
  public IExportsSet location(String... pathtofolder) {
    String dir = String.join(File.separator, pathtofolder) + File.separator;
    File file = new File(dir);

    try {
      if (!file.exists()) {
        throw new Exception("invalid exportation directory/file -> " + dir);
      }

      this.pathtofolder = new StringBuilder(dir);

    } catch (Exception e) {
      _LOG.error("", e);
    }
    return this;
  }

  /**
   * Create the save/export button.
   *
   * @param frame   Main frame button belongs to.
   * @param recloud Recloud data corrior across experiment components.
   * @return JButton enabling user to export results.
   */
  @Override
  public JButton makeButton(JFrame frame, IRecloud recloud) {
    String title = "Export";
    JButton result = new JButton(title);
    result.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
    result.setName(title);
    result.setEnabled(true);
    result.setVisible(true);

    result.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        String resultsFolder = pathtofolder.toString();
        String path = null;
        while (true) {
          path = JOptionPane.showInputDialog(frame, "Results Path", resultsFolder);

          if (path == null) {
            return;
          }

          if (path.equals(resultsFolder)) {
            File file = new File(path);
            file.mkdirs();
            file.setWritable(true);
            file.setExecutable(true);
            break;
          } else if (!Files.exists(Paths.get(path))) {
            JOptionPane.showMessageDialog(frame,
                "Check Your Path For Any Errors (i.e. missing '/').", "Directory Error",
                JOptionPane.ERROR_MESSAGE);
          } else {
            break;
          }
        }

        Integer[] dpiArray = new Integer[] {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
        Integer dpi = (Integer) JOptionPane.showInputDialog(frame,
            "Cancelling Sets DPI To" + imageDpi + " (Default)", "DPI Options",
            JOptionPane.INFORMATION_MESSAGE, null, dpiArray, imageDpi);
        if (dpi != null) {
          imageDpi = dpi.intValue();
        }
        ITables tables = recloud.getWindow().getTables();
        exportText("setup.txt", path, tables.getEnvironmentTable());
        exportText("results.txt", path, tables.getExperimentTable());
        ICharts charts = recloud.getWindow().getCharts();
        exportCharts(charts.getChartList(), path);
      }

      @Override
      public void mouseEntered(MouseEvent arg0) {}

      @Override
      public void mouseExited(MouseEvent arg0) {}

      @Override
      public void mousePressed(MouseEvent arg0) {}

      @Override
      public void mouseReleased(MouseEvent arg0) {}
    });

    return result;
  }

  /**
   * Export experiment's results string to 'simulation.txt' file.
   *
   * @param string Text to be written into text file.
   * @param path   Folder path.
   */
  public void exportText(String title, String path, String string) {
    try {
      File file = new File(path + title);
      file.createNewFile();
      file.setWritable(true);

      PrintWriter resultsFile = new PrintWriter(file);
      resultsFile.print(string);

      file.setWritable(false);
      file.setReadOnly();
      resultsFile.flush();
      resultsFile.close();
    } catch (IOException e) {
      _LOG.error("can't export text", e);
    }
  }

  /**
   * Export result's charts.
   *
   * @param charts      Charts been populated for the experiment.
   * @param chartFormat Image foramt.
   * @param path        Folder's path.
   *
   */
  public void exportCharts(ObjectArrayList<XChartPanel<CategoryChart>> charts, String path) {
    try {
      for (XChartPanel<CategoryChart> panel : charts) {
        BitmapEncoder.saveBitmapWithDPI(
            panel.getChart(), path + panel.getChart().getTitle(), this.imageFormat, this.imageDpi);
      }

    } catch (IOException e) {
      _LOG.error("can't export charts", e);
    }
  }

  @Override
  public void validate() throws Exception {}

  @Override
  public void init(IRecloud recloud) {
    if (this.pathtofolder == null || this.pathtofolder.length() <= 0) {
      this.pathtofolder = new StringBuilder();
      this.pathtofolder.append(System.getProperty("user.dir"));
      this.pathtofolder.append(File.separator);
    }

    this.pathtofolder.append("Recloud Results");
    this.pathtofolder.append(File.separator);

    // All filesystems allow naming of folders to contain {},[]--
    // Create year and month folder.
    this.pathtofolder.append(new SimpleDateFormat("yyyy-MM").format(new Date()));
    this.pathtofolder.append(File.separator);

    IExperiment ex = recloud.getExperiment();
    String[] names = ex.getSimulationList()
                         .stream()
                         .map(x -> x.getClass().getSimpleName())
                         .distinct()
                         .toArray(String[] ::new);

    // Create experiment folder.
    for (int i = 0; i < names.length; i++) {
      this.pathtofolder.append(names[i]);

      if (i + 1 < names.length) {
        this.pathtofolder.append("_");
      }
    }
    this.pathtofolder.append(" [" + new SimpleDateFormat("hh-mm-ss z").format(new Date()) + "]");
    this.pathtofolder.append(File.separator);
  }

  @Override
  public void finish(IRecloud recloud) {}
}