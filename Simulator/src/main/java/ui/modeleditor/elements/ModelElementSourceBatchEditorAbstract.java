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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.io.Serializable;

import javax.swing.JPanel;

/**
 * Abstrakte Basisklasse für verschiedene Batch-Raten-Editoren.
 * @author Alexander Herzog
 * @see ModelElementSourceBatchDialog
 */
public abstract class ModelElementSourceBatchEditorAbstract extends JPanel implements ModelElementSourceBatchEditor {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8384840679613037757L;

	/** Nur-Lese-Status */
	protected final boolean readOnly;

	/**
	 * Konstruktor der Klasse
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementSourceBatchEditorAbstract(final boolean readOnly) {
		this.readOnly=readOnly;
		setLayout(new BorderLayout());
	}
}
