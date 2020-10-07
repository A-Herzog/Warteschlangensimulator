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
package systemtools;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Ermöglicht die Auswahl einer Farbe
 * @author Alexander Herzog
 */
public class SmallColorChooser extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2560660973355260332L;

	/** Farbe "Alice Blau" */
	public static String ColorNameF0F8FF="Alice Blau";
	/** Farbe "Antique Weiß" */
	public static String ColorNameFAEBD7="Antique Weiß";
	/** Farbe "Aquamarine" */
	public static String ColorName7FFFD4="Aquamarine";
	/** Farbe "Azure" */
	public static String ColorNameF0FFFF="Azure";
	/** Farbe "Beige" */
	public static String ColorNameF5F5DC="Beige";
	/** Farbe "Bisque" */
	public static String ColorNameFFE4C4="Bisque";
	/** Farbe "Schwarz" */
	public static String ColorName000000="Schwarz";
	/** Farbe "BlanchedAlmond" */
	public static String ColorNameFFEBCD="BlanchedAlmond";
	/** Farbe "Blau" */
	public static String ColorName0000FF="Blau";
	/** Farbe "Violettblau" */
	public static String ColorName8A2BE2="Violettblau";
	/** Farbe "Braun" */
	public static String ColorNameA52A2A="Braun";
	/** Farbe "BurlyWood" */
	public static String ColorNameDEB887="BurlyWood";
	/** Farbe "Cadet Blau" */
	public static String ColorName5F9EA0="Cadet Blau";
	/** Farbe "Chartreuse" */
	public static String ColorName7FFF00="Chartreuse";
	/** Farbe "Schokolade" */
	public static String ColorNameD2691E="Schokolade";
	/** Farbe "Coral" */
	public static String ColorNameFF7F50="Coral";
	/** Farbe "Kornblumen Blau" */
	public static String ColorName6495ED="Kornblumen Blau";
	/** Farbe "Cornsilk" */
	public static String ColorNameFFF8DC="Cornsilk";
	/** Farbe "Crimson" */
	public static String ColorNameDC143C="Crimson";
	/** Farbe "Zyan" */
	public static String ColorName00FFFF="Zyan";
	/** Farbe "Dunkelblau" */
	public static String ColorName00008B="Dunkelblau";
	/** Farbe "Dunkelzyan" */
	public static String ColorName008B8B="Dunkelzyan";
	/** Farbe "Dunkel GoldenRod" */
	public static String ColorNameB8860B="Dunkel GoldenRod";
	/** Farbe "Dunkegrau" */
	public static String ColorNameA9A9A9="Dunkegrau";
	/** Farbe "Dunkelgrün" */
	public static String ColorName006400="Dunkelgrün";
	/** Farbe "Dunkelkhaki" */
	public static String ColorNameBDB76B="Dunkelkhaki";
	/** Farbe "Dunkelmagenta" */
	public static String ColorName8B008B="Dunkelmagenta";
	/** Farbe "Dunkelolivegrün" */
	public static String ColorName556B2F="Dunkelolivegrün";
	/** Farbe "Dunkelorange" */
	public static String ColorNameFF8C00="Dunkelorange";
	/** Farbe "Dunkel Orchid" */
	public static String ColorName9932CC="Dunkel Orchid";
	/** Farbe "Dunkelrot" */
	public static String ColorName8B0000="Dunkelrot";
	/** Farbe "Dunkel Salmon" */
	public static String ColorNameE9967A="Dunkel Salmon";
	/** Farbe "Dunkelseegrün" */
	public static String ColorName8FBC8F="Dunkelseegrün";
	/** Farbe "Dunkel Slateblau" */
	public static String ColorName483D8B="Dunkel Slateblau";
	/** Farbe "Dunkel Slategrau" */
	public static String ColorName2F4F4F="Dunkel Slategrau";
	/** Farbe "Dunkel Turquoise" */
	public static String ColorName00CED1="Dunkel Turquoise";
	/** Farbe "Dunkelviolett" */
	public static String ColorName9400D3="Dunkelviolett";
	/** Farbe "Tiefes Pink" */
	public static String ColorNameFF1493="Tiefes Pink";
	/** Farbe "Tiefes Himmelsblau" */
	public static String ColorName00BFFF="Tiefes Himmelsblau";
	/** Farbe "Schwaches Grau" */
	public static String ColorName696969="Schwaches Grau";
	/** Farbe "Dodger Blau" */
	public static String ColorName1E90FF="Dodger Blau";
	/** Farbe "Fire Brick" */
	public static String ColorNameB22222="Fire Brick";
	/** Farbe "Blütenweiß" */
	public static String ColorNameFFFAF0="Blütenweiß";
	/** Farbe "Waldgrün" */
	public static String ColorName228B22="Waldgrün";
	/** Farbe "Gainsboro" */
	public static String ColorNameDCDCDC="Gainsboro";
	/** Farbe "Geisterweiß" */
	public static String ColorNameF8F8FF="Geisterweiß";
	/** Farbe "Gold" */
	public static String ColorNameFFD700="Gold";
	/** Farbe "Golden Rod" */
	public static String ColorNameDAA520="Golden Rod";
	/** Farbe "Grau" */
	public static String ColorName808080="Grau";
	/** Farbe "Grün" */
	public static String ColorName008000="Grün";
	/** Farbe "Grüngelb" */
	public static String ColorNameADFF2F="Grüngelb";
	/** Farbe "Honey Dew" */
	public static String ColorNameF0FFF0="Honey Dew";
	/** Farbe "Heißes Pink" */
	public static String ColorNameFF69B4="Heißes Pink";
	/** Farbe "Indian Rot" */
	public static String ColorNameCD5C5C="Indian Rot";
	/** Farbe "Indigo" */
	public static String ColorName4B0082="Indigo";
	/** Farbe "Ivory" */
	public static String ColorNameFFFFF0="Ivory";
	/** Farbe "Khaki" */
	public static String ColorNameF0E68C="Khaki";
	/** Farbe "Lavendell" */
	public static String ColorNameE6E6FA="Lavendell";
	/** Farbe "Lavendell Blush" */
	public static String ColorNameFFF0F5="Lavendell Blush";
	/** Farbe "Rasengrün" */
	public static String ColorName7CFC00="Rasengrün";
	/** Farbe "Lemon Chiffon" */
	public static String ColorNameFFFACD="Lemon Chiffon";
	/** Farbe "Hellblau" */
	public static String ColorNameADD8E6="Hellblau";
	/** Farbe "Helles Coral" */
	public static String ColorNameF08080="Helles Coral";
	/** Farbe "Helles Zyan" */
	public static String ColorNameE0FFFF="Helles Zyan";
	/** Farbe "Helles Golden Rod Gelb" */
	public static String ColorNameFAFAD2="Helles Golden Rod Gelb";
	/** Farbe "Hellgrau" */
	public static String ColorNameD3D3D3="Hellgrau";
	/** Farbe "Hellgrün" */
	public static String ColorName90EE90="Hellgrün";
	/** Farbe "Helles Pink" */
	public static String ColorNameFFB6C1="Helles Pink";
	/** Farbe "Helles Salmon" */
	public static String ColorNameFFA07A="Helles Salmon";
	/** Farbe "Helles Seegrün" */
	public static String ColorName20B2AA="Helles Seegrün";
	/** Farbe "Helles Slate Grau" */
	public static String ColorName778899="Helles Slate Grau";
	/** Farbe "Helles Stahlblau" */
	public static String ColorNameB0C4DE="Helles Stahlblau";
	/** Farbe "Hellgelb" */
	public static String ColorNameFFFFE0="Hellgelb";
	/** Farbe "Limette" */
	public static String ColorName00FF00="Limette";
	/** Farbe "Limettengrün" */
	public static String ColorName32CD32="Limettengrün";
	/** Farbe "Leinen" */
	public static String ColorNameFAF0E6="Leinen";
	/** Farbe "Magenta" */
	public static String ColorNameFF00FF="Magenta";
	/** Farbe "Maroon" */
	public static String ColorName800000="Maroon";
	/** Farbe "Mitteleres Aquamarine" */
	public static String ColorName66CDAA="Mitteleres Aquamarine";
	/** Farbe "Mittleres Blau" */
	public static String ColorName0000CD="Mittleres Blau";
	/** Farbe "Mittleres Orchid" */
	public static String ColorNameBA55D3="Mittleres Orchid";
	/** Farbe "Mittleres Purpur" */
	public static String ColorName9370DB="Mittleres Purpur";
	/** Farbe "Mittleres Seegrün" */
	public static String ColorName3CB371="Mittleres Seegrün";
	/** Farbe "Mittleres Slate Blau" */
	public static String ColorName7B68EE="Mittleres Slate Blau";
	/** Farbe "Mittleres Frühlingsgrün" */
	public static String ColorName00FA9A="Mittleres Frühlingsgrün";
	/** Farbe "Mittleres Turquoise" */
	public static String ColorName48D1CC="Mittleres Turquoise";
	/** Farbe "Mittleres Violettrot" */
	public static String ColorNameC71585="Mittleres Violettrot";
	/** Farbe "Mitternachtsblau" */
	public static String ColorName191970="Mitternachtsblau";
	/** Farbe "Minzcreme" */
	public static String ColorNameF5FFFA="Minzcreme";
	/** Farbe "Nebelige Rose" */
	public static String ColorNameFFE4E1="Nebelige Rose";
	/** Farbe "Moccasin" */
	public static String ColorNameFFE4B5="Moccasin";
	/** Farbe "Navajo Weiß" */
	public static String ColorNameFFDEAD="Navajo Weiß";
	/** Farbe "Navy" */
	public static String ColorName000080="Navy";
	/** Farbe "Olive" */
	public static String ColorNameFDF5E6="Olive";
	/** Farbe "OliveDrab" */
	public static String ColorName6B8E23="OliveDrab";
	/** Farbe "Orange" */
	public static String ColorNameFFA500="Orange";
	/** Farbe "Orangerot" */
	public static String ColorNameFF4500="Orangerot";
	/** Farbe "Orchidee" */
	public static String ColorNameDA70D6="Orchidee";
	/** Farbe "Schwachtes GoldenRod" */
	public static String ColorNameEEE8AA="Schwachtes GoldenRod";
	/** Farbe "Schwaches Grün" */
	public static String ColorName98FB98="Schwaches Grün";
	/** Farbe "Schwaches Turquoise" */
	public static String ColorNameAFEEEE="Schwaches Turquoise";
	/** Farbe "Schwaches Violettrot" */
	public static String ColorNameDB7093="Schwaches Violettrot";
	/** Farbe "Papaya Whip" */
	public static String ColorNameFFEFD5="Papaya Whip";
	/** Farbe "Peach Puff" */
	public static String ColorNameFFDAB9="Peach Puff";
	/** Farbe "Peru" */
	public static String ColorNameCD853F="Peru";
	/** Farbe "Pink" */
	public static String ColorNameFFC0CB="Pink";
	/** Farbe "Plum" */
	public static String ColorNameDDA0DD="Plum";
	/** Farbe "Powder Blau" */
	public static String ColorNameB0E0E6="Powder Blau";
	/** Farbe "Purpur" */
	public static String ColorName800080="Purpur";
	/** Farbe "Rebecca Purpur" */
	public static String ColorName663399="Rebecca Purpur";
	/** Farbe "Rot" */
	public static String ColorNameFF0000="Rot";
	/** Farbe "Rosy Braun" */
	public static String ColorNameBC8F8F="Rosy Braun";
	/** Farbe "Royal Blau" */
	public static String ColorName4169E1="Royal Blau";
	/** Farbe "Saddle Braun" */
	public static String ColorName8B4513="Saddle Braun";
	/** Farbe "Salmon" */
	public static String ColorNameFA8072="Salmon";
	/** Farbe "Sandbraun" */
	public static String ColorNameF4A460="Sandbraun";
	/** Farbe "Seegrün" */
	public static String ColorName2E8B57="Seegrün";
	/** Farbe "Seemuschel" */
	public static String ColorNameFFF5EE="Seemuschel";
	/** Farbe "Sienna" */
	public static String ColorNameA0522D="Sienna";
	/** Farbe "Silber" */
	public static String ColorNameC0C0C0="Silber";
	/** Farbe "Himmelsblau" */
	public static String ColorName87CEEB="Himmelsblau";
	/** Farbe "Slate Blau" */
	public static String ColorName6A5ACD="Slate Blau";
	/** Farbe "Slate Grau" */
	public static String ColorName708090="Slate Grau";
	/** Farbe "Schnee" */
	public static String ColorNameFFFAFA="Schnee";
	/** Farbe "Frühlingsgrün" */
	public static String ColorName00FF7F="Frühlingsgrün";
	/** Farbe "Stahlblau" */
	public static String ColorName4682B4="Stahlblau";
	/** Farbe "Tan" */
	public static String ColorNameD2B48C="Tan";
	/** Farbe "Teal" */
	public static String ColorName008080="Teal";
	/** Farbe "Thistle" */
	public static String ColorNameD8BFD8="Thistle";
	/** Farbe "Tomate" */
	public static String ColorNameFF6347="Tomate";
	/** Farbe "Turquoise" */
	public static String ColorName40E0D0="Turquoise";
	/** Farbe "Violett" */
	public static String ColorNameEE82EE="Violett";
	/** Farbe "Weizen" */
	public static String ColorNameF5DEB3="Weizen";
	/** Farbe "Weiß" */
	public static String ColorNameFFFFFF="Weiß";
	/** Farbe "Rauchweiß" */
	public static String ColorNameF5F5F5="Rauchweiß";
	/** Farbe "Gelb" */
	public static String ColorNameFFFF00="Gelb";
	/** Farbe "Gelbgrün" */
	public static String ColorName9ACD32="Gelbgrün";
	/** Farbe "Zeichenfläche" */
	public static String ColorNameFFFFFA="Zeichenfläche";

	/**
	 * Zuordnung mit den benannten Farben
	 * @see #initNamedColors()
	 */
	private Map<String,Color> namedColors;

	/**
	 * Liste mit den Farben, die dargestellt werden.
	 * @see #addColor(Color)
	 */
	private List<Color> colorsInList;

	/**
	 * Können Farben angeklickt werden?
	 * @see #isEnabled()
	 * @see #setEnabled(boolean)
	 */
	private boolean enabled;

	/**
	 * Konstruktor der Klasse <code>SmallColorChooser</code><br>
	 * Initial wird als Farbe Schwarz ausgewählt.
	 */
	public SmallColorChooser() {
		super();
		enabled=true;
		initNamedColors();
		initUI();
	}

	/**
	 * Konstruktor der Klasse <code>SmallColorChooser</code><br>
	 * @param color	Zu Beginn auszuwählende Farbe
	 */
	public SmallColorChooser(final Color color) {
		this();
		setColor(color);
	}

	/**
	 * Initialisiert die Zuordnung der Farbnamen
	 * @see #namedColors
	 */
	private void initNamedColors() {
		namedColors=new HashMap<>();

		namedColors.put(ColorNameF0F8FF, new Color(0xF0F8FF));
		namedColors.put(ColorNameFAEBD7, new Color(0xFAEBD7));
		namedColors.put(ColorName7FFFD4, new Color(0x7FFFD4));
		namedColors.put(ColorNameF0FFFF, new Color(0xF0FFFF));
		namedColors.put(ColorNameF5F5DC, new Color(0xF5F5DC));
		namedColors.put(ColorNameFFE4C4, new Color(0xFFE4C4));
		namedColors.put(ColorName000000, new Color(0x000000));
		namedColors.put(ColorNameFFEBCD, new Color(0xFFEBCD));
		namedColors.put(ColorName0000FF, new Color(0x0000FF));
		namedColors.put(ColorName8A2BE2, new Color(0x8A2BE2));
		namedColors.put(ColorNameA52A2A, new Color(0xA52A2A));
		namedColors.put(ColorNameDEB887, new Color(0xDEB887));
		namedColors.put(ColorName5F9EA0, new Color(0x5F9EA0));
		namedColors.put(ColorName7FFF00, new Color(0x7FFF00));
		namedColors.put(ColorNameD2691E, new Color(0xD2691E));
		namedColors.put(ColorNameFF7F50, new Color(0xFF7F50));
		namedColors.put(ColorName6495ED, new Color(0x6495ED));
		namedColors.put(ColorNameFFF8DC, new Color(0xFFF8DC));
		namedColors.put(ColorNameDC143C, new Color(0xDC143C));
		namedColors.put(ColorName00FFFF, new Color(0x00FFFF));
		namedColors.put(ColorName00008B, new Color(0x00008B));
		namedColors.put(ColorName008B8B, new Color(0x008B8B));
		namedColors.put(ColorNameB8860B, new Color(0xB8860B));
		namedColors.put(ColorNameA9A9A9, new Color(0xA9A9A9));
		namedColors.put(ColorName006400, new Color(0x006400));
		namedColors.put(ColorNameBDB76B, new Color(0xBDB76B));
		namedColors.put(ColorName8B008B, new Color(0x8B008B));
		namedColors.put(ColorName556B2F, new Color(0x556B2F));
		namedColors.put(ColorNameFF8C00, new Color(0xFF8C00));
		namedColors.put(ColorName9932CC, new Color(0x9932CC));
		namedColors.put(ColorName8B0000, new Color(0x8B0000));
		namedColors.put(ColorNameE9967A, new Color(0xE9967A));
		namedColors.put(ColorName8FBC8F, new Color(0x8FBC8F));
		namedColors.put(ColorName483D8B, new Color(0x483D8B));
		namedColors.put(ColorName2F4F4F, new Color(0x2F4F4F));
		namedColors.put(ColorName00CED1, new Color(0x00CED1));
		namedColors.put(ColorName9400D3, new Color(0x9400D3));
		namedColors.put(ColorNameFF1493, new Color(0xFF1493));
		namedColors.put(ColorName00BFFF, new Color(0x00BFFF));
		namedColors.put(ColorName696969, new Color(0x696969));
		namedColors.put(ColorName1E90FF, new Color(0x1E90FF));
		namedColors.put(ColorNameB22222, new Color(0xB22222));
		namedColors.put(ColorNameFFFAF0, new Color(0xFFFAF0));
		namedColors.put(ColorName228B22, new Color(0x228B22));
		namedColors.put(ColorNameDCDCDC, new Color(0xDCDCDC));
		namedColors.put(ColorNameF8F8FF, new Color(0xF8F8FF));
		namedColors.put(ColorNameFFD700, new Color(0xFFD700));
		namedColors.put(ColorNameDAA520, new Color(0xDAA520));
		namedColors.put(ColorName808080, new Color(0x808080));
		namedColors.put(ColorName008000, new Color(0x008000));
		namedColors.put(ColorNameADFF2F, new Color(0xADFF2F));
		namedColors.put(ColorNameF0FFF0, new Color(0xF0FFF0));
		namedColors.put(ColorNameFF69B4, new Color(0xFF69B4));
		namedColors.put(ColorNameCD5C5C, new Color(0xCD5C5C));
		namedColors.put(ColorName4B0082, new Color(0x4B0082));
		namedColors.put(ColorNameFFFFF0, new Color(0xFFFFF0));
		namedColors.put(ColorNameF0E68C, new Color(0xF0E68C));
		namedColors.put(ColorNameE6E6FA, new Color(0xE6E6FA));
		namedColors.put(ColorNameFFF0F5, new Color(0xFFF0F5));
		namedColors.put(ColorName7CFC00, new Color(0x7CFC00));
		namedColors.put(ColorNameFFFACD, new Color(0xFFFACD));
		namedColors.put(ColorNameADD8E6, new Color(0xADD8E6));
		namedColors.put(ColorNameF08080, new Color(0xF08080));
		namedColors.put(ColorNameE0FFFF, new Color(0xE0FFFF));
		namedColors.put(ColorNameFAFAD2, new Color(0xFAFAD2));
		namedColors.put(ColorNameD3D3D3, new Color(0xD3D3D3));
		namedColors.put(ColorName90EE90, new Color(0x90EE90));
		namedColors.put(ColorNameFFB6C1, new Color(0xFFB6C1));
		namedColors.put(ColorNameFFA07A, new Color(0xFFA07A));
		namedColors.put(ColorName20B2AA, new Color(0x20B2AA));
		namedColors.put(ColorName778899, new Color(0x778899));
		namedColors.put(ColorNameB0C4DE, new Color(0xB0C4DE));
		namedColors.put(ColorNameFFFFE0, new Color(0xFFFFE0));
		namedColors.put(ColorName00FF00, new Color(0x00FF00));
		namedColors.put(ColorName32CD32, new Color(0x32CD32));
		namedColors.put(ColorNameFAF0E6, new Color(0xFAF0E6));
		namedColors.put(ColorNameFF00FF, new Color(0xFF00FF));
		namedColors.put(ColorName800000, new Color(0x800000));
		namedColors.put(ColorName66CDAA, new Color(0x66CDAA));
		namedColors.put(ColorName0000CD, new Color(0x0000CD));
		namedColors.put(ColorNameBA55D3, new Color(0xBA55D3));
		namedColors.put(ColorName9370DB, new Color(0x9370DB));
		namedColors.put(ColorName3CB371, new Color(0x3CB371));
		namedColors.put(ColorName7B68EE, new Color(0x7B68EE));
		namedColors.put(ColorName00FA9A, new Color(0x00FA9A));
		namedColors.put(ColorName48D1CC, new Color(0x48D1CC));
		namedColors.put(ColorNameC71585, new Color(0xC71585));
		namedColors.put(ColorName191970, new Color(0x191970));
		namedColors.put(ColorNameF5FFFA, new Color(0xF5FFFA));
		namedColors.put(ColorNameFFE4E1, new Color(0xFFE4E1));
		namedColors.put(ColorNameFFE4B5, new Color(0xFFE4B5));
		namedColors.put(ColorNameFFDEAD, new Color(0xFFDEAD));
		namedColors.put(ColorName000080, new Color(0x000080));
		namedColors.put(ColorNameFDF5E6, new Color(0xFDF5E6));
		namedColors.put(ColorName6B8E23, new Color(0x6B8E23));
		namedColors.put(ColorNameFFA500, new Color(0xFFA500));
		namedColors.put(ColorNameFF4500, new Color(0xFF4500));
		namedColors.put(ColorNameDA70D6, new Color(0xDA70D6));
		namedColors.put(ColorNameEEE8AA, new Color(0xEEE8AA));
		namedColors.put(ColorName98FB98, new Color(0x98FB98));
		namedColors.put(ColorNameAFEEEE, new Color(0xAFEEEE));
		namedColors.put(ColorNameDB7093, new Color(0xDB7093));
		namedColors.put(ColorNameFFEFD5, new Color(0xFFEFD5));
		namedColors.put(ColorNameFFDAB9, new Color(0xFFDAB9));
		namedColors.put(ColorNameCD853F, new Color(0xCD853F));
		namedColors.put(ColorNameFFC0CB, new Color(0xFFC0CB));
		namedColors.put(ColorNameDDA0DD, new Color(0xDDA0DD));
		namedColors.put(ColorNameB0E0E6, new Color(0xB0E0E6));
		namedColors.put(ColorName800080, new Color(0x800080));
		namedColors.put(ColorName663399, new Color(0x663399));
		namedColors.put(ColorNameFF0000, new Color(0xFF0000));
		namedColors.put(ColorNameBC8F8F, new Color(0xBC8F8F));
		namedColors.put(ColorName4169E1, new Color(0x4169E1));
		namedColors.put(ColorName8B4513, new Color(0x8B4513));
		namedColors.put(ColorNameFA8072, new Color(0xFA8072));
		namedColors.put(ColorNameF4A460, new Color(0xF4A460));
		namedColors.put(ColorName2E8B57, new Color(0x2E8B57));
		namedColors.put(ColorNameFFF5EE, new Color(0xFFF5EE));
		namedColors.put(ColorNameA0522D, new Color(0xA0522D));
		namedColors.put(ColorNameC0C0C0, new Color(0xC0C0C0));
		namedColors.put(ColorName87CEEB, new Color(0x87CEEB));
		namedColors.put(ColorName6A5ACD, new Color(0x6A5ACD));
		namedColors.put(ColorName708090, new Color(0x708090));
		namedColors.put(ColorNameFFFAFA, new Color(0xFFFAFA));
		namedColors.put(ColorName00FF7F, new Color(0x00FF7F));
		namedColors.put(ColorName4682B4, new Color(0x4682B4));
		namedColors.put(ColorNameD2B48C, new Color(0xD2B48C));
		namedColors.put(ColorName008080, new Color(0x008080));
		namedColors.put(ColorNameD8BFD8, new Color(0xD8BFD8));
		namedColors.put(ColorNameFF6347, new Color(0xFF6347));
		namedColors.put(ColorName40E0D0, new Color(0x40E0D0));
		namedColors.put(ColorNameEE82EE, new Color(0xEE82EE));
		namedColors.put(ColorNameF5DEB3, new Color(0xF5DEB3));
		namedColors.put(ColorNameFFFFFF, new Color(0xFFFFFF));
		namedColors.put(ColorNameF5F5F5, new Color(0xF5F5F5));
		namedColors.put(ColorNameFFFF00, new Color(0xFFFF00));
		namedColors.put(ColorName9ACD32, new Color(0x9ACD32));
		namedColors.put(ColorNameFFFFFA, new Color(0xFFFFFA));
	}

	/**
	 * Fügt eine Farbe zu dem Auswahlfeld hinzu.<br>
	 * Es wird dabei geprüft, ob die Farbe evtl. schon
	 * in dem Auswahlbereich enthalten ist. Wenn ja,
	 * wird sie nicht ein zweites Mal hinzugefügt.
	 * @param color	Hinzuzufügende Farbe
	 * @see #initUI()
	 */
	private void addColor(final Color color) {
		if (colorsInList==null) colorsInList=new ArrayList<>();
		if (colorsInList.indexOf(color)>=0) return;

		final GridBagConstraints gbc=new GridBagConstraints();
		gbc.gridx=getComponentCount()/17;
		gbc.gridy=getComponentCount()%17;
		add(new ColorBox(color),gbc);
		colorsInList.add(color);
	}

	/**
	 * Fügt die Farben zu der Auswahl hinzu.
	 */
	private void initUI() {
		setLayout(new GridBagLayout());

		/* Grau */
		addColor(new Color(0,0,0));
		for (int i=30;i<=255;i+=15) addColor(new Color(i,i,i));

		/* Gelb */
		for (int i=0;i<=255;i+=15) addColor(new Color(i,i,0));

		/* Rot */
		for (int i=0;i<=255;i+=15) addColor(new Color(i,0,0));

		/* Magenta */
		for (int i=0;i<=255;i+=15) addColor(new Color(i,0,i));

		/* Grün */
		for (int i=0;i<=255;i+=15) addColor(new Color(0,i,0));

		/* Zyan */
		for (int i=0;i<=255;i+=15) addColor(new Color(0,i,i));

		/* Blau */
		for (int i=0;i<=255;i+=15) addColor(new Color(0,0,i));

		/* Restliche benannte Farben */
		for (Map.Entry<String,Color> entry: namedColors.entrySet()) addColor(entry.getValue());
	}

	/**
	 * Liefert den Namen einer Farbe als Text.
	 * @param color	Farbe für die der Name ermittelt werden soll
	 * @return	Namen einer Farbe als Text (zur Verwendung in Tooltips)
	 * @see ColorBox
	 */
	private String getColorName(final Color color) {
		String name=null;
		for (Map.Entry<String,Color> entry: namedColors.entrySet()) if (entry.getValue().equals(color)) {name=entry.getKey(); break;}

		StringBuilder sb=new StringBuilder();
		if (name!=null) {sb.append(name); sb.append(" (");}
		sb.append(color.getRed());
		sb.append(",");
		sb.append(color.getGreen());
		sb.append(",");
		sb.append(color.getBlue());
		if (name!=null) sb.append(")");

		return sb.toString();
	}

	/**
	 * Stellt die ausgewählte Farbe ein
	 * @param color	Auszuwählende Farbe; wird <code>null</code> oder eine in der Anzeige nicht vorhandene Farbe gewählt, so wird Schwarz ausgewählt
	 */
	public void setColor(final Color color) {
		int index=colorsInList.indexOf(color);
		if (index<0) index=0;
		for (int i=0;i<getComponentCount();i++) ((ColorBox)getComponent(i)).setSelected(i==index);

	}

	/**
	 * Liefert die momentan eingestellte Farbe
	 * @return	Aktuell gewählte Farbe
	 */
	public Color getColor() {
		for (int i=0;i<getComponentCount();i++) if (((ColorBox)getComponent(i)).isSelected()) return ((ColorBox)getComponent(i)).getColor();
		return Color.BLACK;
	}

	/**
	 * Gibt an, ob Farben per Klick ausgewählt werden können
	 * @return	Farben per Klick anwählbar
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Stellt ein, ob Farben per Klick gewählt werden dürfen
	 * @param enabled	Farben per Klick anwählbar
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		this.enabled=enabled;
	}

	/**
	 * Listener, die benachrichtigt werden sollen, wenn auf eine Farbe geklickt wird.
	 * @see #colorBoxClick(Color)
	 */
	private List<ActionListener> clickListeners=new ArrayList<>();

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn auf eine Farbe geklickt wird
	 * @param clickListener	Zu benachrichtigender Listener
	 */
	public void addClickListener(final ActionListener clickListener) {
		if (clickListeners.indexOf(clickListener)<0) clickListeners.add(clickListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle eines Klicks auf eine Farbe zu benachrichtigenden Listener
	 * @param clickListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeClickListener(final ActionListener clickListener) {
		return clickListeners.remove(clickListener);
	}

	/**
	 * Wird aufgerufen, wenn auf eine Farbe geklickt wurde.
	 * @param color	Angeklickte Farbe
	 */
	private void colorBoxClick(final Color color) {
		boolean ok=false;
		for (int i=0;i<getComponentCount();i++) {
			final boolean b=((ColorBox)getComponent(i)).getColor().equals(color);
			if (b) ok=true;
			((ColorBox)getComponent(i)).setSelected(b);
		}
		if (!ok) ((ColorBox)getComponent(0)).setSelected(true);

		repaint();

		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"click");
		for (ActionListener listener: clickListeners) listener.actionPerformed(event);
	}

	/**
	 * Darstellung einer einzelnen Farbe
	 */
	private class ColorBox extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 2485552914337779647L;

		/** Anzuzeigende Farbe */
		private final Color color;

		/** Ausgewählt darstellen? */
		private boolean selected;

		/**
		 * Konstruktor der Klasse
		 * @param color	Anzuzeigende Farbe
		 */
		public ColorBox(final Color color) {
			super();
			this.color=color;
			final Dimension size=new Dimension(16,16);
			setPreferredSize(size);
			setSize(size);
			setMaximumSize(size);
			setMinimumSize(size);
			setToolTipText(getColorName(color));
			addMouseListener(new MouseAdapter() {
				@Override public void mousePressed(MouseEvent e) {if (enabled) colorBoxClick(color);}
			});
		}

		/**
		 * Liefert die Farbe der Box
		 * @return	Farbe der Box
		 */
		public Color getColor() {
			return color;
		}

		/**
		 * Ist die Box ausgewählt?
		 * @return	Liefert <code>true</code>, wenn die Box ausgewählt ist.
		 * @see #setSelected(boolean)
		 */
		public boolean isSelected() {
			return selected;
		}

		/**
		 * Stellt ein, ob die Box ausgewählt dargestellt werden soll.
		 * @param selected	Box ausgewählt darstellen?
		 * @see #isSelected()
		 */
		public void setSelected(final boolean selected) {
			this.selected=selected;
			repaint();
		}

		@Override
		public void paint(Graphics g) {
			final Dimension size=getSize();
			g.setColor(color);
			g.fillRect(0,0,size.width-1,size.height-1);
			if (selected) {
				g.setColor(new Color(255-color.getRed(),255-color.getGreen(),255-color.getBlue()));
				g.drawRect(0,0,size.width-1,size.height-1);
				g.drawLine(0,0,size.width-1,size.height-1);
				g.drawLine(size.width-1,0,0,size.height-1);
			}
		}
	}
}
