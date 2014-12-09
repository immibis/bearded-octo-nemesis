package immibis.bon.gui;

import immibis.bon.ClassCollection;
import immibis.bon.IProgressListener;
import immibis.bon.NameSet;
import immibis.bon.ReferenceDataCollection;
import immibis.bon.Remapper;
import immibis.bon.com.immibis.json.JsonReader;
import immibis.bon.io.ClassCollectionFactory;
import immibis.bon.io.JarWriter;
import immibis.bon.mcp.CsvFile;
import immibis.bon.mcp.ExcFile;
import immibis.bon.mcp.MappingFactory;
import immibis.bon.mcp.MappingLoader_MCP;
import immibis.bon.mcp.MinecraftNameSet;
import immibis.bon.mcp.SrgFile;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.Dialog.ModalityType;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JCheckBox;

public class NewGUI {
	
	// The Java Preferences API is used to store the last directory the user was browsing
	// for the input/output files (PREFS_KEY_BROWSEDIR)
	// and the selected MCP directory (PREFS_KEY_MCPDIR).
	// Prefs are saved when the user clicks "Go" or closes the window.
	private final Preferences prefs = Preferences.userNodeForPackage(NewGUI.class);
	private final static String PREFS_KEY_BROWSEDIR = "browseDir";
	private final static String PREFS_KEY_MCPDIR = "mcpDir";
	private final static String PREFS_KEY_FORGEJAR = "forgeUserdevJar";
	private final static String PREFS_KEY_FGCACHE = "forgegradleCacheDir";
	

	private JFrame frmBeardedOctoNemesis;
	private JTextField txtInputFile;
	private JTextField txtOutputFile;
	private JButton btnBrowseOutputFile;
	private JTextField txtMCPDir;
	private JButton btnBrowseMCPDir;
	private JButton btnBrowseInputFile;
	private JComboBox mcpSideSelector;
	private JTabbedPane tabbedPane;
	private JPanel panel;
	private JPanel panel_1;
	private JButton btnGoForgeDownload;
	private JPanel panel_2;
	private JLabel label_1;
	private JComboBox downloadForgeVersionSelector;
	private JButton btnGetVersionList;
	private JLabel lblNoteThisWill;
	private JLabel lblForgeUserdevjar;
	private JTextField txtForgeJar;
	private JButton btnBrowseForgeJar;
	private JButton btnGoForge;
	private JButton btnGoMCP;
	private JCheckBox chckbxUseSrgNames;
	private JLabel lblOperation;
	private JComboBox operationSelector;
	private JLabel lblThisIsNot;
	private JLabel lblForgegradleCacheFolder;
	private JTextField txtFGCache;
	private JButton btnBrowseFGCache;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels()) {
						//System.out.println("Installed look-and-feel: "+lafi.getName()+" @ "+lafi.getClassName());
						
						if(lafi.getName().equals("Windows"))
							UIManager.setLookAndFeel(lafi.getClassName());
					}
					
					NewGUI window = new NewGUI();
					window.frmBeardedOctoNemesis.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public NewGUI() {
		initialize();
		
		String lastMCPDir = prefs.get(PREFS_KEY_MCPDIR, "");
		if(!lastMCPDir.equals("") && new File(lastMCPDir).isDirectory())
			txtMCPDir.setText(new File(lastMCPDir).getAbsolutePath());
		
		String lastForgeJar = prefs.get(PREFS_KEY_FORGEJAR, "");
		if(!lastForgeJar.equals("") && new File(lastForgeJar).exists())
			txtForgeJar.setText(new File(lastForgeJar).getAbsolutePath());
		
		String lastFGCache = prefs.get(PREFS_KEY_FGCACHE, "");
		if(!lastFGCache.equals("") && new File(lastFGCache).isDirectory())
			txtFGCache.setText(new File(lastFGCache).getAbsolutePath());
		
		tabbedPane.setSelectedIndex(2);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBeardedOctoNemesis = new JFrame();
		frmBeardedOctoNemesis.setTitle("Bearded Octo Nemesis");
		frmBeardedOctoNemesis.setBounds(100, 100, 450, 285);
		frmBeardedOctoNemesis.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{55, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{14, 14, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		frmBeardedOctoNemesis.getContentPane().setLayout(gridBagLayout);
		
		JLabel lblInputFile = new JLabel("Input file:");
		lblInputFile.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblInputFile = new GridBagConstraints();
		gbc_lblInputFile.fill = GridBagConstraints.VERTICAL;
		gbc_lblInputFile.anchor = GridBagConstraints.EAST;
		gbc_lblInputFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblInputFile.gridx = 0;
		gbc_lblInputFile.gridy = 0;
		frmBeardedOctoNemesis.getContentPane().add(lblInputFile, gbc_lblInputFile);
		
		txtInputFile = new JTextField();
		GridBagConstraints gbc_txtInputFile = new GridBagConstraints();
		gbc_txtInputFile.weightx = 1.0;
		gbc_txtInputFile.insets = new Insets(0, 0, 5, 5);
		gbc_txtInputFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtInputFile.gridx = 1;
		gbc_txtInputFile.gridy = 0;
		frmBeardedOctoNemesis.getContentPane().add(txtInputFile, gbc_txtInputFile);
		txtInputFile.setColumns(10);
		
		btnBrowseInputFile = new JButton("Browse");
		btnBrowseInputFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				File curDir = new File(prefs.get(PREFS_KEY_BROWSEDIR, "."));
				if(!curDir.isDirectory())
					curDir = new File(".");
				fc.setCurrentDirectory(curDir);
				
				fc.setFileFilter(new FileNameExtensionFilter("JAR files", "jar", "zip"));
				
				if(fc.showOpenDialog(frmBeardedOctoNemesis) == JFileChooser.APPROVE_OPTION) {
					prefs.put(PREFS_KEY_BROWSEDIR, fc.getCurrentDirectory().getAbsolutePath());
					
					txtInputFile.setText(fc.getSelectedFile().getAbsolutePath());
					
					if(txtOutputFile.getText().equals("")) {
						File inputDir = fc.getSelectedFile().getParentFile();
						String inputName = fc.getSelectedFile().getName();
						
						String outputName;
						if(inputName.contains(".")) {
							int i = inputName.lastIndexOf('.');
							outputName = inputName.substring(0, i) + ".deobf." + inputName.substring(i+1);
						} else
							outputName = inputName + ".deobf.jar";
						
						txtOutputFile.setText(new File(inputDir, outputName).getAbsolutePath());
					}
				}
			}
		});
		GridBagConstraints gbc_btnBrowseInputFile = new GridBagConstraints();
		gbc_btnBrowseInputFile.anchor = GridBagConstraints.WEST;
		gbc_btnBrowseInputFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseInputFile.gridx = 2;
		gbc_btnBrowseInputFile.gridy = 0;
		frmBeardedOctoNemesis.getContentPane().add(btnBrowseInputFile, gbc_btnBrowseInputFile);
		
		JLabel lblOutputFile = new JLabel("Output file:");
		lblOutputFile.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblOutputFile = new GridBagConstraints();
		gbc_lblOutputFile.fill = GridBagConstraints.VERTICAL;
		gbc_lblOutputFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutputFile.anchor = GridBagConstraints.EAST;
		gbc_lblOutputFile.gridx = 0;
		gbc_lblOutputFile.gridy = 1;
		frmBeardedOctoNemesis.getContentPane().add(lblOutputFile, gbc_lblOutputFile);
		
		txtOutputFile = new JTextField();
		GridBagConstraints gbc_txtOutputFile = new GridBagConstraints();
		gbc_txtOutputFile.weightx = 1.0;
		gbc_txtOutputFile.insets = new Insets(0, 0, 5, 5);
		gbc_txtOutputFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtOutputFile.gridx = 1;
		gbc_txtOutputFile.gridy = 1;
		frmBeardedOctoNemesis.getContentPane().add(txtOutputFile, gbc_txtOutputFile);
		txtOutputFile.setColumns(10);
		
		btnBrowseOutputFile = new JButton("Browse");
		btnBrowseOutputFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				File curDir = new File(prefs.get(PREFS_KEY_BROWSEDIR, "."));
				if(!curDir.isDirectory())
					curDir = new File(".");
				fc.setCurrentDirectory(curDir);
				
				fc.setFileFilter(new FileNameExtensionFilter("JAR files", "jar", "zip"));
				
				if(fc.showSaveDialog(frmBeardedOctoNemesis) == JFileChooser.APPROVE_OPTION) {
					prefs.put(PREFS_KEY_BROWSEDIR, fc.getCurrentDirectory().getAbsolutePath());
					
					txtOutputFile.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		GridBagConstraints gbc_btnBrowseOutputFile = new GridBagConstraints();
		gbc_btnBrowseOutputFile.anchor = GridBagConstraints.WEST;
		gbc_btnBrowseOutputFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseOutputFile.gridx = 2;
		gbc_btnBrowseOutputFile.gridy = 1;
		frmBeardedOctoNemesis.getContentPane().add(btnBrowseOutputFile, gbc_btnBrowseOutputFile);
		
		lblOperation = new JLabel("Operation:");
		lblOperation.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblOperation = new GridBagConstraints();
		gbc_lblOperation.anchor = GridBagConstraints.EAST;
		gbc_lblOperation.insets = new Insets(0, 0, 5, 5);
		gbc_lblOperation.gridx = 0;
		gbc_lblOperation.gridy = 2;
		frmBeardedOctoNemesis.getContentPane().add(lblOperation, gbc_lblOperation);
		
		operationSelector = new JComboBox();
		operationSelector.setModel(new DefaultComboBoxModel(Operation.values()));
		GridBagConstraints gbc_operationSelector = new GridBagConstraints();
		gbc_operationSelector.insets = new Insets(0, 0, 5, 5);
		gbc_operationSelector.fill = GridBagConstraints.HORIZONTAL;
		gbc_operationSelector.gridx = 1;
		gbc_operationSelector.gridy = 2;
		frmBeardedOctoNemesis.getContentPane().add(operationSelector, gbc_operationSelector);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.gridwidth = 3;
		gbc_tabbedPane.insets = new Insets(0, 5, 5, 5);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 3;
		frmBeardedOctoNemesis.getContentPane().add(tabbedPane, gbc_tabbedPane);
		
		panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.addTab("MCP", null, panel, null);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblMcpFolder = new JLabel("MCP folder:");
		GridBagConstraints gbc_lblMcpFolder = new GridBagConstraints();
		gbc_lblMcpFolder.anchor = GridBagConstraints.EAST;
		gbc_lblMcpFolder.insets = new Insets(0, 0, 5, 5);
		gbc_lblMcpFolder.gridx = 0;
		gbc_lblMcpFolder.gridy = 0;
		panel.add(lblMcpFolder, gbc_lblMcpFolder);
		
		txtMCPDir = new JTextField();
		GridBagConstraints gbc_txtMCPDir = new GridBagConstraints();
		gbc_txtMCPDir.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMCPDir.insets = new Insets(0, 0, 5, 5);
		gbc_txtMCPDir.gridx = 1;
		gbc_txtMCPDir.gridy = 0;
		panel.add(txtMCPDir, gbc_txtMCPDir);
		txtMCPDir.setColumns(10);
		
		btnBrowseMCPDir = new JButton("Browse");
		btnBrowseMCPDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				File curDir = new File(txtMCPDir.getText());
				if(!curDir.isDirectory())
					curDir = new File(".");
				fc.setCurrentDirectory(curDir);
				
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				if(fc.showOpenDialog(frmBeardedOctoNemesis) == JFileChooser.APPROVE_OPTION) {
					prefs.put(PREFS_KEY_MCPDIR, fc.getSelectedFile().getAbsolutePath());
					
					txtMCPDir.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		GridBagConstraints gbc_btnBrowseMCPDir = new GridBagConstraints();
		gbc_btnBrowseMCPDir.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseMCPDir.gridx = 2;
		gbc_btnBrowseMCPDir.gridy = 0;
		panel.add(btnBrowseMCPDir, gbc_btnBrowseMCPDir);
		
		JLabel lblSide = new JLabel("Side:");
		GridBagConstraints gbc_lblSide = new GridBagConstraints();
		gbc_lblSide.anchor = GridBagConstraints.EAST;
		gbc_lblSide.insets = new Insets(0, 0, 5, 5);
		gbc_lblSide.gridx = 0;
		gbc_lblSide.gridy = 1;
		panel.add(lblSide, gbc_lblSide);
		
		mcpSideSelector = new JComboBox();
		GridBagConstraints gbc_mcpSideSelector = new GridBagConstraints();
		gbc_mcpSideSelector.fill = GridBagConstraints.HORIZONTAL;
		gbc_mcpSideSelector.insets = new Insets(0, 0, 5, 5);
		gbc_mcpSideSelector.gridx = 1;
		gbc_mcpSideSelector.gridy = 1;
		panel.add(mcpSideSelector, gbc_mcpSideSelector);
		mcpSideSelector.setModel(new DefaultComboBoxModel(Side.values()));
		
		chckbxUseSrgNames = new JCheckBox("Use SRG names");
		GridBagConstraints gbc_chckbxUseSrgNames = new GridBagConstraints();
		gbc_chckbxUseSrgNames.anchor = GridBagConstraints.WEST;
		gbc_chckbxUseSrgNames.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxUseSrgNames.gridx = 1;
		gbc_chckbxUseSrgNames.gridy = 2;
		panel.add(chckbxUseSrgNames, gbc_chckbxUseSrgNames);
		
		btnGoMCP = new JButton("Go");
		btnGoMCP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				goWithMCP();
			}
		});
		GridBagConstraints gbc_btnGoMCP = new GridBagConstraints();
		gbc_btnGoMCP.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnGoMCP.gridwidth = 3;
		gbc_btnGoMCP.gridx = 0;
		gbc_btnGoMCP.gridy = 4;
		panel.add(btnGoMCP, gbc_btnGoMCP);
		
		panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.addTab("Forge", null, panel_1, null);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		lblForgeUserdevjar = new JLabel("Forge userdev.jar:");
		GridBagConstraints gbc_lblForgeUserdevjar = new GridBagConstraints();
		gbc_lblForgeUserdevjar.anchor = GridBagConstraints.EAST;
		gbc_lblForgeUserdevjar.insets = new Insets(0, 0, 5, 5);
		gbc_lblForgeUserdevjar.gridx = 0;
		gbc_lblForgeUserdevjar.gridy = 0;
		panel_1.add(lblForgeUserdevjar, gbc_lblForgeUserdevjar);
		
		txtForgeJar = new JTextField();
		txtForgeJar.setColumns(10);
		GridBagConstraints gbc_txtForgeJar = new GridBagConstraints();
		gbc_txtForgeJar.insets = new Insets(0, 0, 5, 5);
		gbc_txtForgeJar.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtForgeJar.gridx = 1;
		gbc_txtForgeJar.gridy = 0;
		panel_1.add(txtForgeJar, gbc_txtForgeJar);
		
		btnBrowseForgeJar = new JButton("Browse");
		btnBrowseForgeJar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				File curDir = new File(txtForgeJar.getText());
				if(!curDir.isDirectory())
					curDir = new File(".");
				fc.setCurrentDirectory(curDir);
				
				fc.setFileFilter(new FileNameExtensionFilter("JAR files", "jar", "zip"));
				
				if(fc.showOpenDialog(frmBeardedOctoNemesis) == JFileChooser.APPROVE_OPTION) {
					prefs.put(PREFS_KEY_FORGEJAR, fc.getSelectedFile().getAbsolutePath());
					
					txtForgeJar.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		GridBagConstraints gbc_btnBrowseForgeJar = new GridBagConstraints();
		gbc_btnBrowseForgeJar.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseForgeJar.gridx = 2;
		gbc_btnBrowseForgeJar.gridy = 0;
		panel_1.add(btnBrowseForgeJar, gbc_btnBrowseForgeJar);
		
		btnGoForge = new JButton("Go");
		btnGoForge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				goWithLocalForge();
			}
		});
		
		lblForgegradleCacheFolder = new JLabel("FG cache folder:");
		GridBagConstraints gbc_lblForgegradleCacheFolder = new GridBagConstraints();
		gbc_lblForgegradleCacheFolder.anchor = GridBagConstraints.EAST;
		gbc_lblForgegradleCacheFolder.insets = new Insets(0, 0, 5, 5);
		gbc_lblForgegradleCacheFolder.gridx = 0;
		gbc_lblForgegradleCacheFolder.gridy = 1;
		panel_1.add(lblForgegradleCacheFolder, gbc_lblForgegradleCacheFolder);
		
		txtFGCache = new JTextField();
		txtFGCache.setToolTipText("The \".gradle/caches/minecraft/net/minecraftforge/forge\" folder.");
		txtFGCache.setColumns(10);
		GridBagConstraints gbc_txtFGCache = new GridBagConstraints();
		gbc_txtFGCache.insets = new Insets(0, 0, 5, 5);
		gbc_txtFGCache.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFGCache.gridx = 1;
		gbc_txtFGCache.gridy = 1;
		panel_1.add(txtFGCache, gbc_txtFGCache);
		
		btnBrowseFGCache = new JButton("Browse");
		btnBrowseFGCache.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				File curDir = new File(txtFGCache.getText());
				if(!curDir.isDirectory())
					curDir = new File(".");
				fc.setCurrentDirectory(curDir);
				
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				if(fc.showOpenDialog(frmBeardedOctoNemesis) == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					
					if(new File(file, ".gradle").isDirectory()) file = new File(file, ".gradle");
					if(new File(file, "caches").isDirectory()) file = new File(file, "caches");
					if(new File(file, "minecraft").isDirectory()) file = new File(file, "minecraft");
					if(new File(file, "net").isDirectory()) file = new File(file, "net");
					if(new File(file, "minecraftforge").isDirectory()) file = new File(file, "minecraftforge");
					if(new File(file, "forge").isDirectory()) file = new File(file, "forge");
					
					prefs.put(PREFS_KEY_FGCACHE, file.getAbsolutePath());
					txtFGCache.setText(file.getAbsolutePath());
				}
			}
		});
		GridBagConstraints gbc_btnBrowseFGCache = new GridBagConstraints();
		gbc_btnBrowseFGCache.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseFGCache.gridx = 2;
		gbc_btnBrowseFGCache.gridy = 1;
		panel_1.add(btnBrowseFGCache, gbc_btnBrowseFGCache);
		
		lblThisIsNot = new JLabel("This is not done, and will still download stuff.");
		GridBagConstraints gbc_lblThisIsNot = new GridBagConstraints();
		gbc_lblThisIsNot.insets = new Insets(0, 0, 5, 5);
		gbc_lblThisIsNot.gridx = 1;
		gbc_lblThisIsNot.gridy = 2;
		panel_1.add(lblThisIsNot, gbc_lblThisIsNot);
		GridBagConstraints gbc_btnGoForge = new GridBagConstraints();
		gbc_btnGoForge.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnGoForge.gridwidth = 3;
		gbc_btnGoForge.gridx = 0;
		gbc_btnGoForge.gridy = 3;
		panel_1.add(btnGoForge, gbc_btnGoForge);
		
		panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.addTab("Forge (auto-download)", null, panel_2, null);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		label_1 = new JLabel("Forge version:");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.anchor = GridBagConstraints.EAST;
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 0;
		gbc_label_1.gridy = 0;
		panel_2.add(label_1, gbc_label_1);
		
		downloadForgeVersionSelector = new JComboBox();
		GridBagConstraints gbc_downloadForgeVersionSelector = new GridBagConstraints();
		gbc_downloadForgeVersionSelector.fill = GridBagConstraints.HORIZONTAL;
		gbc_downloadForgeVersionSelector.insets = new Insets(0, 0, 5, 5);
		gbc_downloadForgeVersionSelector.gridx = 1;
		gbc_downloadForgeVersionSelector.gridy = 0;
		panel_2.add(downloadForgeVersionSelector, gbc_downloadForgeVersionSelector);
		
		btnGetVersionList = new JButton("Get version list");
		btnGetVersionList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getForgeVersionList();
			}
		});
		GridBagConstraints gbc_btnGetVersionList = new GridBagConstraints();
		gbc_btnGetVersionList.insets = new Insets(0, 0, 5, 0);
		gbc_btnGetVersionList.gridx = 2;
		gbc_btnGetVersionList.gridy = 0;
		panel_2.add(btnGetVersionList, gbc_btnGetVersionList);
		
		lblNoteThisWill = new JLabel("Note: This will automatically download Minecraft Forge from the Internet.");
		GridBagConstraints gbc_lblNoteThisWill = new GridBagConstraints();
		gbc_lblNoteThisWill.insets = new Insets(0, 0, 5, 0);
		gbc_lblNoteThisWill.gridwidth = 3;
		gbc_lblNoteThisWill.gridx = 0;
		gbc_lblNoteThisWill.gridy = 1;
		panel_2.add(lblNoteThisWill, gbc_lblNoteThisWill);
		
		btnGoForgeDownload = new JButton("Go");
		GridBagConstraints gbc_btnGoForgeDownload = new GridBagConstraints();
		gbc_btnGoForgeDownload.fill = GridBagConstraints.BOTH;
		gbc_btnGoForgeDownload.gridwidth = 3;
		gbc_btnGoForgeDownload.gridx = 0;
		gbc_btnGoForgeDownload.gridy = 2;
		panel_2.add(btnGoForgeDownload, gbc_btnGoForgeDownload);
		btnGoForgeDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				goWithDownloadedForge();
			}
		});
	}
	
	private void goWithForge(final InputStream userdevJarIn) {
		final Operation op = (Operation)operationSelector.getSelectedItem();
		if(op == Operation.ReobfuscateMod || op == Operation.SRGifyMod) {
			displayError("Only '"+Operation.ReobfuscateModSRG+"' and '"+Operation.DeobfuscateMod+"' are supported with Forge mods.");
			return;
		}
		
		final File fgCacheDirRoot = new File(txtFGCache.getText());
		if(!fgCacheDirRoot.isDirectory()) {
			displayError("Not a directory: "+txtFGCache.getText());
			return;
		}
		
		final DownloadDialog dlg = new DownloadDialog(frmBeardedOctoNemesis);
		dlg.setTitle("Running...");
		dlg.label.setText("");
		dlg.setModalityType(ModalityType.MODELESS);
		dlg.setVisible(true);
		
		dlg.progressBar.setValue(0);
		
		final File inputFile = new File(txtInputFile.getText());
		final File outputFile = new File(txtOutputFile.getText());
		
		Thread curTask = new Thread() {
			@Override
			public void run() {
				boolean crashed = false;
				
				try {
					
					IProgressListener progress = new IProgressListener() {
						private String currentText;
						
						@Override
						public void start(final int max, final String text) {
							currentText = text.equals("") ? " " : text;
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									dlg.label.setText(currentText);
									if(max >= 0)
										dlg.progressBar.setMaximum(max);
									dlg.progressBar.setValue(0);
								}
							});
						}
						
						@Override
						public void set(final int value) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									dlg.progressBar.setValue(value);
								}
							});
						}
						
						@Override
						public void setMax(final int max) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									dlg.progressBar.setMaximum(max);
								}
							});
						}
					};
					
					progress.start(1, "Reading userdev jar file");
					SrgFile packagedSRG = null;
					ExcFile packagedEXC = null;
					Map<String, String> fieldsCSV = null;
					Map<String, String> methodsCSV = null;
					String forgeVer = null;
					try (ZipInputStream zis = new ZipInputStream(userdevJarIn)) {
						ZipEntry ze;
						while((ze = zis.getNextEntry()) != null) {
							if(ze.getName().equals("conf/packaged.exc"))
								packagedEXC = ExcFile.read(zis);
							if(ze.getName().equals("conf/packaged.srg"))
								packagedSRG = SrgFile.read(new InputStreamReader(zis, StandardCharsets.UTF_8), false);
							if(ze.getName().equals("conf/fields.csv"))
								fieldsCSV = CsvFile.read(new InputStreamReader(zis, StandardCharsets.UTF_8), new int[] {2, 1, 0});
							if(ze.getName().equals("conf/methods.csv"))
								methodsCSV = CsvFile.read(new InputStreamReader(zis, StandardCharsets.UTF_8), new int[] {2, 1, 0});
							if(ze.getName().startsWith("forge-") && ze.getName().endsWith("-changelog.txt"))
								forgeVer = ze.getName().substring(6, ze.getName().length()-14);
						}
					}
					progress.set(0);
					
					if(packagedSRG == null) throw new Exception("conf/packaged.srg not found in Forge jar file");
					if(packagedEXC == null) throw new Exception("conf/packaged.exc not found in Forge jar file");
					if(fieldsCSV == null) throw new Exception("conf/fields.csv not found in Forge jar file");
					if(methodsCSV == null) throw new Exception("conf/methods.csv not found in Forge jar file");
					if(forgeVer == null) throw new Exception("unable to determine Forge version from jar file");
					
					File fgCacheDir = new File(fgCacheDirRoot, forgeVer);
					if(!fgCacheDir.isDirectory())
						throw new Exception("ForgeGradle cache directory doesn't exist: "+fgCacheDir+". Is the directory set correctly, and is this version of Forge installed?");
					
					
					String mcVer = "unknown";
					immibis.bon.mcp.MinecraftNameSet.Side side = immibis.bon.mcp.MinecraftNameSet.Side.UNIVERSAL;
					
					MappingLoader_MCP loader = new MappingLoader_MCP();
					progress.start(0, "Loading mappings and configuration");
					loader.load(side, mcVer, packagedEXC, packagedSRG, fieldsCSV, methodsCSV, progress);
					MappingFactory.registerMCPInstance(mcVer, side, loader);
					
					
					
					MinecraftNameSet refNS = new MinecraftNameSet(MinecraftNameSet.Type.MCP, side, mcVer);
					Map<String, ClassCollection> refCCList = new HashMap<>();
					
					for(File refPathFile : new File[] {new File(fgCacheDir, "forgeSrc-"+forgeVer+".jar")}) {
						System.err.println(refPathFile.getAbsolutePath());
						progress.start(0, "Reading "+refPathFile.getName());
						refCCList.put(refPathFile.getName(), ClassCollectionFactory.loadClassCollection(refNS, refPathFile, progress));
						
						//progress.start(0, "Remapping "+s);
						//refs.add(Remapper.remap(mcpRefCC, inputNS, Collections.<ClassCollection>emptyList(), progress));
					}
					
					MinecraftNameSet.Type[] remapTo;
					MinecraftNameSet.Type inputType;
					
					switch(op) {
					case DeobfuscateMod:
						inputType = MinecraftNameSet.Type.SRG;
						remapTo = new MinecraftNameSet.Type[] {MinecraftNameSet.Type.MCP};
						break;
						
					case ReobfuscateModSRG:
						inputType = MinecraftNameSet.Type.MCP;
						remapTo = new MinecraftNameSet.Type[] {MinecraftNameSet.Type.SRG};
						break;
						
					default:
						throw new AssertionError("operation = "+op+"?");
					}
					
					NameSet inputNS = new MinecraftNameSet(inputType, side, mcVer);
					
					progress.start(0, "Reading "+inputFile.getName());
					ClassCollection inputCC = ClassCollectionFactory.loadClassCollection(inputNS, inputFile, progress);
					
					
					
					// For deobfuscation:
					/*                       MCP reference
					 *                       |           |
					 *                       |           |
					 *                       |           |
					 *                       V           V
					 *             OBF reference       SRG reference
					 *                 |                     |
					 *                 |                     |
					 *                 V                     V
					 * OBF input -----------> SRG input -----------> MCP input (output file)
					 */
					
					
					
					
					// remap to obf names from searge names, then searge names to MCP names, in two steps
					// the first will be a no-op if the mod uses searge names already
					for(MinecraftNameSet.Type outputType : remapTo) {
						MinecraftNameSet outputNS = new MinecraftNameSet(outputType, side, mcVer);
						
						List<ReferenceDataCollection> remappedRefs = new ArrayList<>();
						for(Map.Entry<String, ClassCollection> e : refCCList.entrySet()) {
							
							if(inputCC.getNameSet().equals(e.getValue().getNameSet())) {
								// no need to remap this
								remappedRefs.add(ReferenceDataCollection.fromClassCollection(e.getValue()));
								
							} else {
								progress.start(0, "Remapping "+e.getKey()+" to "+outputType+" names");
								remappedRefs.add(ReferenceDataCollection.fromClassCollection(Remapper.remap(e.getValue(), MappingFactory.getMapping((MinecraftNameSet)e.getValue().getNameSet(), (MinecraftNameSet)inputCC.getNameSet(), null), Collections.<ReferenceDataCollection>emptyList(), progress)));
							}
						}
						
						progress.start(0, "Remapping "+inputFile.getName()+" to "+outputType+" names");
						inputCC = Remapper.remap(inputCC, MappingFactory.getMapping((MinecraftNameSet)inputCC.getNameSet(), outputNS, null), remappedRefs, progress);
					}
					
					progress.start(0, "Writing "+outputFile.getName());
					JarWriter.write(outputFile, inputCC, progress);
					
				} catch(Exception e) {
					String s = getStackTraceMessage(e);
					
					System.err.println(s);
					
					crashed = true;
					
					final String errMsg = s;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							dlg.setVisible(false);
							dlg.dispose();
							
							Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(errMsg), null);
							JOptionPane.showMessageDialog(frmBeardedOctoNemesis, errMsg, "BON - Error", JOptionPane.ERROR_MESSAGE);
						}
					});
				} finally {
					if(!crashed) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								dlg.setVisible(false);
								dlg.dispose();
								
								JOptionPane.showMessageDialog(frmBeardedOctoNemesis, "Done!", "BON", JOptionPane.INFORMATION_MESSAGE);
							}
						});
					}
				}
			}
		};
		curTask.setName("remap thread");
		curTask.setDaemon(true);
		curTask.start();
	}
	
	private void displayError(String msg) {
		JOptionPane.showMessageDialog(frmBeardedOctoNemesis, msg, "Error - Bearded Octo Nemesis", JOptionPane.ERROR_MESSAGE);
	}
	
	private void goWithLocalForge() {
		try {
			goWithForge(new FileInputStream(new File(txtForgeJar.getText())));
		} catch(FileNotFoundException e) {
			displayError("File not found: "+txtForgeJar.getText());
		}
	}
	
	private void goWithDownloadedForge() {
		String selver = (String)downloadForgeVersionSelector.getSelectedItem();
		if(selver == null || selver.equals(""))
			displayError("You must select a Forge version.");
		else {
			try {
				URL url = new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/"+selver+"/forge-"+selver+"-userdev.jar");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DownloadDialog.download(frmBeardedOctoNemesis, url, baos);
				goWithForge(new ByteArrayInputStream(baos.toByteArray()));
			} catch(Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frmBeardedOctoNemesis, e.toString(), "Error - Bearded Octo Nemesis", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void getForgeVersionList() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DownloadDialog.download(frmBeardedOctoNemesis, new URL("http://files.minecraftforge.net/maven/net/minecraftforge/forge/json"), baos);
			
			Object jsonroot = JsonReader.readJSON(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())));
			
			List<String> buildNumbers = new ArrayList<>(((Map<String,Map<String,?>>)jsonroot).get("number").keySet());
			Collections.sort(buildNumbers, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return Integer.parseInt(o2) - Integer.parseInt(o1);
				}
			});
			
			DefaultComboBoxModel<String> model = ((DefaultComboBoxModel<String>)downloadForgeVersionSelector.getModel());
			model.removeAllElements();
			for(String buildNum : buildNumbers) {
				Map build = ((Map<String,Map<String,Map>>)jsonroot).get("number").get(buildNum);
				model.addElement(build.get("mcversion")+"-"+build.get("version"));
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frmBeardedOctoNemesis, e.toString(), "Error - Bearded Octo Nemesis", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	
	
	
	
	private void goWithMCP() {
		final Operation op = (Operation)operationSelector.getSelectedItem();
		final Side side = (Side)mcpSideSelector.getSelectedItem();
		
		final File mcpDir = new File(txtMCPDir.getText());
		final File confDir = new File(mcpDir, "conf");
		final String[] refPathList = side.referencePath.split(File.pathSeparator);
		
		String error = null;
		
		if(!mcpDir.isDirectory())
			error = "MCP folder not found (at "+mcpDir+")";
		else if(!confDir.isDirectory())
			error = "'conf' folder not found in MCP folder (at "+confDir+")";
		
		if(error != null) {
			displayError(error);
			return;
		}
		
		final DownloadDialog dlg = new DownloadDialog(frmBeardedOctoNemesis);
		dlg.setTitle("Running...");
		dlg.label.setText("");
		dlg.setModalityType(ModalityType.MODELESS);
		dlg.setVisible(true);
		
		dlg.progressBar.setValue(0);
		
		final File inputFile = new File(txtInputFile.getText());
		final File outputFile = new File(txtOutputFile.getText());
		
		Thread curTask = new Thread() {
			@Override
			public void run() {
				boolean crashed = false;
				
				try {
					
					IProgressListener progress = new IProgressListener() {
						private String currentText;
						
						@Override
						public void start(final int max, final String text) {
							currentText = text.equals("") ? " " : text;
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									dlg.label.setText(currentText);
									if(max >= 0)
										dlg.progressBar.setMaximum(max);
									dlg.progressBar.setValue(0);
								}
							});
						}
						
						@Override
						public void set(final int value) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									dlg.progressBar.setValue(value);
								}
							});
						}
						
						@Override
						public void setMax(final int max) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									dlg.progressBar.setMaximum(max);
								}
							});
						}
					};
					
					
					
					String mcVer = MappingLoader_MCP.getMCVer(mcpDir);
					
					MinecraftNameSet refNS = new MinecraftNameSet(MinecraftNameSet.Type.MCP, side.nsside, mcVer);
					Map<String, ClassCollection> refCCList = new HashMap<>();
					
					for(String s : refPathList) {
						File refPathFile = new File(mcpDir, s);
						
						progress.start(0, "Reading "+s);
						refCCList.put(s, ClassCollectionFactory.loadClassCollection(refNS, refPathFile, progress));
						
						//progress.start(0, "Remapping "+s);
						//refs.add(Remapper.remap(mcpRefCC, inputNS, Collections.<ClassCollection>emptyList(), progress));
					}
					
					MinecraftNameSet.Type[] remapTo;
					MinecraftNameSet.Type inputType;
					
					switch(op) {
					case DeobfuscateMod:
						inputType = MinecraftNameSet.Type.OBF;
						remapTo = new MinecraftNameSet.Type[] {MinecraftNameSet.Type.SRG, MinecraftNameSet.Type.MCP};
						break;
						
					case ReobfuscateMod:
						inputType = MinecraftNameSet.Type.MCP;
						remapTo = new MinecraftNameSet.Type[] {MinecraftNameSet.Type.OBF};
						break;
						
					case SRGifyMod:
						inputType = MinecraftNameSet.Type.OBF;
						remapTo = new MinecraftNameSet.Type[] {MinecraftNameSet.Type.SRG};
						break;
						
					case ReobfuscateModSRG:
						inputType = MinecraftNameSet.Type.MCP;
						remapTo = new MinecraftNameSet.Type[] {MinecraftNameSet.Type.SRG};
						break;
						
					default:
						throw new AssertionError("operation = "+op+"?");
					}
					
					NameSet inputNS = new MinecraftNameSet(inputType, side.nsside, mcVer);
					
					progress.start(0, "Reading "+inputFile.getName());
					ClassCollection inputCC = ClassCollectionFactory.loadClassCollection(inputNS, inputFile, progress);
					
					progress.start(0, "Reading MCP configuration");
					MappingFactory.registerMCPInstance(mcVer, side.nsside, mcpDir, progress);
					
					
					
					// For deobfuscation:
					/*                       MCP reference
					 *                       |           |
					 *                       |           |
					 *                       |           |
					 *                       V           V
					 *             OBF reference       SRG reference
					 *                 |                     |
					 *                 |                     |
					 *                 V                     V
					 * OBF input -----------> SRG input -----------> MCP input (output file)
					 */
					
					
					
					
					// remap to obf names from searge names, then searge names to MCP names, in two steps
					// the first will be a no-op if the mod uses searge names already
					for(MinecraftNameSet.Type outputType : remapTo) {
						MinecraftNameSet outputNS = new MinecraftNameSet(outputType, side.nsside, mcVer);
						
						List<ReferenceDataCollection> remappedRefs = new ArrayList<>();
						for(Map.Entry<String, ClassCollection> e : refCCList.entrySet()) {
							
							if(inputCC.getNameSet().equals(e.getValue().getNameSet())) {
								// no need to remap this
								remappedRefs.add(ReferenceDataCollection.fromClassCollection(e.getValue()));
								
							} else {
								progress.start(0, "Remapping "+e.getKey()+" to "+outputType+" names");
								remappedRefs.add(ReferenceDataCollection.fromClassCollection(Remapper.remap(e.getValue(), MappingFactory.getMapping((MinecraftNameSet)e.getValue().getNameSet(), (MinecraftNameSet)inputCC.getNameSet(), null), Collections.<ReferenceDataCollection>emptyList(), progress)));
							}
						}
						
						progress.start(0, "Remapping "+inputFile.getName()+" to "+outputType+" names");
						inputCC = Remapper.remap(inputCC, MappingFactory.getMapping((MinecraftNameSet)inputCC.getNameSet(), outputNS, null), remappedRefs, progress);
					}
					
					progress.start(0, "Writing "+outputFile.getName());
					JarWriter.write(outputFile, inputCC, progress);
					
				} catch(Exception e) {
					String s = getStackTraceMessage(e);
					
					/*if(!new File(confDir, side.nsside.srg_name).exists()) {
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
					}*/
					
					System.err.println(s);
					
					crashed = true;
					
					final String errMsg = s;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							dlg.setVisible(false);
							dlg.dispose();
							
							Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(errMsg), null);
							JOptionPane.showMessageDialog(frmBeardedOctoNemesis, errMsg, "BON - Error", JOptionPane.ERROR_MESSAGE);
						}
					});
				} finally {
					if(!crashed) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								dlg.setVisible(false);
								dlg.dispose();
								
								JOptionPane.showMessageDialog(frmBeardedOctoNemesis, "Done!", "BON", JOptionPane.INFORMATION_MESSAGE);
							}
						});
					}
				}
			}
		};
		curTask.setName("remap thread");
		curTask.setDaemon(true);
		curTask.start();
	}
	
	private static String getPrintableStackTrace(Throwable e, Set<StackTraceElement> stopAt) {
		String s = e.toString();
		int numPrinted = 0;
		for(StackTraceElement ste : e.getStackTrace())
		{
			boolean stopHere = false;
			if(stopAt.contains(ste) && numPrinted > 0)
				stopHere = true;
			else {
				s += "\n    at " + ste.toString();
				numPrinted++;
				if(ste.getClassName().startsWith("javax.swing."))
					stopHere = true;
			}
			
			if(stopHere) {
				int numHidden = e.getStackTrace().length - numPrinted;
				s += "\n    ... "+numHidden+" more";
				break;
			}
		}
		return s;
	}
	
	private static String getStackTraceMessage(Throwable e) {
		String s = "An error has occurred - give immibis this stack trace (which has been copied to the clipboard)\n";
		
		s += "\n" + getPrintableStackTrace(e, Collections.<StackTraceElement>emptySet());
		while(e.getCause() != null) {
			Set<StackTraceElement> stopAt = new HashSet<StackTraceElement>(Arrays.asList(e.getStackTrace()));
			e = e.getCause();
			s += "\nCaused by: "+getPrintableStackTrace(e, stopAt);
		}
		return s;
	}
}
