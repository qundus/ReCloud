package com.upm.researcher.components.window;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.text.DecimalFormat;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.upm.researcher.controllers.Brokers;
import com.upm.researcher.controllers.Datacenters;
import com.upm.researcher.templates.infos.ASimulationInfo;

public class Tables
{
    //  *****************************
    //  Enums 
    //  *****************************

    /**Style of dipicting results tables.*/
    public enum TableMode
    {
        /**Simple results table format.*/
        Simple,
        /**Simple results table format with additional info.*/
        Simple_With_TasksSplit,
        /**Compact results table format.*/
        Compact,
        /**Compact results table format with additional info.*/
        Compact_With_TasksSplit,
    }
    
    //  *****************************
    //  Fields
    //  *****************************
    public int cellSpace;
    public int fontSize;
    public Font font;
    public char cellSeperator;
    public char hintStart;
    public char hintEnd;
	public int titlePlace;
    public char titleSides;
    public char titleFooter;
    public DecimalFormat numf;
    public TableMode tableForm;
    
    private int lastTableStartIdx;
    private int numSimulations;
    
    //  *****************************
    //  Fields (GUI)
    //  *****************************
    private JScrollPane setupScrollPanel;
    private JTextArea setupTextPanel;

    private JScrollPane simScrollPanel;
    private JTextArea simTextPanel;

    public Color fgColor;
    public Color bgColor;

    //  *****************************
    //  Contructors
    //  *****************************

    public Tables()
    {
        // Legacy
        this.cellSpace = -1;
        this.fontSize = -1;
        this.font = new Font(Font.DIALOG_INPUT, Font.PLAIN, this.fontSize);
        this.titlePlace = -1;
        this.cellSeperator = '|';
        this.hintStart = '(';
        this.hintEnd = ')';
        this.titleSides = '|';
        this.titleFooter = '-';
        this.numf = new DecimalFormat("###,###,###.####");
        this.tableForm = TableMode.Compact;

        // GUI
        this.fgColor = Color.BLACK;
        this.bgColor = Color.WHITE;

        this.setupTextPanel = new JTextArea();
        this.setupScrollPanel = new JScrollPane(setupTextPanel);
        this.setupScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setupScrollPanel.getHorizontalScrollBar().setUnitIncrement(16);
        this.setupScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.setupScrollPanel.getVerticalScrollBar().setUnitIncrement(16);
        
        
        
        this.simTextPanel = new JTextArea();
        this.simScrollPanel = new JScrollPane(simTextPanel);
        this.simScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.simScrollPanel.getHorizontalScrollBar().setUnitIncrement(16);
        this.simScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.simScrollPanel.getVerticalScrollBar().setUnitIncrement(16);
    }

    /**
     * Prepare text display style.
     * @param numSimulations Number of simulations in the experiment.
     */
    public void Initialize(int numSimulations)
    {
        // Legacy
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        double minWidth = Double.POSITIVE_INFINITY;
        for(int i = 0; i < gd.length; i++)
        {
            if (minWidth > gd[i].getDefaultConfiguration().getBounds().width)
            {
                minWidth = gd[i].getDefaultConfiguration().getBounds().width;
            }
        }

        if (this.titlePlace == -1) this.titlePlace = (int)(minWidth * 0.05);
        if (this.fontSize == -1) this.fontSize = (int)(minWidth * 0.0108);
		if (this.cellSpace == -1) this.cellSpace = (int)(minWidth * 0.015);

        this.numSimulations = numSimulations + 1;

        // GUI
        this.font = new Font(Font.DIALOG_INPUT, Font.PLAIN, this.fontSize);

        // Setup text area
        StringBuilder string = new StringBuilder();

		string.append(this.CreateTitle("    Environment Setup    ", this.titlePlace, ""));
		
		int tempCellSpace = this.cellSpace;
		this.cellSpace = tempCellSpace+(int)(tempCellSpace*0.2);

		// Datacenters Table
		string.append("Datacenters And Hosts" + System.lineSeparator());
		String[] datacentersTable = new Datacenters().GetSetupTable(this);
		string.append(
		this.AddFooter(datacentersTable[0].length(), this.titleFooter, this.titleFooter));
		string.append(String.join(System.lineSeparator(), datacentersTable));
		string.append(System.lineSeparator());
		string.append(
		this.AddFooter(datacentersTable[0].length(), this.titleFooter, this.titleFooter));
		
		// Brokers Table
		string.append(System.lineSeparator());
		string.append("Brokers And Virtual Machines" + System.lineSeparator());
		String[] brokersTable = new Brokers().GetSetupTable(this);
		string.append(
		this.AddFooter(brokersTable[0].length(), this.titleFooter, this.titleFooter));
		string.append(String.join(System.lineSeparator(), brokersTable));
		string.append(System.lineSeparator());
		string.append(
        this.AddFooter(brokersTable[0].length(), this.titleFooter, this.titleFooter));
		string.append(System.lineSeparator());

		// Finalize
		this.cellSpace = tempCellSpace;
		this.setupTextPanel.setFont(this.font);
        this.setupTextPanel.setText(string.toString());
        this.setupTextPanel.setAutoscrolls(true);
        this.setupTextPanel.setEditable(false);
        this.setupTextPanel.setBackground(this.bgColor);
        this.setupTextPanel.setForeground(this.fgColor);

        ////////////////////////
        // Simulation text area
        this.simTextPanel.setFont(this.font);
        this.simTextPanel.setText(this.CreateTitle("      Simulations     ", this.titlePlace, "").toString());
        this.simTextPanel.setAutoscrolls(true);
        this.simTextPanel.setEditable(false);
        this.simTextPanel.setBackground(this.bgColor);
        this.simTextPanel.setForeground(this.fgColor);

        // Finalize
        // this.panel.setLayout(new BorderLayout());
        // this.panel.add(this.simScrollPanel, BorderLayout.CENTER);
    }
    
    /**
     * Append tables' columns/rows headers to simulation's text for the next 
     * number of tasks.
     * @param tasksTarget Number of tasks for the next round of simulations.
     * @param warnings Text carrying any initialization, cloudism setup and/or customization warnings.
     */
    public void NewTasksTarget(final int tasksTarget, String warnings)
    {
        // Legacy
        StringBuilder string = new StringBuilder();
        string.append(System.lineSeparator());
        
        string.append("Simulation For " + this.numf.format(tasksTarget) + " Tasks");     
        string.append(System.lineSeparator());
        
        if (warnings != null && !warnings.isEmpty())
        {
            warnings = "# Warning: " + warnings;
            string.append(warnings);
            string.append(System.lineSeparator());
        }
        
        
        // Format table accordingly.
        switch(this.tableForm)
        {
            case Compact_With_TasksSplit:
            case Compact:
            string.append(this.AddFooter((this.cellSpace * numSimulations) + numSimulations, 
            this.titleFooter, this.titleFooter));
            
            // Set last table index for revisiting and table modification.
            this.lastTableStartIdx = this.simTextPanel.getText().length() + string.length();

            string.append(this.CellFormat(this.cellSeperator + "Algorithm","-", null, true));
            string.append(this.CellFormat(this.cellSeperator + "Makespan","-", "S", true));
            string.append(this.CellFormat(this.cellSeperator + "Vms Makespan","-", "S", true));
            string.append(this.CellFormat(this.cellSeperator + "Imbalance Degree","-", null, true));
            string.append(this.CellFormat(this.cellSeperator + "Standard Deviation","-", null, true));
            string.append(this.CellFormat(this.cellSeperator + "ProcessingSpeed","-", "ms", true));
            string.append(this.CellFormat(this.cellSeperator + "Vm Scheduler","-", null, true));
            string.append(this.CellFormat(this.cellSeperator + "Cloudlet Scheduler","-", null, false));
            break;
            
            case Simple_With_TasksSplit:
            case Simple:
            string.append(this.AddFooter((this.cellSpace * 8) + 8, this.titleFooter, this.titleFooter));
            string.append(this.CellFormat(this.cellSeperator + "Algorithm","-", null, false));
            string.append(this.CellFormat("Makespan","-", "S", false));
            string.append(this.CellFormat("Vms Makespan","-", "S", false));
            string.append(this.CellFormat("Imbalance Degree","-", null, false));
            string.append(this.CellFormat("Standard Deviation","-", null, false));
            string.append(this.CellFormat("ProcessingSpeed","-", "ms", false));
            string.append(this.CellFormat("Vm Scheduler","-", null, false));
            string.append(this.CellFormat("Cloudlet Scheduler","-", null, false));
            break;
        }
        
        // GUI
        // Add text
        this.simTextPanel.setText(this.simTextPanel.getText() + string.toString());
        this.simTextPanel.updateUI();
        // this.panel.updateUI();
	}

    /**
     * Formulate results text to be displayed according to 
     * pre-formatted table styles (TableMode).
     * @param info Text carrying current simulation's data.
     */
    public void EndSimulation(ASimulationInfo info)
    {
        // Legacy -> GUI
        switch(this.tableForm)
        {
            case Compact_With_TasksSplit:
            case Compact:
            String[] table = 
            this.simTextPanel.getText().substring(this.lastTableStartIdx, this.simTextPanel.getText().length())
            .split(System.lineSeparator());

            table[0] += this.CellFormat(info.Name,"-", info.algorithm.GetHint(), true);
            table[1] += this.CellFormat(this.numf.format(info.Makespan), "", null, true);
            table[2] += this.CellFormat(this.numf.format(info.Vms_Makespan),"", null, true);
            table[3] += this.CellFormat(this.numf.format(info.Degree_Of_Imbalance),"", null, true);
            table[4] += this.CellFormat(this.numf.format(info.Standard_Deviation),"", null, true);
            table[5] += this.CellFormat(this.numf.format(info.Algorithm_Time),"", null, true);
            table[6] += this.CellFormat(info.algorithm.vmScheduler.name(),"-", null, true);
            table[7] += this.CellFormat(info.algorithm.taskScheduler.name(),"-", null, false);

            // Remove last table
            this.simTextPanel.setText(this.simTextPanel.getText().substring(0, this.lastTableStartIdx));

            // Add text
            this.simTextPanel.setText(this.simTextPanel.getText() + String.join("", table));
            break;

            case Simple_With_TasksSplit:
            case Simple:

            StringBuilder string = new StringBuilder(System.lineSeparator());
            
            string.append(this.CellFormat(this.cellSeperator + info.Name,"-", info.algorithm.GetHint(), false));
            string.append(this.CellFormat(this.numf.format(info.Makespan), "", null, false));
            string.append(this.CellFormat(this.numf.format(info.Vms_Makespan),"", null, false));
            string.append(this.CellFormat(this.numf.format(info.Degree_Of_Imbalance),"", null, false));
            string.append(this.CellFormat(this.numf.format(info.Standard_Deviation),"", null, false));
            string.append(this.CellFormat(this.numf.format(info.Algorithm_Time),"", null, false));
            string.append(this.CellFormat(info.algorithm.vmScheduler.name(),"-", null, false));
            string.append(this.CellFormat(info.algorithm.taskScheduler.name(),"-", null, false));

            // Add text
            this.simTextPanel.setText(this.simTextPanel.getText() + string.toString());
            break;
        }
	}

    /**
     * Append stylezation text to current results text to clearly mark
     * the seperation line between every experiment done for a specific
     * number of tasks.
    */
    public void EndTasksTarget()
    {
        // Legacy
        // Print table footer
        StringBuilder string = new StringBuilder();
        
        int lastLineIdx = this.simTextPanel.getText().lastIndexOf(System.lineSeparator());
        String lastLine = this.simTextPanel.getText().substring(lastLineIdx, this.simTextPanel.getText().length()-1);
        switch(this.tableForm)
        {
            case Compact_With_TasksSplit:
            case Simple_With_TasksSplit:
            
            String[] splits = new Brokers().GetDistributionTable(this);
    
            if (lastLine.length() >= splits[0].length())
            string.append(this.AddFooter(lastLine.length()-1, this.cellSeperator, this.titleFooter));
            else
            string.append(this.AddFooter(splits[0].length()-1, this.cellSeperator, this.titleFooter));

            string.append(String.join("", splits));

            if (lastLine.length() >= splits[0].length())
            string.append(this.AddFooter(lastLine.length()-1, this.titleFooter, this.titleFooter));
            else
            string.append(this.AddFooter(splits[0].length()-1, this.titleFooter, this.titleFooter));

            break;
            
            case Compact:
            case Simple:
            string.append(this.AddFooter(lastLine.length()-1, this.titleFooter, this.titleFooter));
            break;
        }

        // GUI
        // Add text
        this.simTextPanel.setText(this.simTextPanel.getText() + System.lineSeparator() + string.toString());
    }

    /**
     * Finalizing results texts, therefore, experiments done are signed 
     * off by their research conductor to eliminate experiments fraud
     * and/or impurity. For now, the exported results are locked from
     * being written on but in the future it's planned to be encrypted
     * by the signee's name and never allowing any way of manipulating it.
     * 
     * @param signedBy Name of the experiment conductor to be included 
     * in the results files.
     */
    public void EndExperiment(String signedBy)
    {
        // Legacy
        if (signedBy == null || signedBy.isEmpty())
        signedBy = "University Putra Malaysia";

        // Print table footer
        StringBuilder string = new StringBuilder();

        string.append("Experiment Done!");
        string.append(System.lineSeparator());
        string.append(System.lineSeparator());
        string.append("Signed By : " + signedBy);

        string = this.CreateTitle(string.toString(), 0, "-");

        // GUI
        // Add text
        this.simTextPanel.setText(this.simTextPanel.getText()
        + System.lineSeparator()
        + System.lineSeparator()
        + string.toString());
    }

    /**
     * @return Experiment's scroll pane carrying 
     * text area panel filled with experiment results.
     */
    public JScrollPane GetSimPanel()
    {
        return this.simScrollPanel;
    }

    /**
     * @return Experiment's text area panel filled with 
     * experiment's results. 
     */
    public JTextArea GetSimTextArea()
    {
        return this.simTextPanel;
    }

    /**
     * @return Experiment's scroll pane carrying
     * cloudsim's environment setup text area panel.
     */
    public JScrollPane GetSetupPanel()
    {
        return this.setupScrollPanel;
    }

    /**
     * @return Experiment's scroll pane filled with
     * cloudsim's environment setup.
     */
    public JTextArea GetSetupTextArea()
    {
        return this.setupTextPanel;
    }

    //#region Legacy Methods
    //  *****************************
    //  Public Methods
    //  *****************************

    /**
     * Format a text string to follow the title scheme to some extent.
     * @param msg Title name to be formatted.
     * @param len Length of title's space.
     * @param side Which side is the title text on (can be filled with "-", "").
     * @return Well formatted title text string.
     */
    public StringBuilder CreateTitle(String msg, int len, String side)
    {
		String[] lines = msg.toString().split(System.lineSeparator());
		
		// Look for longest string length
        int longestLine = 0;
        for (String line : lines)
        {
            if (longestLine < line.length())
            {
                longestLine = line.length();
            }
        }
		len += longestLine+1;
		
		// Create footer
        StringBuilder footerStr = new StringBuilder();
        for (int i = 0; i < longestLine; i++)
        {
            footerStr.append(this.titleFooter);
        }

        
        StringBuilder result = new StringBuilder();
        result.append(this.MessageFormat(footerStr.toString(), len, side));
        
        for (String line : lines)
        {
            result.append(System.lineSeparator());
            result.append(this.MessageFormat(line, len, side));
        }
        
        result.append(System.lineSeparator());
        result.append(this.MessageFormat(footerStr.toString(), len, side));
        result.append(System.lineSeparator());
        
        return result;
    }

    /**
     * Format a given message to be within table's cell format.
     * @param msg Message text string.
     * @param side Side the message is gonna be on ("-" or "").
     * @param hint A hint to visually distinguish simulations.
     * @param addNewLine Wheather to add a new line after the cell.
     * @return Well formatted table cell.
     * 
     * @see #CellFormatNoSeparator(String, String, String, boolean)
     */
    public String CellFormat(String msg, String side, String hint, boolean addNewLine)
    {
        try
        {
            String s;
            if (hint != null && !hint.isEmpty())
            {
                // To account for the hint, hintStart and hintEnd chars.;
                int msgSpace = this.cellSpace - hint.length() - 2;
                s = String.format("%" + side + msgSpace + '.' + msgSpace + 's'
                + this.hintStart + '%' + hint.length() + '.' + hint.length() + 's' + this.hintEnd
                + this.cellSeperator
                , msg, hint);
            }
            else
            {
                s = String.format("%" + side + this.cellSpace + '.' + 
                this.cellSpace + 's' + this.cellSeperator, msg);
            }
            
            if (addNewLine)
            {
                s += System.lineSeparator();
            }

            return s;

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        return "null";
	}
	
    /**
     * Format a given message to be within table's cell format.
     * @param msg Message text string.
     * @param side Side the message is gonna be on ("-" or "").
     * @param hint A hint to visually distinguish simulations.
     * @param addNewLine Wheather to add a new line after the cell.
     * @return Well formatted table cell.
     * 
     * @see #CellFormat(String, String, String, boolean)
     */
	public String CellFormatNoSeparator(String msg, String side, String hint, boolean addNewLine)
    {
        try
        {
            String s;
            if (hint != null && !hint.isEmpty())
            {
                // To account for the hint, hintStart and hintEnd chars.;
                int msgSpace = this.cellSpace - hint.length() - 2;
                s = String.format("%" + side + msgSpace + '.' + msgSpace + 's'
                + this.hintStart + '%' + hint.length() + '.' + hint.length() + 's' + this.hintEnd
                , msg, hint);
            }
            else
            {
                s = String.format("%" + side + this.cellSpace + '.' + 
                this.cellSpace + 's', msg);
            }
            
            if (addNewLine)
            {
                s += System.lineSeparator();
            }

            return s;

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        return "null";
    }

    //  *****************************
    //  Protected Methods
    //  *****************************
    
    /**
     * Append a pre-formated footer to texts.
     * @param msg Message that's going to be written above the footer.
     * @param sides Side characters to add a sense of beautification to footers.
     * @param footer Preferred character of footer's notion.
     * @return Well formatted footer text.
     */
    protected StringBuilder AddFooter(String msg, char sides, char footer)
    {
        StringBuilder result = new StringBuilder(System.lineSeparator());
        String[] deduce = msg.toString().split(System.lineSeparator());
        int longestLine = 0;
        for (String s : deduce)
        {
            if (s.length() > longestLine)
            {
                longestLine = s.length();
            }
        }

        for (int i = 0; i < longestLine; i++)
        {
            if (i == 0 || i + 1 >= longestLine)
            {
                result.append(sides);
            }
            else
            {
                result.append(footer);
            }
        }

        result.append(System.lineSeparator());

        return result;
    }

    /**
     * Append a pre-formated footer to texts.
     * @param count Number of characters to print.
     * @param sides Side characters to add a sense of beautification to footers.
     * @param footer Footer character to print.
     * @return Formatted footer text.
     */
    protected StringBuilder AddFooter(int count, char sides, char footer)
    {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < count; i++)
        {
            if (i == 0 || i + 1 >= count)
            {
                result.append(sides);
            }
            else
            {
                result.append(footer);
            }
        }

        result.append(System.lineSeparator());

        return result;
    }

    /**
     * Create formatted text string in the form 'String.format()'
     * without the headache of writing the same lines repeatedly.
     * @param msg Message text string.
     * @param len Space before and after message.
     * @param side Side of the message ("-" or "").
     * @return Well formatted message text string.
     */
    protected String MessageFormat(String msg, int len, String side)
    {
        return String.format(
        "%" + side + len + '.' + len + "s" + this.titleSides,
        this.titleSides + msg);
    }

    /**
     * Calculates number division for when a number is too big
     * or too small so it would be better written with the number
     * suffix format for better clarification and
     * cell space optimization.
     * So, 1000KB will be 1MB.
     * @param count Number to be altered.
     * @return Number given as a string of a well formatted number.
     */
    @Deprecated
    protected String GetNumberDivision(int count)
    {
        //if (count < 1000) return "" + count;
        
        int exp = (int) (Math.log(count) / Math.log(1000));

        return String.format("%-0.1f %-c", (1.0 * count) / Math.pow(1000, exp), " KMBTPE".charAt(exp));
    }
    //#endregion
}