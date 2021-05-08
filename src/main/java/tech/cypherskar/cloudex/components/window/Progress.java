package tech.cypherskar.cloudex.components.window;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import tech.cypherskar.cloudex.templates.infos.ASimulationInfo;

/**
 * Shows each simulation's progress update by having 
 * {@link tech.cypherskar.cloudex.components.cloudsim.ASimulation} call UpdateProgress(int)
 * method.
 * @see tech.cypherskar.cloudex.components.cloudsim.ASimulation
 * @see tech.cypherskar.cloudex.components.algorithms.standard.StandardSim
 */
public class Progress
{
    //  *****************************
    //  Fields
    //  *****************************
    private JPanel panel;
    private JProgressBar simPB;
    private JProgressBar sentTasksPB;
    private JProgressBar recievedTasksPB;
    
    //  *****************************
    //  Public Mehtods
    //  *****************************

    /**Initial customization of progress bar.*/
    public void Initialize()
    {
        this.simPB = new JProgressBar();
        this.sentTasksPB = new JProgressBar();
		this.recievedTasksPB = new JProgressBar();
		
        this.simPB.setValue(0);
        this.sentTasksPB.setValue(0);
        this.recievedTasksPB.setValue(0);
        
        this.simPB.setBorderPainted(true);
        this.simPB.setStringPainted(true);
        this.sentTasksPB.setBorderPainted(true);
        this.sentTasksPB.setStringPainted(true);
        this.recievedTasksPB.setBorderPainted(true);
        this.recievedTasksPB.setStringPainted(true);
        
        this.simPB.setName("CloudSim ");
        this.sentTasksPB.setName("Sending Tasks ");
        this.recievedTasksPB.setName("Recieving Tasks ");

        this.UpdateProgressString(this.simPB);
        this.UpdateProgressString(this.sentTasksPB);
        this.UpdateProgressString(this.recievedTasksPB);
    
        JPanel lowerPanel = new JPanel(new GridLayout(1, 2));
        lowerPanel.add(this.sentTasksPB);
        lowerPanel.add(this.recievedTasksPB);
    
        this.panel = new JPanel();
        this.panel.setLayout(new BorderLayout());
        this.panel.add(this.simPB, BorderLayout.PAGE_START);
        this.panel.add(lowerPanel, BorderLayout.PAGE_END);
	}
	
    /**
     * Reset progress bar's maximum bound value.
     * @param max Progress bar limit.
     */
	public void NewTasksTarget(int max)
    {
        this.simPB.setMaximum(max);
        this.sentTasksPB.setMaximum(max);
		this.recievedTasksPB.setMaximum(max);
        
		this.UpdateProgressString(this.simPB);
        this.UpdateProgressString(this.sentTasksPB);
        this.UpdateProgressString(this.recievedTasksPB);
    }

    /**
     * Reset progress bars' values for the next simulation.
     * @param info Next simulation's data.
     */
    public void NewSimulation(ASimulationInfo info)
    {
        this.simPB.setValue(0);
        this.sentTasksPB.setValue(0);
		this.recievedTasksPB.setValue(0);
		
		//this.sentTasksPB.setIndeterminate(true);
		//this.recievedTasksPB.setIndeterminate(true);

		if (!info.algorithm.GetHint().isEmpty())
		this.simPB.setName(info.Name + '_' + info.algorithm.GetHint() + ' ');
		else
		this.simPB.setName(info.Name + ' ');
		
        this.UpdateProgressString(this.simPB);
        //this.UpdateProgressString(this.sentTasksPB);
        //this.UpdateProgressString(this.recievedTasksPB);
		this.sentTasksPB.setString("Sending Tasks In Progress...");
		this.recievedTasksPB.setString("Recieving Tasks In Progress...");
	}
	
    /**
     * @return Progress bar's maximum bound value.
     */
	public int GetTasksTarget()
	{
		return this.sentTasksPB.getMaximum();
	}

    /**
     * @return The panel containing all progress bars.
     */
    public JPanel GetPanel()
    {
        return this.panel;
    }

    /**
     * Update the current progress value by the given number.
     * @param value Number to update progress by.
     */
    public void UpdateAlgorithm(int value)
    {
        this.simPB.setValue(this.simPB.getValue() + value);
        this.UpdateProgressString(this.simPB);
    }

    /**
     * Set algorithm's progress bar's maximum bound value.
     * @param max Value of the maximum bound.
     */
    public void SetAlgorithmMax(int max)
    {
        this.simPB.setMaximum(max);
		this.UpdateProgressString(this.simPB);
	}
	
    /**
     * Progress bar's display message.
     * @param msg progress display message.
     */
	public void SetAlgorithmString(String msg)
    {
		this.simPB.setName(msg);
		this.UpdateProgressString(this.simPB);
    }

    /**
     * Update sent tasks progress bar.
     * @param value Update simulation's finished tasks number.
     */
    public void UpdateSentTasks(int value)
    {
        this.sentTasksPB.setValue(this.sentTasksPB.getValue() + value);
        this.UpdateProgressString(this.sentTasksPB);
    }
    
    /**
     * Update recieved tasks progress bar.
     * @param value Update simulation's received tasks number.
     */
    public void UpdateRecievedTasks(int value)
    {
        this.recievedTasksPB.setValue(this.recievedTasksPB.getValue() + value);
        this.UpdateProgressString(this.recievedTasksPB);
    }

    //  *****************************
    //  Private Mehtods
    //  *****************************

    /**
     * Format the display message.
     * @param bar Progress bar to be updated.
     */
    private void UpdateProgressString(JProgressBar bar)
    {
        bar.setString(bar.getName() + ' ' + bar.getValue() + " \\ " + bar.getMaximum());
    }
}