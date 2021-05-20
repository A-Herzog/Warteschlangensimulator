/**
 * Copyright 2021 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scripting.java;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import ui.help.Help;
import ui.script.ScriptEditorAreaBuilder;
import ui.script.ScriptPopup;
import ui.tools.FlatLaFHelper;

/**
 * Zeigt einen Fehlermeldungsdialog zu einem Skript-Objekt an.
 * @author Alexander Herzog
 * @see DynamicRunner
 */
public class DynamicErrorInfo extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=387299555439019838L;

	/**
	 * Zeilennummer an der der Fehler aufgetreten ist.<br>
	 * (Ist per Default 0, wenn keine Zeilennummer ermittelt werden konnte. Sonst 1-basierend.)
	 */
	private int lineNr;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param runner	Skript-Objekt aus dem die Fehlerdaten entnommen werden sollen
	 */
	public DynamicErrorInfo(final Component owner, final DynamicRunner runner) {
		super(owner,Language.tr("Simulation.Java.Error.Title"));

		/* GUI */
		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(this,"Java"));
		content.setLayout(new BorderLayout());

		/* Fehler-Farbe */
		final Color errorColor;
		if (FlatLaFHelper.isDark()) {
			errorColor=new Color(96,0,0);
		} else {
			errorColor=new Color(255,128,128);
		}

		/* Fehlermeldung */
		final JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(top,BorderLayout.NORTH);
		top.setBorder(BorderFactory.createEmptyBorder(5,5,10,5));
		top.setBackground(errorColor);
		top.add(new JLabel(processErrorMessage(runner)));

		/* Skript-Anzeige */
		final ScriptEditorAreaBuilder builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Java,true,null);
		final RSyntaxTextArea editor=builder.get();
		editor.setText(runner.getFullClass());
		content.add(new RTextScrollPane (editor),BorderLayout.CENTER);
		if (lineNr>0) try {
			editor.addLineHighlight(lineNr-1,errorColor);
		} catch (BadLocationException e) {}

		/* Dialog starten */
		setMinSizeRespectingScreensize(800,600);
		setSizeRespectingScreensize(800,600);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Erzeugt eine html-Fehlermeldung.
	 * @param runner	Skript-Objekt aus dem die Fehlermeldung entnommen werden soll
	 * @return	html-Fehlermeldung
	 */
	private String processErrorMessage(final DynamicRunner runner) {
		String msg=Language.tr("Simulation.Java.Error.CompileError.Line");
		final int index=msg.indexOf(" %d");
		if (index<0) msg=null; else msg=msg.substring(0,index);

		final StringBuilder message=new StringBuilder();
		message.append("<html><body>\n");
		message.append("<p><b>"+DynamicFactory.getStatusText(runner.getStatus())+"</b></p>\n");
		if (runner.getError()!=null) {
			message.append("<p>");
			for (String line: runner.getError().split("\\n")) {
				message.append(line);
				message.append("<br>\n");
				if (msg!=null && line.startsWith(msg)) {
					String part=line.substring(msg.length()).trim();
					for (int i=0;i<part.length();i++) if (part.charAt(i)<'0' || part.charAt(i)>'9') {
						if (i>0) {
							final Integer I=NumberTools.getInteger(part.substring(0,i));
							if (I!=null && I>0) lineNr=I.intValue();
						}
					}
				}
			}
			message.append("</p>\n");
		}
		message.append("</body></html>\n");
		return message.toString();
	}
}
