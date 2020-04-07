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
package systemtools.help;

import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * Kapselt einen Webbrowser auf Basis eines <code>JTextPane</code>-Elements
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @author Alexander Herzog
 * @see HTMLBrowserPanel
 * @version 1.1
 */
public class HTMLBrowserTextPane extends JTextPane implements HTMLBrowserPanel {
	private static final long serialVersionUID = 6093964682884036080L;

	private Runnable linkClickListener;
	private Runnable pageLoadListener;

	private List<ElementPos> pageContentList;

	private URL lastClickedURL;
	private String lastClickedURLDescription;

	/**
	 * Konstruktor der Klasse <code>HTMLBrowserTextPane</code>
	 */
	public HTMLBrowserTextPane() {
		super();
		lastClickedURL=null;
		lastClickedURLDescription="";
		setEditable(false);
		addHyperlinkListener(new LinkListener());
		addPropertyChangeListener("page",new PageLoadListener());
	}

	@Override
	public void init(Runnable linkClickListener, Runnable pageLoadListener) {
		this.linkClickListener=linkClickListener;
		this.pageLoadListener=pageLoadListener;
	}

	@Override
	public JComponent asScrollableJComponent() {
		return new JScrollPane(this);
	}

	@Override
	public JComponent asInnerJComponent() {
		return this;
	}

	@Override
	public final boolean showPage(final URL url) {
		try {setPage(url);} catch (IOException e) {return false;}

		/*
		Funktioniert leider nicht, wenn in jar-Datei. Daher muss überall das BOM weg.
		try {
			String text=new String(Files.readAllBytes(Paths.get(url.toURI())),Charsets.UTF_8);
			if (text.length()>1 && text.charAt(0)==65279) text=text.substring(1);
			int index=text.toLowerCase().indexOf("<meta");
			while (index>=0) {
				String textNeu=(index==0)?"":text.substring(0,index);
				text=text.substring(index);
				final int index2=text.indexOf(">");
				if (index2<0) break;
				text=textNeu+text.substring(index2+1);
				index=text.toLowerCase().indexOf("<meta");
			}
			setEditorKit(new HTMLEditorKit());
			read(new ByteArrayInputStream(text.getBytes()),url);
		} catch (IOException | URISyntaxException e) {
			return false;
		}
		 */

		return true;
	}

	@Override
	public final URL getLastClickedURL() {
		return lastClickedURL;
	}

	@Override
	public final String getLastClickedURLDescription() {
		return lastClickedURLDescription;
	}

	private final class ElementPos {
		private final Element element;
		private final int level;
		private final int position;

		public ElementPos(Element element) {
			this.element=element;
			int l=0;
			for (int i=2;i<5;i++) if (element.getName().equalsIgnoreCase("h"+i)) {l=i; break;}
			level=l;
			position=element.getStartOffset();
		}

		@Override
		public String toString() {
			StringBuilder s=new StringBuilder();
			for (int i=0;i<element.getElementCount();i++) {
				if (element.getElement(i) instanceof HTMLDocument.RunElement) {
					HTMLDocument.RunElement runElement=(HTMLDocument.RunElement)element.getElement(i);
					try {
						String t=runElement.getDocument().getText(runElement.getStartOffset(),runElement.getEndOffset()-runElement.getStartOffset());
						s.append(t.replace("\n",""));
					} catch (BadLocationException e) {}
				}
			}

			for (int i=3;i<=level;i++) s.insert(0,"   ");

			return s.toString();
		}

		public int getLevel() {
			return level;
		}

		public int getPosition() {
			return position;
		}
	}

	private final List<ElementPos> scanElement(Element parent) {
		final List<ElementPos> list=new ArrayList<>();

		for (int i=0;i<parent.getElementCount();i++) {
			Element element=parent.getElement(i);

			/* Überschriften speichern */
			boolean ok=false;
			for (int j=2;j<5;j++) if (element.getName().equalsIgnoreCase("h"+j)) {ok=true; break;}
			if (ok) list.add(new ElementPos(element));

			/* Unterelemente untersuchen */
			list.addAll(scanElement(element));
		}

		return list;
	}

	@Override
	public final List<String> getPageContent() {
		final List<String> list=new ArrayList<>();
		if (pageContentList!=null) for (int i=0;i<pageContentList.size();i++) list.add(pageContentList.get(i).toString());
		return list;
	}

	@Override
	public final List<Integer> getPageContentLevel() {
		final List<Integer> list=new ArrayList<>();
		if (pageContentList!=null) for (int i=0;i<pageContentList.size();i++) list.add(pageContentList.get(i).getLevel());
		return list;
	}

	@Override
	public final boolean scrollToPageContent(int index) {
		if (pageContentList==null || index<0 || index>=pageContentList.size()) return false;
		setCaretPosition(pageContentList.get(index).getPosition());
		return true;
	}

	private final class LinkListener implements HyperlinkListener {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==HyperlinkEvent.EventType.ENTERED) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}

			if (e.getEventType()==HyperlinkEvent.EventType.EXITED) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}

			if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
				if (e instanceof HTMLFrameHyperlinkEvent) {
					HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e;
					HTMLDocument doc=(HTMLDocument)getDocument();
					doc.processHTMLFrameHyperlinkEvent(evt);
				} else {
					lastClickedURL=e.getURL();
					lastClickedURLDescription=e.getDescription();
					if (linkClickListener!=null) linkClickListener.run();
				}
			}
		}
	}

	private final class PageLoadListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			pageContentList=scanElement(((HTMLDocument)getStyledDocument()).getDefaultRootElement());
			if (pageLoadListener!=null) pageLoadListener.run();
		}
	}

	@Override
	public boolean needsLoadLock() {
		return false;
	}

	@Override
	public boolean setUserDefinedStyleSheet(String styleSheet) {
		return false;
	}

	@Override
	public boolean needsBorder() {
		return false;
	}
}