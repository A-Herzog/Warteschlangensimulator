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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Interface zur Kapselung von verschiedenen m�glichen Batch-Raten-Editoren f�r
 * {@link ModelElementSourceBatchDialog}
 * @author Alexander Herzog
 * @see ModelElementSourceBatchDialog
 */
public interface ModelElementSourceBatchEditor {
	/**
	 * Bei einer �nderung der Raten zu benachrichtigende Listener
	 * @see #fireChangeListeners()
	 */
	Set<Consumer<ModelElementSourceBatchEditor>> changeListeners=new HashSet<>();

	/**
	 * Stellt neue Batch-Raten ein.
	 * @param distribution	Batch-Raten
	 */
	void setDistribution(double[] distribution);

	/**
	 * Liefert die aktuellen Batch-Raten.
	 * @return	Batch-Raten
	 */
	double[] getDistribution();

	/**
	 * Pr�ft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> �bergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	boolean checkData(final boolean showErrorMessage);

	/**
	 * Pr�ft die Eingaben und gibt im Fehlerfall auch eine Fehlermeldung aus.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	default boolean checkData() {
		return checkData(true);
	}

	/**
	 * F�gt einen zu benachrichtigen Change-Listener hinzu.
	 * @param listener	Neuer Chance-Listener
	 * @return	Liefert <code>true</code>, wenn der Listener hinzugef�gt werden konnte.
	 */
	default boolean addChangeListener(final Consumer<ModelElementSourceBatchEditor> listener) {
		return changeListeners.add(listener);
	}

	/**
	 * Entfernt einen zu benachrichtigen Change-Listener.
	 * @param listener	Nicht mehr zu benachrichtigender Change-Listener
	 * @return	Liefert <code>true</code>, wenn der Listener entfernt werden konnte.
	 */
	default boolean removeChangeListener(final Consumer<ModelElementSourceBatchEditor> listener) {
		return changeListeners.remove(listener);
	}

	/**
	 * Benachrichtigt alle Listener, dass sich die Batch-Raten ver�ndert haben.
	 */
	default void fireChangeListeners() {
		changeListeners.forEach(listener->listener.accept(this));
	}

	/**
	 * Liefert den Namen des Editors (z.B. zur Anzeige im Tabtitel).
	 * @return	Name des Editors
	 */
	String getEditorName();
}
