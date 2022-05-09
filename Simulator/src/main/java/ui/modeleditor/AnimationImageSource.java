/**
 * Copyright 2020 Alexander Herzog
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
package ui.modeleditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;

/**
 * Diese Klasse hält Bilder für die Animation von Modellen vor.
 * Die Bilder können dabei direkt in einer passend skalierten Größe ausgeliefert werden.
 * @author Alexander Herzog
 */
public class AnimationImageSource {
	/* Famfamfam Icons */

	/** Name für Animationssymbol "Büroklammer" */
	public static String iconNameAttach="Büroklammer";
	/** Name für Animationssymbol "Buch" */
	public static String iconNameBook="Buch";
	/** Name für Animationssymbol "Käfer" */
	public static String iconNameBug="Käfer";
	/** Name für Animationssymbol "CD" */
	public static String iconNameCD="CD";
	/** Name für Animationssymbol "Kuchen" */
	public static String iconNameCake="Kuchen";
	/** Name für Animationssymbol "Auto" */
	public static String iconNameCar="Auto";
	/** Name für Animationssymbol "Einkaufswagen" */
	public static String iconNameCart="Einkaufswagen";
	/** Name für Animationssymbol "Uhr" */
	public static String iconNameClock="Uhr";
	/** Name für Animationssymbol "Computer" */
	public static String iconNameComputer="Computer";
	/** Name für Animationssymbol "Datenbank" */
	public static String iconNameDatabase="Datenbank";
	/** Name für Animationssymbol "Diskette" */
	public static String iconNameDisk="Diskette";
	/** Name für Animationssymbol "Brief" */
	public static String iconNameLetter="Brief";
	/** Name für Animationssymbol "Smiley" */
	public static String iconNameSmiley="Smiley";
	/** Name für Animationssymbol "Symbol - Frau" */
	public static String iconNameSymbolFemale="Symbol - Frau";
	/** Name für Animationssymbol "Symbol - Mann" */
	public static String iconNameSymbolMale="Symbol - Mann";
	/** Name für Animationssymbol "Fahne - Blue" */
	public static String iconNameFlagBlue="Fahne - Blue";
	/** Name für Animationssymbol "Fahne - Grün" */
	public static String iconNameFlagGreen="Fahne - Grün";
	/** Name für Animationssymbol "Fahne - Orange" */
	public static String iconNameFlagOrange="Fahne - Orange";
	/** Name für Animationssymbol "Fahne - Pink" */
	public static String iconNameFlagPink="Fahne - Pink";
	/** Name für Animationssymbol "Fahne - Lila" */
	public static String iconNameFlagPurple="Fahne - Lila";
	/** Name für Animationssymbol "Fahne - Rot" */
	public static String iconNameFlagRed="Fahne - Rot";
	/** Name für Animationssymbol "Fahne - Gelb" */
	public static String iconNameFlagYellow="Fahne - Gelb";
	/** Name für Animationssymbol "Herz" */
	public static String iconNameHeart="Herz";
	/** Name für Animationssymbol "Schlüssel" */
	public static String iconNameKey="Schlüssel";
	/** Name für Animationssymbol "LKW" */
	public static String iconNameLorry="LKW";
	/** Name für Animationssymbol "LKW-links" */
	public static String iconNameLorryLeft="LKW-links";
	/** Name für Animationssymbol "LKW (leer)" */
	public static String iconNameLorryEmpty="LKW (leer)";
	/** Name für Animationssymbol "LKW-links (leer)" */
	public static String iconNameLorryLeftEmpty="LKW-links (leer)";
	/** Name für Animationssymbol "Geld - Dollar" */
	public static String iconNameMoneyDollar="Geld - Dollar";
	/** Name für Animationssymbol "Geld - Euro" */
	public static String iconNameMoneyEuro="Geld - Euro";
	/** Name für Animationssymbol "Geld - Pfund" */
	public static String iconNameMoneyPound="Geld - Pfund";
	/** Name für Animationssymbol "Geld - Yen" */
	public static String iconNameMoneyYen="Geld - Yen";
	/** Name für Animationssymbol "Musik" */
	public static String iconNameMusic="Musik";
	/** Name für Animationssymbol "Dokument" */
	public static String iconNameDocument="Dokument";
	/** Name für Animationssymbol "Stern" */
	public static String iconNameStar="Stern";
	/** Name für Animationssymbol "Person - Grau" */
	public static String iconNamePersonGray="Person - Grau";
	/** Name für Animationssymbol "Person - Grün" */
	public static String iconNamePersonGreen="Person - Grün";
	/** Name für Animationssymbol "Person - Orange" */
	public static String iconNamePersonOrange="Person - Orange";
	/** Name für Animationssymbol "Person - Rot" */
	public static String iconNamePersonRed="Person - Rot";
	/** Name für Animationssymbol "Person - Anzug" */
	public static String iconNamePersonSuit="Person - Anzug";
	/** Name für Animationssymbol "Person - Blau" */
	public static String iconNamePersonBlue="Person - Blau";
	/** Name für Animationssymbol "Notiz" */
	public static String iconNameNote="Notiz";
	/** Name für Animationssymbol "Paket" */
	public static String iconNamePackage="Paket";
	/** Name für Animationssymbol "Wolken" */
	public static String iconNameClouds="Wolken";
	/** Name für Animationssymbol "Gewitter" */
	public static String iconNameLightning="Gewitter";
	/** Name für Animationssymbol "Sonne" */
	public static String iconNameSun="Sonne";
	/** Name für Animationssymbol "Welt" */
	public static String iconNameWorld="Welt";
	/** Name für Animationssymbol "Kreis - Rot" */
	public static String iconNameBallRed="Kreis - Rot";
	/** Name für Animationssymbol "Kreis - Blau" */
	public static String iconNameBallBlue="Kreis - Blau";
	/** Name für Animationssymbol "Kreis - Gelb" */
	public static String iconNameBallYellow="Kreis - Gelb";
	/** Name für Animationssymbol "Kreis - Grün" */
	public static String iconNameBallGreen="Kreis - Grün";
	/** Name für Animationssymbol "Kreis - Schwarz" */
	public static String iconNameBallBlack="Kreis - Schwarz";
	/** Name für Animationssymbol "Kreis - Weiß" */
	public static String iconNameBallWhite="Kreis - Weiß";
	/** Name für Animationssymbol "Kreis - Orange" */
	public static String iconNameBallOrange="Kreis - Orange";
	/** Name für Animationssymbol "Kreis - Grau" */
	public static String iconNameBallGray="Kreis - Grau";
	/** Name für Animationssymbol "Zahnrad" */
	public static String iconNameCog="Zahnrad";
	/** Name für Animationssymbol "Auge" */
	public static String iconNameEye="Auge";
	/** Name für Animationssymbol "Haus" */
	public static String iconNameHouse="Haus";
	/** Name für Animationssymbol "Bausteine" */
	public static String iconNameBricks="Bausteine";
	/** Name für Animationssymbol "Bediener" */
	public static String iconNameOperator="Bediener";
	/** Name für Animationssymbol "Farbe - Rot" */
	public static String iconNameColorRed="Farbe - Rot";
	/** Name für Animationssymbol "Farbe - Blau" */
	public static String iconNameColorBlue="Farbe - Blau";
	/** Name für Animationssymbol "Farbe - Gelb" */
	public static String iconNameColorYellow="Farbe - Gelb";
	/** Name für Animationssymbol "Farbe - Grün" */
	public static String iconNameColorGreen="Farbe - Grün";
	/** Name für Animationssymbol "Farbe - Schwarz" */
	public static String iconNameColorBlack="Farbe - Schwarz";
	/** Name für Animationssymbol "Farbe - Weiß" */
	public static String iconNameColorWhite="Farbe - Weiß";
	/** Name für Animationssymbol "Farbe - Orange" */
	public static String iconNameColorOrange="Farbe - Orange";
	/** Name für Animationssymbol "Farbe - Grau" */
	public static String iconNameColorGray="Farbe - Grau";
	/** Name für Animationssymbol "Pfeil - nach unten" */
	public static String iconNameArrowDown="Pfeil - nach unten";
	/** Name für Animationssymbol "Pfeil - nach links" */
	public static String iconNameArrowLeft="Pfeil - nach links";
	/** Name für Animationssymbol "Pfeil - nach rechts" */
	public static String iconNameArrowRight="Pfeil - nach rechts";
	/** Name für Animationssymbol "Pfeil - nach oben" */
	public static String iconNameArrowUp="Pfeil - nach oben";
	/** Name für Animationssymbol "Fragezeichen" */
	public static String iconNameQuestionmark="Fragezeichen";

	/* p.yusukekamiyamane Icons */

	/** Name für Animationssymbol "Hund" */
	public static String iconNameAnimalDog="Hund";
	/** Name für Animationssymbol "Affe" */
	public static String iconNameAnimalMonkey="Affe";
	/** Name für Animationssymbol "Pinguin" */
	public static String iconNameAnimalPenguin="Pinguin";
	/** Name für Animationssymbol "Katze" */
	public static String iconNameAnimalCat="Katze";
	/** Name für Animationssymbol "Batterie" */
	public static String iconNameBattery="Batterie";
	/** Name für Animationssymbol "Glocke" */
	public static String iconNameBell="Glocke";
	/** Name für Animationssymbol "Aktentasche" */
	public static String iconNameBriefcase="Aktentasche";
	/** Name für Animationssymbol "Kerze" */
	public static String iconNameCandle="Kerze";
	/** Name für Animationssymbol "Taxi" */
	public static String iconNameTaxi="Taxi";
	/** Name für Animationssymbol "Glaskolben" */
	public static String iconNameFlask="Glaskolben";
	/** Name für Animationssymbol "Blume" */
	public static String iconNameFlower="Blume";
	/** Name für Animationssymbol "Hamburger" */
	public static String iconNameHamburger="Hamburger";
	/** Name für Animationssymbol "Hand" */
	public static String iconNameHand="Hand";
	/** Name für Animationssymbol "Einmachglas" */
	public static String iconNameJar="Einmachglas";
	/** Name für Animationssymbol "Laubblatt" */
	public static String iconNameLeaf="Laubblatt";
	/** Name für Animationssymbol "Rettungsring" */
	public static String iconNameLifebuoy="Rettungsring";
	/** Name für Animationssymbol "Glühbirne-aus" */
	public static String iconNameLightbulbOff="Glühbirne-aus";
	/** Name für Animationssymbol "Glühbirne-an" */
	public static String iconNameLightbuldOn="Glühbirne-an";
	/** Name für Animationssymbol "Lutscher" */
	public static String iconNameLollipop="Lutscher";
	/** Name für Animationssymbol "Farbpalette" */
	public static String iconNamePalette="Farbpalette";
	/** Name für Animationssymbol "Stecknadel" */
	public static String iconNamePin="Stecknadel";
	/** Name für Animationssymbol "Fußball" */
	public static String iconNameSoccer="Fußball";
	/** Name für Animationssymbol "Werkzeugkasten" */
	public static String iconNameToolbox="Werkzeugkasten";
	/** Name für Animationssymbol "Pylon" */
	public static String iconNameTrafficCone="Pylon";
	/** Name für Animationssymbol "Regenschirm" */
	public static String iconNameUmbrella="Regenschirm";
	/** Name für Animationssymbol "Wassertropfen" */
	public static String iconNameWaterDrop="Wassertropfen";

	/**
	 * Zuordnung von Animationssymbol-Namen zu Dateinamen für die Symbole
	 */
	public static Map<String,String> ICONS;

	/**
	 * Initialisiert {@link #ICONS}. Dies erfolgt beim Laden
	 * der Klasse automatisch und muss nur nach einem Wechsel
	 * der Sprache manuell angestoßen werden.
	 */
	public static void initIconsMap() {
		ICONS=new HashMap<>();

		/* Famfamfam Icons */

		ICONS.put(iconNameAttach,"attach");
		ICONS.put(iconNameBook,"book");
		ICONS.put(iconNameBug,"bug");
		ICONS.put(iconNameCD,"cd");
		ICONS.put(iconNameCake,"cake");
		ICONS.put(iconNameCar,"car");
		ICONS.put(iconNameCart,"cart");
		ICONS.put(iconNameClock,"clock");
		ICONS.put(iconNameComputer,"computer");
		ICONS.put(iconNameDatabase,"database");
		ICONS.put(iconNameDisk,"disk");
		ICONS.put(iconNameLetter,"email");
		ICONS.put(iconNameSmiley,"emoticon_smile");
		ICONS.put(iconNameSymbolFemale,"female");
		ICONS.put(iconNameFlagBlue,"flag_blue");
		ICONS.put(iconNameFlagGreen,"flag_green");
		ICONS.put(iconNameFlagOrange,"flag_orange");
		ICONS.put(iconNameFlagPink,"flag_pink");
		ICONS.put(iconNameFlagPurple,"flag_purple");
		ICONS.put(iconNameFlagRed,"flag_red");
		ICONS.put(iconNameFlagYellow,"flag_yellow");
		ICONS.put(iconNameHeart,"heart");
		ICONS.put(iconNameKey,"key");
		ICONS.put(iconNameLorry,"lorry");
		ICONS.put(iconNameLorryLeft,"lorry-left");
		ICONS.put(iconNameLorryEmpty,"lorry-empty");
		ICONS.put(iconNameLorryLeftEmpty,"lorry-left-empty");
		ICONS.put(iconNameSymbolMale,"male");
		ICONS.put(iconNameMoneyDollar,"money_dollar");
		ICONS.put(iconNameMoneyEuro,"money_euro");
		ICONS.put(iconNameMoneyPound,"money_pound");
		ICONS.put(iconNameMoneyYen,"money_yen");
		ICONS.put(iconNameMusic,"music");
		ICONS.put(iconNameDocument,"page_white");
		ICONS.put(iconNameStar,"star");
		ICONS.put(iconNamePersonGray,"user_gray");
		ICONS.put(iconNamePersonGreen,"user_green");
		ICONS.put(iconNamePersonOrange,"user_orange");
		ICONS.put(iconNamePersonRed,"user_red");
		ICONS.put(iconNamePersonSuit,"user_suit");
		ICONS.put(iconNamePersonBlue,"user");
		ICONS.put(iconNameNote,"note");
		ICONS.put(iconNamePackage,"icon_package");
		ICONS.put(iconNameClouds,"weather_cloudy");
		ICONS.put(iconNameLightning,"weather_lightning");
		ICONS.put(iconNameSun,"weather_sun");
		ICONS.put(iconNameWorld,"world");
		ICONS.put(iconNameBallRed,"Ball_red");
		ICONS.put(iconNameBallBlue,"Ball_blue");
		ICONS.put(iconNameBallYellow,"Ball_yellow");
		ICONS.put(iconNameBallGreen,"Ball_green");
		ICONS.put(iconNameBallBlack,"Ball_black");
		ICONS.put(iconNameBallWhite,"Ball_white");
		ICONS.put(iconNameBallOrange,"Ball_orange");
		ICONS.put(iconNameBallGray,"Ball_gray");
		ICONS.put(iconNameCog,"cog");
		ICONS.put(iconNameEye,"eye");
		ICONS.put(iconNameHouse,"house");
		ICONS.put(iconNameBricks,"bricks");
		ICONS.put(iconNameOperator,"status_online");
		ICONS.put(iconNameColorRed,"Intern-1");
		ICONS.put(iconNameColorBlue,"Intern-2");
		ICONS.put(iconNameColorYellow,"Intern-3");
		ICONS.put(iconNameColorGreen,"Intern-4");
		ICONS.put(iconNameColorBlack,"Intern-5");
		ICONS.put(iconNameColorWhite,"Intern-6");
		ICONS.put(iconNameColorOrange,"Intern-7");
		ICONS.put(iconNameColorGray,"Intern-8");
		ICONS.put(iconNameArrowDown,"arrow_down");
		ICONS.put(iconNameArrowLeft,"arrow_left");
		ICONS.put(iconNameArrowRight,"arrow_right");
		ICONS.put(iconNameArrowUp,"arrow_up");
		ICONS.put(iconNameQuestionmark,"questionmark");

		/* p.yusukekamiyamane Icons */

		ICONS.put(iconNameAnimalDog,"animal-dog");
		ICONS.put(iconNameAnimalMonkey,"animal-monkey");
		ICONS.put(iconNameAnimalPenguin,"animal-penguin");
		ICONS.put(iconNameAnimalCat,"animal");
		ICONS.put(iconNameBattery,"battery");
		ICONS.put(iconNameBell,"bell");
		ICONS.put(iconNameBriefcase,"briefcase");
		ICONS.put(iconNameCandle,"candle");
		ICONS.put(iconNameTaxi,"car-taxi");
		ICONS.put(iconNameFlask,"flask");
		ICONS.put(iconNameFlower,"flower");
		ICONS.put(iconNameHamburger,"hamburger");
		ICONS.put(iconNameHand,"hand");
		ICONS.put(iconNameJar,"jar");
		ICONS.put(iconNameLeaf,"leaf");
		ICONS.put(iconNameLifebuoy,"lifebuoy");
		ICONS.put(iconNameLightbulbOff,"light-bulb-off");
		ICONS.put(iconNameLightbuldOn,"light-bulb");
		ICONS.put(iconNameLollipop,"lollipop");
		ICONS.put(iconNamePalette,"palette");
		ICONS.put(iconNamePin,"pin");
		ICONS.put(iconNameSoccer,"sport-soccer");
		ICONS.put(iconNameToolbox,"toolbox");
		ICONS.put(iconNameTrafficCone,"traffic-cone");
		ICONS.put(iconNameUmbrella,"umbrella");
		ICONS.put(iconNameWaterDrop,"water");
	}

	static {
		initIconsMap();
	}

	/**
	 * Relativer Pfad für die Animations-Icon-Ressourcen
	 */
	private static final String IMAGE_PATH="animation";

	/**
	 * Speichert bereits einmal generierte Bilder
	 * @see #get(String, ModelAnimationImages, int)
	 */
	private Map<Long,BufferedImage> cache;

	/**
	 * Konstruktor der Klasse
	 */
	public AnimationImageSource() {
		cache=new HashMap<>();
	}

	/**
	 * Löscht alle Bilder aus dem Cache.<br>
	 * (Ist nötig, wenn die Liste der benutzerdefinierten Bilder während der Verwendung dieses Objekts verändert wurde.)
	 */
	public void clearCache() {
		cache.clear();
	}

	/**
	 * Erzeugt eine Grafik die ein Symbol für ein leeres Bild enthält
	 * @param size	Größe der Grafik
	 * @return	Grafik die ein Symbol für ein leeres Bild enthält
	 * @see #loadImageFromResource(String, ModelAnimationImages, int)
	 */
	private BufferedImage getEmptyImage(int size) {
		if (size<=0) size=32;
		if (size>=2048) size=2048;

		final BufferedImage image=new BufferedImage(size,size,BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics g=image.getGraphics();
		g.setColor(Color.RED);
		g.drawRect(0,0,size-1,size-1);
		g.drawLine(0,0,size-1,size-1);
		g.drawLine(size-1,0,0,size-1);
		return image;
	}

	/**
	 * Skaliert ein Bild auf eine vorgegebene Breite
	 * @param original	Ausgangsbild
	 * @param width	Zielbreite
	 * @return	Neues Bild; wenn das Ausgangsbild bereits die korrekte Breit hat, wird das Ausgangsbild zurückgeliefert.
	 */
	public static BufferedImage scaleImage(final BufferedImage original, final int width) {
		if (original.getWidth()==width && original.getHeight()==width) return original;
		final Image image=original.getScaledInstance(width,(int)Math.round(((double)original.getHeight())/original.getWidth()*width),Image.SCALE_SMOOTH);
		final BufferedImage bufferedImage=new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_4BYTE_ABGR);
		bufferedImage.getGraphics().drawImage(image,0,0,null);
		return bufferedImage;
	}

	/**
	 * Liefert ein mit einer bestimmten Farbe gefülltes Bild
	 * @param index	Auswahl der Farbe
	 * @param preferredSize	Größe des Bildes
	 * @return	Neues Bild
	 */
	private BufferedImage getInternalImage(final int index, int preferredSize) {
		if (preferredSize<=0) preferredSize=128;
		if (preferredSize>=2048) preferredSize=2048;
		final BufferedImage image=new BufferedImage(preferredSize,preferredSize,BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics g=image.getGraphics();
		switch (index) {
		case 1:
			g.setColor(Color.RED);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 2:
			g.setColor(Color.BLUE);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 3:
			g.setColor(Color.YELLOW);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 4:
			g.setColor(Color.GREEN);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 5:
			g.setColor(Color.BLACK);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 6:
			g.setColor(Color.WHITE);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 7:
			g.setColor(Color.ORANGE);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 8:
			g.setColor(Color.GRAY);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		}

		return image;
	}

	/**
	 * Bereits geladenen Bilder zwischenspeichern
	 * @see #loadImageFromResource(String, ModelAnimationImages, int)
	 */
	private static Map<String,Map<Integer,BufferedImage>> resourceCache=new HashMap<>();

	/**
	 * Versucht ein Bild in einer bestimmten Größe aus dem Cache zu laden.
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param preferredSize	Breite des Bildes
	 * @return	Liefert im Erfolgsfall das Bild, sonst <code>null</code>
	 * @see #storeToResourceCache(String, int, BufferedImage)
	 */
	private BufferedImage getFromResourceCache(final String name, final int preferredSize) {
		final Map<Integer,BufferedImage> map=resourceCache.get(name);
		if (map==null) return null;
		return map.get(preferredSize);
	}

	/**
	 * Speichert ein Bild im Cache.
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param preferredSize	Breite des Bildes
	 * @param image	Zu speicherndes Bild
	 * @see #getFromResourceCache(String, int)
	 */
	private void storeToResourceCache(final String name, final int preferredSize, final BufferedImage image) {
		Map<Integer,BufferedImage> map=resourceCache.get(name);
		if (map==null) resourceCache.put(name,map=new HashMap<>());

		map.put(preferredSize,image);
	}

	/**
	 * Lädt ein Bild in einer bestimmten (festen) Größe aus einer Ressource (ohne Caching)
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param size	Breite des Bildes
	 * @return	Bild in der angegebenen Größe (oder <code>null</code>, wenn das Bild in der Größe nicht existiert)
	 */
	private BufferedImage getFromResourceFixedSize(final String name, final int size) {
		final String folder=(size==16)?IMAGE_PATH:(IMAGE_PATH+size);


		URL url=getClass().getResource(folder+"/"+name+".png");
		if (url==null) url=getClass().getResource(folder+"/"+name.replace('_','-')+".png");
		if (url==null) return null;
		try {
			return ImageIO.read(url);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Lädt ein Bild in einer bestimmten Größe aus einer Ressource (ohne Caching)
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param preferredSize	Gewünschte Breite des Bildes
	 * @return	Bild in der der angegebenen Größe am nächsten kommenden Größe (oder <code>null</code>, wenn überhaupt kein Bild mit dem angegebenen Namen existiert)
	 */
	private BufferedImage getFromResource(final String name, final int preferredSize) {
		int pSize=(int)Math.round(preferredSize/8.0)*8;
		if (pSize<16) pSize=16;
		if (pSize>48) pSize=48;
		if (pSize==40) pSize=48;

		while (pSize>=16) {
			BufferedImage result=getFromResourceFixedSize(name,pSize);
			if (result!=null) return result;
			pSize-=8;
		}

		return null;
	}

	/**
	 * Lädt ein Bild in einer bestimmten Größe aus einer Ressource
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param preferredSize	Gewünschte Breite des Bildes
	 * @return	Bild in der angegebenen Größe (existiert das Bild nicht, wird ein Fehler-Bild geliefert, d.h. es wird immer ein Bild geliefert)
	 */
	private synchronized BufferedImage loadImageFromResource(final String name, final ModelAnimationImages modelImages, final int preferredSize) {
		/* Bilder aus Modell laden */
		if (modelImages!=null) {
			final BufferedImage image=modelImages.getLocal(name);
			if (image!=null) return image;
		}

		/* Per Zeichenbefehle generierte Bilder */
		final String INTERN="Intern-";
		if (name.startsWith(INTERN)) {
			try {
				final int index=Integer.parseInt(name.substring(INTERN.length()));
				return getInternalImage(index,preferredSize);
			} catch (NumberFormatException e) {} /* weiter unten */
		}

		/* Bild wenn möglich aus Cache holen */
		final BufferedImage cacheImage=getFromResourceCache(name,preferredSize);
		if (cacheImage!=null) return cacheImage;

		/* Bilder aus Ressourcen laden */
		BufferedImage resourceImage=getFromResource(name,preferredSize);
		if (resourceImage==null) resourceImage=getEmptyImage(preferredSize);

		/* Bild in Cache speichern */
		storeToResourceCache(name,preferredSize,resourceImage);

		return resourceImage;
	}

	/**
	 * Liefert ein Bild in einer bestimmten Größe aus
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param size	Gewünschte Breite des Bildes
	 * @return	Bild in der angegebenen Größe (existiert das Bild nicht, wird ein Fehler-Bild geliefert, d.h. es wird immer ein Bild geliefert)
	 */
	public BufferedImage get(final String name, final ModelAnimationImages modelImages, final int size) {
		final long key=name.hashCode()*1000L+size;
		/* verbraucht mehr Speicher: final String key=name+"-"+size; */
		BufferedImage image=cache.get(key);
		if (image!=null) return image;

		image=loadImageFromResource(name,modelImages,size);
		image=scaleImage(image,size);
		cache.put(key,image);
		return image;
	}

	/**
	 * Liefert das Bild in einer gegenüber der nativen Größe skalierten Fassung
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param zoom	Zoomfaktor
	 * @return	Bild in der angegebenen Größe (existiert das Bild nicht, wird ein Fehler-Bild geliefert, d.h. es wird immer ein Bild geliefert)
	 */
	public BufferedImage getZoomed(final String name, final ModelAnimationImages modelImages, final double zoom) {
		final int preferredSize=FastMath.max(4,(int)Math.round(16*zoom));
		final BufferedImage image=loadImageFromResource(name,modelImages,preferredSize);
		return scaleImage(image,preferredSize);
	}

	/**
	 * Liefert die Breite des Bildes im Normalfall
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @return	Breite und Höhe des Bildes
	 */
	public int[] getNativeSize(final String name, final ModelAnimationImages modelImages) {
		final BufferedImage image=loadImageFromResource(name,modelImages,-1);
		return new int[] {image.getWidth(),image.getHeight()};
	}

	/**
	 * Liefert ein Bild in der maximal zur Verfügung stehenden Größe aus
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @return	Bild in der maximal möglichen Größe (existiert das Bild nicht, wird ein Fehler-Bild geliefert, d.h. es wird immer ein Bild geliefert)
	 */
	public BufferedImage getMaxSize(final String name, final ModelAnimationImages modelImages) {
		return loadImageFromResource(name,modelImages,-1);
	}

	/**
	 * Liefert ein Bild in einer bestimmten Größe aus
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param baseSize	Gewünschte Breite des Bildes bezogen auf einen Zoomfaktor von 100%
	 * @param zoom	Zoomfaktor
	 * @return	Bild in der angegebenen Größe (existiert das Bild nicht, wird ein Fehler-Bild geliefert, d.h. es wird immer ein Bild geliefert)
	 */
	public BufferedImage get(final String name, final ModelAnimationImages modelImages, final int baseSize, final double zoom) {
		return get(name,modelImages,(int)FastMath.round(baseSize*zoom));
	}

	/**
	 * Liefert eine sortierte Liste der Namen der verfügbaren Bilder
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @return	Sortierte Liste der Namen der verfügbaren Bilder
	 */
	private List<String> getSortedIconsList(final ModelAnimationImages modelImages) {
		final List<String> names=new ArrayList<>(ICONS.keySet());
		if (modelImages!=null) names.addAll(Arrays.asList(modelImages.getLocalNames()));
		names.sort(null);
		return names;
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren Icons für eine Combobox
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @return	ComboBox-Modell mit allen verfügbaren Icons
	 */
	public DefaultComboBoxModel<JLabel> getIconsComboBox(final ModelAnimationImages modelImages) {
		final DefaultComboBoxModel<JLabel> iconsListModel=new DefaultComboBoxModel<>();

		for (String name: getSortedIconsList(modelImages)) {
			final JLabel label=new JLabel(name);
			String imageName=ICONS.get(name);
			if (imageName==null) imageName=name;
			final BufferedImage image=get(imageName,modelImages,16);
			label.setIcon(new ImageIcon(image));
			iconsListModel.addElement(label);
		}
		return iconsListModel;
	}

	/**
	 * Fügt alle verfügbaren Icons als Menüpunkte zu einem Menü hinzu
	 * @param menu	Menü, zu dem die Punkte hinzugefügt werden sollen
	 * @param clickCallback	Callback-Funktion, die bei einem Klick auf einen der Menüpunkte aufgerufen werden soll. Es wird der Dateiname (ohne Endung) des Bildes übergeben (der direkt für <code>get</code> verwendet werden kann) und nicht der Anzeigename.
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 */
	public void addIconsToMenu(final JMenu menu, final Consumer<String> clickCallback, final ModelAnimationImages modelImages) {
		for (String name: getSortedIconsList(modelImages)) {
			String imageName=ICONS.get(name);
			if (imageName==null) imageName=name;
			final JMenuItem item=new JMenuItem(name,new ImageIcon(get(imageName,modelImages,16)));
			final String fixName=imageName;
			item.addActionListener(e->clickCallback.accept(fixName));
			menu.add(item);
		}
	}

	/**
	 * Fügt alle verfügbaren Icons als Menüpunkte zu einem Menü hinzu
	 * @param menu	Menü, zu dem die Punkte hinzugefügt werden sollen
	 * @param clickCallback	Callback-Funktion, die bei einem Klick auf einen der Menüpunkte aufgerufen werden soll. Es wird der Dateiname (ohne Endung) des Bildes übergeben (der direkt für <code>get</code> verwendet werden kann) und nicht der Anzeigename.
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 */
	public void addIconsToMenu(final JPopupMenu menu, final Consumer<String> clickCallback, final ModelAnimationImages modelImages) {
		for (String name: getSortedIconsList(modelImages)) {
			final String imageName=ICONS.get(name);
			final JMenuItem item=new JMenuItem(name,new ImageIcon(get(imageName,modelImages,16)));
			item.addActionListener(e->clickCallback.accept(imageName));
			menu.add(item);
		}
	}

	/**
	 * ComboBox ListCellRenderer, der es ermöglicht, Icons inkl. Beschriftungen in der Liste anzuzeigen
	 * @author Alexander Herzog
	 */
	public static class IconComboBoxCellRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 6913560392242517601L;

		/**
		 * Konstruktor der Klasse
		 */
		public IconComboBoxCellRenderer() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof JLabel) {
				((IconComboBoxCellRenderer)renderer).setText(((JLabel)value).getText());
				((IconComboBoxCellRenderer)renderer).setIcon(((JLabel)value).getIcon());
			}
			return renderer;
		}
	}
}
