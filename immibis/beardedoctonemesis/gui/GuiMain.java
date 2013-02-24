package immibis.beardedoctonemesis.gui;

import immibis.beardedoctonemesis.IProgressListener;
import immibis.beardedoctonemesis.Main;
import immibis.beardedoctonemesis.mcp.McpMapping;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

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
					if(xpathfile.toString().endsWith("_reobf.jar"))
						error += "\n\nYou need to reobfuscate before using BON.";
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
					String s = getStackTraceMessage(e);
					
					if(!new File(confDir, side.mcpside.srg_name).exists()) {
						s = side.mcpside.srg_name+" not found in conf directory. \n";
						switch(side) {
						case Client:
						case Server:
							s += "If you're using Forge, set the side to Universal (1.4.6+) or Universal_old (1.4.5 and earlier)";
							break;
						case Universal:
							s += "If you're not using Forge, set the side to Client or Server.\n";
							s += "If you're using Forge on 1.4.5 or earlier, set the side to Universal_old.";
							break;
						case Universal_old:
							s += "If you're not using Forge, set the side to Client or Server.\n";
							break;
						}
					}
					
					System.err.println(s);
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
					JOptionPane.showMessageDialog(GuiMain.this, s, "BON - Error", JOptionPane.ERROR_MESSAGE);
				} finally {
					progressBar.setValue(0);
				}
			}
		};
		
		curTask.start();
	}
	
	private static String getStackTraceMessage(Throwable e) {
		String s = "An error has occurred - give immibis this stack trace (which has been copied to the clipboard)\n";
		s += "\n" + e;
		for(StackTraceElement ste : e.getStackTrace())
		{
			s += "\n\tat " + ste.toString();
			if(ste.getClassName().startsWith("javax.swing."))
				break;
		}
		return s;
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
		
		
		File defaultPath  = new File(System.getProperty("user.dir"));
		File inputPath    = defaultPath;
		File outputPath   = defaultPath;
		File mcpPath      = defaultPath;
		
		
		// Handle .properties file, if one exists
		try{
		    Properties configFile = new Properties();
		    configFile.load(new FileReader(".properties"));
		    
		    // Validate paths and update text fields
	        String inputProp = configFile.getProperty("inputPath");
	        if(inputProp != null && new File(inputProp).exists()){
	            inputPath = new File(inputProp);
	            inputField.setText(inputPath.toString());
	        }
	        else{
	            System.out.println("inputPath property not specified or path not accessible");
	        }
		    
	        String outputProp = configFile.getProperty("outputPath");
	        if(outputProp != null && new File(outputProp).exists()){
	            outputPath = new File(outputProp);
	            outputField.setText(outputPath.toString());
	        }
	        else{
	            System.out.println("outputPath property not specified or path not accessible");
	        }
		    
		    String mcpProp = configFile.getProperty("mcpPath");
		    if(mcpProp != null && new File(mcpProp).exists()){
		        mcpPath = new File(mcpProp);
		        mcpField.setText(mcpPath.toString());
		    }
		    else{
		        System.out.println("mcpPath property not specified or path not accessible");
		    }
		    
		}
		catch(IOException e)
		{
		    System.out.println("No .properties file found.");
		}
		catch(NullPointerException e)
		{
		    System.out.println("Malformed .properties file.");
		}
		
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
		
		chooseInputButton.addActionListener(new BrowseActionListener(inputField, true, this, false, inputPath));
		chooseOutputButton.addActionListener(new BrowseActionListener(outputField, false, this, false, outputPath));
		chooseMCPButton.addActionListener(new BrowseActionListener(mcpField, true, this, true, mcpPath));
		
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
