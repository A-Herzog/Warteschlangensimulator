package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import language.Language;
import systemtools.BaseDialog;
import tools.SetupData;
import ui.help.Help;
import ui.modeleditor.HeatMapImage;
import ui.modeleditor.fastpaint.GradientFill;

/**
 * Zeigt einen Dialog zur Konfiguration der Heatmap-Darstellung an.
 * @author Alexander Herzog
 * @see HeatMapImage
 */
public class HeatMapSetupDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-1654215525875237736L;

	/**
	 * Vordefinierte Farbwerte f�r die Heatmaps
	 */
	private static final Color[][] HEATMAP_COLORS=new Color[][] {
		new Color[] {Color.RED,Color.BLUE},
		new Color[] {Color.GREEN,Color.RED},
		new Color[] {Color.WHITE,Color.RED},
		new Color[] {Color.WHITE,Color.BLUE},
		new Color[] {Color.YELLOW,Color.BLUE}
	};

	/**
	 * Gr��e der Heatmap-Wolken
	 */
	private final SpinnerNumberModel heatMapSize;

	/**
	 * Deckkraft der Heatmap-Farbe bei einer Intensit�t von 0%
	 */
	private final SpinnerNumberModel heatMapIntensityMin;

	/**
	 * Deckkraft der Heatmap-Farbe bei einer Intensit�t von 100%
	 */
	private final SpinnerNumberModel heatMapIntensityMax;

	/**
	 * Auswahl der Farben f�r die Heatmaps
	 */
	private final List<JRadioButton> heatMapColors;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 */
	public HeatMapSetupDialog(final Component owner) {
		super(owner,Language.tr("HeatMapSetup.Title"));

		final SetupData setup=SetupData.getSetup();

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this,"HeatMap"));
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		all.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Gr��e der Heatmap-Wolken */
		heatMapSize=addSpinner(content,Language.tr("HeatMapSetup.Size"),1,500,setup.statisticHeatMapSize,null);

		/* Intensit�t */
		heatMapIntensityMin=addSpinner(content,Language.tr("HeatMapSetup.Intensity.Low"),0,100,(int)Math.round(setup.statisticHeatMapIntensityMin*100),"%");
		heatMapIntensityMax=addSpinner(content,Language.tr("HeatMapSetup.Intensity.High"),0,100,(int)Math.round(setup.statisticHeatMapIntensityMax*100),"%");

		/* Farbverlauf */
		final ButtonGroup buttonGroup=new ButtonGroup();
		heatMapColors=new ArrayList<>();
		boolean hasMatch=false;
		for (Color[] colors: HEATMAP_COLORS) {
			final boolean select=(colors[0].equals(setup.statisticHeatMapColorLow) && colors[1].equals(setup.statisticHeatMapColorHigh));
			if (select) hasMatch=true;
			final JRadioButton radio=addColorGradient(content,colors[0],colors[1],select);
			heatMapColors.add(radio);
			buttonGroup.add(radio);
		}
		if (!hasMatch) heatMapColors.get(0).setSelected(true);

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Erstellt ein Auswahlfeld und f�gt es in ein Panel ein
	 * @param panel	Panel in das das Auswahlfeld eingef�gt werden soll
	 * @param text	Beschriftung des Auswahlfeldes
	 * @param min	Minimaler Wert
	 * @param max	Maximaler Wert
	 * @param value	Initialer Wert
	 * @param info	Optionaler Infotext nach dem Feld (kann <code>null</code> sein)
	 * @return	Neues Auswahlfeld
	 */
	private SpinnerNumberModel addSpinner(final JPanel panel, final String text, final int min, final int max, final int value, final String info) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		final JLabel label=new JLabel(text+":");
		line.add(label);
		final JSpinner spinner=new JSpinner(new SpinnerNumberModel(value,min,max,1));
		line.add(spinner);
		label.setLabelFor(spinner);
		if (info!=null) line.add(new JLabel(info));
		return (SpinnerNumberModel)spinner.getModel();
	}

	/**
	 * Erstellt ein Farbverlauf-Auswahl-Radiobutton und f�gr es in ein Panel ein
	 * @param panel	Panel in das das Auswahlfeld eingef�gt werden soll
	 * @param color1	Farbe f�r niedrige Intensit�t (links)
	 * @param color2	Farbe f�r hohe Intensit�t (rechts)
	 * @param select	Soll die Option ausgew�hlt dargestellt werden?
	 * @return	Neues Radiobutton
	 */
	private JRadioButton addColorGradient(final JPanel panel, final Color color1, final Color color2, final boolean select) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		final JRadioButton radio=new JRadioButton();
		line.add(radio);
		radio.setSelected(select);
		final ColorPanel color=new ColorPanel(color1,color2);
		line.add(color);
		color.setMinimumSize(new Dimension(250,20));
		color.setSize(new Dimension(250,20));
		color.setPreferredSize(new Dimension(250,20));
		color.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {radio.setSelected(true);}
		});
		return radio;
	}

	@Override
	protected void storeData() {
		final SetupData setup=SetupData.getSetup();

		/* Gr��e der Heatmap-Wolken */
		setup.statisticHeatMapSize=(Integer)heatMapSize.getValue();

		/* Intensit�t */
		setup.statisticHeatMapIntensityMin=((Integer)heatMapIntensityMin.getValue()).intValue()/100.0;
		setup.statisticHeatMapIntensityMax=((Integer)heatMapIntensityMax.getValue()).intValue()/100.0;

		/* Farbverlauf */
		for (int i=0;i<heatMapColors.size();i++) if (heatMapColors.get(i).isSelected()) {
			setup.statisticHeatMapColorLow=HEATMAP_COLORS[i][0];
			setup.statisticHeatMapColorHigh=HEATMAP_COLORS[i][1];
			break;
		}

		setup.saveSetup();
	}

	/**
	 * Panel in dem ein Farbverlauf angezeigt wird
	 * @see HeatMapSetupDialog#addColorGradient(JPanel, Color, Color, boolean)
	 */
	private static class ColorPanel extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=6761202590771458510L;

		/**
		 * Farbe f�r niedrige Intensit�t (links)
		 */
		private final Color color1;

		/**
		 * Farbe f�r hohe Intensit�t (rechts)
		 */
		private final Color color2;

		/**
		 * Farbe f�r den Text am linken Rand<br>
		 * (Komplement�rfarbe zu {@link #color1})
		 */
		private final Color textColor1;

		/**
		 * Farbe f�r den Text am rechten Rand<br>
		 * (Komplement�rfarbe zu {@link #color2})
		 */
		private final Color textColor2;

		/**
		 * System zum Zeichnen von Farbverl�ufen
		 * @see #paint(Graphics)
		 */
		private final GradientFill filler;

		/**
		 * Cache f�r das Rechteck-Objekt, das in {@link #paint(Graphics)} verwendet wird
		 * @see #paint(Graphics)
		 */
		private final Rectangle rect;

		/**
		 * Konstruktor der Klasse
		 * @param color1	Farbe f�r niedrige Intensit�t (links)
		 * @param color2	Farbe f�r hohe Intensit�t (rechts)
		 */
		public ColorPanel(final Color color1, final Color color2) {
			this.color1=color1;
			this.color2=color2;
			textColor1=getComplementaryColor(color1);
			textColor2=getComplementaryColor(color2);
			filler=new GradientFill(true);
			rect=new Rectangle();
			rect.x=0;
			rect.y=0;
		}

		/**
		 * Liefert zu einer Farbe die Komplement�rfarbe
		 * @param color	Ausgangsfarbe
		 * @return	Komplement�rfarbe
		 */
		private Color getComplementaryColor(final Color color) {
			final int rgb=color.getRGB();

			int r=((rgb & 0xFF0000) >> 16) & 0xFF;
			int g=((rgb & 0xFF00) >> 8) & 0xFF;
			int b=rgb & 0xFF;

			r=255-r;
			g=255-g;
			b=255-b;

			return new Color((0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)  | ((b & 0xFF) << 0));
		}

		@Override
		public void paint(final Graphics graphics) {
			rect.width=getWidth();
			rect.height=getHeight();
			filler.set(graphics,rect,color1,color2,false);
			graphics.fillRect(0,0,rect.width,rect.height);
			graphics.setColor(Color.BLACK);
			graphics.drawRect(0,0,rect.width,rect.height);

			final int a=graphics.getFontMetrics().getAscent();
			final int d=graphics.getFontMetrics().getDescent();
			final int y=(rect.height-(a+d))/2+a;

			graphics.setColor(textColor1);
			graphics.drawString(Language.tr("HeatMapSetup.Intensity.Low.Short"),5,y);
			graphics.setColor(textColor2);
			graphics.drawString(Language.tr("HeatMapSetup.Intensity.High.Short"),rect.width-5-graphics.getFontMetrics().stringWidth(Language.tr("HeatMapSetup.Intensity.High.Short")),y);
		}
	}
}
