package tech.cypherskar.cloudex.components.window;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import tech.cypherskar.cloudex.components.cloudsim.ASimulation;
import tech.cypherskar.cloudex.controllers.Simulations;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**Responsible for Exporting charts to files and creating save folder.*/
public class Exports
{
    //  *****************************
    //  Public Mehtods
    //  *****************************

    /**
     * Create the save/export button.
     * @param frame Main frame button belongs to.
     * @param setupText Referrence to experiment setup-text-area panel string.
     * @param simText Referrence to experiment results-text-area panel string.
     * @param charts charts panels.
     * @param chartsFormat Image format.
     * @return JButton enabling user to export results.
     */
    public static JButton GetButton(JFrame frame, JTextArea setupText, JTextArea simText,
    ObjectArrayList<XChartPanel<CategoryChart>> charts, BitmapFormat chartsFormat)
    {
        String title = "Export";

        JButton result = new JButton(title);
        result.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
        result.setName(title);
        result.setEnabled(true);
        result.setVisible(true);
        
        result.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent arg0) 
            {
                Exports exports = new Exports();
                String resultsFolder = exports.GetInitialFolderPath(Simulations.GetList());
                String path = null;
                while(true)
                {
                    path = JOptionPane.showInputDialog(frame, "Results Path",
                    resultsFolder);

                    if (path == null) return;

                    if (path.equals(resultsFolder))
                    {
                        exports.CreateFolder(path);
                        break;
                    }
                    else
                    if (!Files.exists(Paths.get(path)))
                    {
                        JOptionPane.showMessageDialog(frame, 
                        "Check Your Path For Any Errors (i.e. missing '/').", "Directory Error",
                        JOptionPane.ERROR_MESSAGE);
                    }
                    else
                    {
                        break;
                    }
                }

                Integer[] dpiArray = new Integer[]{100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
                Integer dpi = (Integer)JOptionPane.showInputDialog(frame, "Cancelling Sets DPI To 72 (Default)", 
                "DPI Options", JOptionPane.INFORMATION_MESSAGE, null, dpiArray, 72);
                
                exports.ExportText(setupText.getText() + simText.getText(), path);
                exports.ExportCharts(charts, chartsFormat, path, dpi);
                
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
     * Populate exportation's initial folder's path.
     * @param sims All simulations involved in the experiment.
     * @return Exportation folder path.
     */
    public String GetInitialFolderPath(ObjectArrayList<ASimulation> sims)
    {
        StringBuilder result = new StringBuilder(System.getProperty("user.dir"));
        result.append(File.separator + "results" + File.separator);
        
        // All filesystems allow naming of folders to contain {},[]--
        // Create year and month folder.
        result.append(new SimpleDateFormat("yyyy-MM").format(new Date()));
        result.append(File.separator);
        
        String[] names = 
        sims.stream().map(x -> x.getClass().getSimpleName()).distinct().toArray(String[]::new);
        // Create experiment folder.
        for(int i = 0; i < names.length; i++)
        {
            result.append(names[i]);
            
            if (i+1 < names.length)
            result.append("_");
        }
        result.append(" [" + new SimpleDateFormat("hh-mm-ss z").format(new Date()) + "]");
        result.append(File.separator);

        return result.toString();
    }

    /**
     * Confirm folder creation.
     * @param path Path to create results folder in.
     * @return File "has been created" status.
     */
    public boolean CreateFolder(String path)
    {
        File file = new File(path);
        boolean result = file.mkdirs();
        file.setWritable(true);
        file.setExecutable(true);

        return result;
    }

    /**
     * Export experiment's results string to 'simulation.txt' file.
     * @param string Text to be written into text file.
     * @param path Folder path.
     */
    public void ExportText(String string, String path)
    { 
        try
        {
            File file = new File(path + "simulation.txt");
            file.createNewFile();
            file.setWritable(true);

            PrintWriter resultsFile = new PrintWriter(file);
            resultsFile.print(string);

            file.setWritable(false);
            file.setReadOnly();
            resultsFile.flush();
            resultsFile.close();
        } catch (IOException e) {
        e.printStackTrace();
        }
    }

    /**
     * Export result's charts
     * @param charts Charts been populated for the experiment.
     * @param chartFormat Image foramt.
     * @param path Folder's path.
     * @param dpi Image quality value.
     */
	public void ExportCharts(ObjectArrayList<XChartPanel<CategoryChart>> charts, 
	BitmapFormat chartFormat , String path, Integer dpi)
    { 
        if (chartFormat == null) chartFormat = BitmapFormat.JPG;
        if (dpi == null) dpi = 72;
        
        //System.out.println(dpi);
        try
        {
            for(XChartPanel<CategoryChart> panel : charts)
            {
                //BitmapEncoder.saveJPGWithQuality(panel.getChart(), path + panel.getChart().getTitle(), 1);
                BitmapEncoder.saveBitmapWithDPI(panel.getChart(), path + panel.getChart().getTitle(), chartFormat, dpi);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}