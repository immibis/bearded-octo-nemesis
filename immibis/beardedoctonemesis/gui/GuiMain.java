package immibis.beardedoctonemesis.gui;

import immibis.beardedoctonemesis.IProgressListener;
import immibis.beardedoctonemesis.Main;
import immibis.beardedoctonemesis.mcp.McpMapping;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

public class GuiMain extends JFrame {
	private static final long serialVersionUID = 1;
	
	private JComboBox<Operation> opSelect;
	private JComboBox<Side>sideSelect;
	private JTextField inputField, outputField, mcpField;
	private JButton goButton;
	private JProgressBar progressBar;
	
	private Thread curTask = null;
	
	synchronized void goButtonPressed() {
		
		if(curTask != null && curTask.isAlive())
			return;
		
		//final Operation op = (Operation)opSelect.getSelectedItem();
		final Side side = (Side)sideSelect.getSelectedItem();
		
		final File mcpDir = new File(mcpField.getText());
		final File confDir = new File(mcpDir, "conf");
		final String[] xpathlist = side.xpath.split(File.pathSeparator);
		
		String error = null;
		
		if(!mcpDir.isDirectory())
			error = "MCP folder not found (at "+mcpDir+")";
		else if(!confDir.isDirectory())
			error = "'conf' folder not found in MCP folder (at "+confDir+")";
		else
		{
			for(int k = 0; k < xpathlist.length; k++)
			{
				String path = xpathlist[k];
				File xpathfile = new File(mcpDir, path);
				if(!xpathfile.isFile())
				{
					error = "'" + path + "' not found in MCP folder (at "+xpathfile+")";
					break;
				}
				xpathlist[k] = xpathfile.getAbsolutePath();
			}
		}
		
		if(error != null)
		{
			JOptionPane.showMessageDialog(this, error, "BON - Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		progressBar.setValue(0);
		
		curTask = new Thread() {
			public void run() {
				try {
					McpMapping mcp = new McpMapping(confDir, side.mcpside, false);
					
					Main m = new Main();
					m.input = new File(inputField.getText());
					m.output = new File(outputField.getText());
					m.map = mcp.getMapping();
					m.xpathlist = xpathlist;
					m.progress = new IProgressListener() {
						@Override
						public void start(final int max, String text) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									progressBar.setMaximum(max);
									progressBar.setValue(0);
								}
							});
						}
						
						@Override
						public void set(final int value) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									progressBar.setValue(value);
								}
							});
						}
					};
					m.run();
				} catch(Exception e) {
					String s = "An error has occurred - give immibis this stack trace (which has been copied to the clipboard)\n";
					s += "\n" + e;
					for(StackTraceElement ste : e.getStackTrace())
					{
						s += "\n\tat " + ste.toString();
						if(ste.getClassName().startsWith("javax.swing."))
							break;
					}
					System.err.println(s);
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
					JOptionPane.showMessageDialog(GuiMain.this, s, "BON - Internal error", JOptionPane.ERROR_MESSAGE);
				} finally {
					progressBar.setValue(0);
				}
			}
		};
		
		curTask.start();
	}
	
	public GuiMain() {
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.LINE_END;
		contentPane.add(new JLabel("Input file"), gbc.clone());
		gbc.gridy = 1;
		contentPane.add(new JLabel("Output file"), gbc.clone());
		gbc.gridy = 2;
		contentPane.add(new JLabel("MCP folder"), gbc.clone());
		gbc.gridy = 3;
		contentPane.add(new JLabel("Side"), gbc.clone());
		gbc.gridy = 4;
		contentPane.add(new JLabel("Operation"), gbc.clone());
		
		JButton chooseInputButton = new JButton("Browse");
		JButton chooseOutputButton = new JButton("Browse");
		JButton chooseMCPButton = new JButton("Browse");
		
		goButton = new JButton("Go");
		
		inputField = new JTextField();
		outputField = new JTextField();
		mcpField = new JTextField();
		
		sideSelect = new JComboBox<Side>(Side.values());
		opSelect = new JComboBox<Operation>(Operation.values());
		
		progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		
		inputField.setMinimumSize(new Dimension(100, 0));
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		contentPane.add(chooseInputButton, gbc.clone());
		gbc.gridy = 1;
		contentPane.add(chooseOutputButton, gbc.clone());
		gbc.gridy = 2;
		contentPane.add(chooseMCPButton, gbc.clone());
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.ipadx = 200;
		contentPane.add(inputField, gbc.clone());
		gbc.gridy = 1;
		contentPane.add(outputField, gbc.clone());
		gbc.gridy = 2;
		contentPane.add(mcpField, gbc.clone());
		gbc.gridy = 3;
		contentPane.add(sideSelect, gbc.clone());
		gbc.gridy = 4;
		contentPane.add(opSelect, gbc.clone());
		
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 3;
		contentPane.add(goButton, gbc.clone());
		gbc.gridy = 6;
		contentPane.add(progressBar, gbc.clone());
		
		setContentPane(contentPane);
		pack();
		
		Reference<File> defaultDir = new Reference<File>();
		
		chooseInputButton.addActionListener(new BrowseActionListener(inputField, true, this, false, defaultDir));
		chooseOutputButton.addActionListener(new BrowseActionListener(outputField, false, this, false, defaultDir));
		chooseMCPButton.addActionListener(new BrowseActionListener(mcpField, true, this, true, defaultDir));
		
		goButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				goButtonPressed();
			}
		});
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Bearded Octo Nemesis");
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GuiMain().setVisible(true);
			}
		});
	}
}
