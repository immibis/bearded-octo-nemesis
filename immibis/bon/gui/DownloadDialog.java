package immibis.bon.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JProgressBar;

import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DownloadDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	public JLabel label;
	public JProgressBar progressBar;

	/**
	 * Create the dialog.
	 */
	public DownloadDialog(JFrame owner) {
		super(owner);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setTitle("Downloading...");
		setBounds(100, 100, 314, 81);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{97, 0};
		gbl_contentPanel.rowHeights = new int[]{20, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			label = new JLabel("<label>");
			label.setHorizontalAlignment(SwingConstants.CENTER);
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.fill = GridBagConstraints.HORIZONTAL;
			gbc_label.insets = new Insets(0, 0, 5, 0);
			gbc_label.gridx = 0;
			gbc_label.gridy = 0;
			contentPanel.add(label, gbc_label);
		}
		{
			progressBar = new JProgressBar();
			GridBagConstraints gbc_progressBar = new GridBagConstraints();
			gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
			gbc_progressBar.anchor = GridBagConstraints.BELOW_BASELINE;
			gbc_progressBar.gridx = 0;
			gbc_progressBar.gridy = 1;
			contentPanel.add(progressBar, gbc_progressBar);
		}
		
		setLocationRelativeTo(owner);
	}
	
	public static void download(final JFrame parent, final URL url, final OutputStream out) throws IOException {
		final DownloadDialog dlg = new DownloadDialog(parent);
		//String name = url.getPath().substring(url.getPath().lastIndexOf('/')+1);
		String name = url.toString();
		dlg.label.setText("Downloading "+name+"...");
		
		final AtomicReference<Throwable> exception = new AtomicReference<>(null);
		
		new Thread() {
			{setDaemon(true); setName("Downloader thread");}
			
			@Override
			public void run() {
				
				try {
					{
						// Wait for dialog to be visible.
						// This is a slightly dumb way to synchronize threads, but I don't
						// see a better way within Swing.
						final boolean[] wasVisible = new boolean[] {false};
						while(!wasVisible[0]) {
							EventQueue.invokeAndWait(new Runnable() {
								@Override
								public void run() {
									wasVisible[0] = dlg.isVisible();
								}
							});
						}
					}
					
					URLConnection c = url.openConnection();
					final int length = c.getContentLength();
					
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							if(length == -1)
								dlg.progressBar.setIndeterminate(true);
							else
								dlg.progressBar.setMaximum(length);
						};
					});
					
					class UpdateBarTask implements Runnable {
						private AtomicBoolean isRunning = new AtomicBoolean();
						private AtomicInteger progress = new AtomicInteger();
						
						@Override
						public void run() {
							dlg.progressBar.setValue(progress.get());
							isRunning.set(false);
						}
					}
					UpdateBarTask updateBarTask = new UpdateBarTask();
					
					try(InputStream in = c.getInputStream()) {
						byte[] buffer = new byte[65536];
						while(true) {
							int read = in.read(buffer);
							if(read <= 0)
								break;
							out.write(buffer, 0, read);
							
							if(length != -1) {
								updateBarTask.progress.addAndGet(read);
								if(!updateBarTask.isRunning.getAndSet(true))
									EventQueue.invokeLater(updateBarTask);
							}
						}
					}
				} catch(Throwable t) {
					exception.set(t);
				} finally {
					dlg.setVisible(false);
				}
			}
		}.start();
		
		dlg.setVisible(true);
		
		Throwable t = exception.get();
		if(t instanceof IOException)
			throw new IOException(t);
		else if(t instanceof Error)
			throw new Error(t);
		else if(t != null)
			throw new RuntimeException(t);
	}

}
