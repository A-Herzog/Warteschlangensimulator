package ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import simulator.statistics.Statistics;
import tools.SetupData;
import ui.dialogs.InfoDialog;

/**
 * Fasst alle aktuellen Programmdaten zusammen und speichert
 * diese in einer zip-Datei.
 * @author Alexander Herzog
 */
public class ProblemReporter {
	private File file;

	/**
	 * Konstruktor der Klasse
	 * @param file	Datei in die die Ausgabe erfolgen soll
	 */
	public ProblemReporter(final File file) {
		this.file=file;
	}

	private boolean processRecords(final ZipOutputStream zipOutput) throws IOException {
		zipOutput.putNextEntry(new ZipEntry("setup.xml"));
		SetupData.getSetup().saveToStream(zipOutput);

		if (SetupData.getSetup().lastError!=null) {
			zipOutput.putNextEntry(new ZipEntry("error.txt"));
			zipOutput.write(SetupData.getSetup().lastError.getBytes(StandardCharsets.UTF_8));
		}

		zipOutput.putNextEntry(new ZipEntry("system.txt"));
		zipOutput.write(String.join("\n",InfoDialog.getSystemInfo().toArray(new String[0])).getBytes(StandardCharsets.UTF_8));

		zipOutput.putNextEntry(new ZipEntry("version.txt"));
		zipOutput.write(MainPanel.VERSION.getBytes(StandardCharsets.UTF_8));

		zipOutput.putNextEntry(new ZipEntry("java.txt"));
		final StringBuilder java=new StringBuilder();
		for (Map.Entry<Object,Object> entry: System.getProperties().entrySet()) {
			java.append(entry.getKey()+"="+entry.getValue());
			java.append('\n');
		}
		zipOutput.write(java.toString().getBytes(StandardCharsets.UTF_8));

		zipOutput.putNextEntry(new ZipEntry("environment.txt"));
		final StringBuilder env=new StringBuilder();
		for (Map.Entry<String,String> entry: System.getenv().entrySet()) {
			env.append(entry.getKey()+"="+entry.getValue());
			env.append('\n');
		}
		zipOutput.write(env.toString().getBytes(StandardCharsets.UTF_8));

		int count=0;
		for (MainFrame frame: ReloadManager.getMainFrames()) {
			count++;

			zipOutput.putNextEntry(new ZipEntry(String.format("model%d.xml",count)));
			frame.getModel().saveToStream(zipOutput);

			final Statistics statistics=frame.getStatistics();
			if (statistics!=null) {
				zipOutput.putNextEntry(new ZipEntry(String.format("statistics%d.xml",count)));
				statistics.saveToStream(zipOutput);
			}

			zipOutput.putNextEntry(new ZipEntry(String.format("window%d.png",count)));
			final Dimension d=frame.getSize();
			final BufferedImage image=new BufferedImage(d.width,d.height,BufferedImage.TYPE_INT_RGB);
			frame.paintAll(image.getGraphics());
			ImageIO.write(image,"png",zipOutput);
		}

		return true;
	}

	/**
	 * Führt die Ausgabe durch.
	 * @return	Gibt an, ob die Ausgabe erfolgreich war.
	 */
	public boolean process() {
		try (BufferedOutputStream fileOutput=new BufferedOutputStream(new FileOutputStream(file));) {
			try (ZipOutputStream zipOutput=new ZipOutputStream(fileOutput)) {
				if (!processRecords(zipOutput)) return false;
			}
		} catch (IOException e) {return false;}
		return true;
	}

	/**
	 * Zeigt einen Dateiauswahldialog zum Speichern der zip-Ausgabe an.
	 * @param owner	Übergeordnetes Element
	 * @return	Gewählte Ausgabedatei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static File selectOutputFile(final Component owner) {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("ProblemReporter.Dialog.Title"));

		final FileFilter zip=new FileNameExtensionFilter(Language.tr("ProblemReporter.Dialog.FileTypeZip")+" (*.zip)","zip");

		fc.addChoosableFileFilter(zip);
		fc.setFileFilter(zip);

		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==zip) file=new File(file.getAbsoluteFile()+".zip");
		}
		return file;
	}
}
