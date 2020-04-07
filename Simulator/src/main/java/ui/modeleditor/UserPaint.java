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

import java.awt.Graphics;

/**
 * Über die Methode {@link ModelSurfacePanel#setAdditionalUserPaint(UserPaint)} kann
 * ein Objekt, welches dieses Interface implementiert, eingetragen werden, welches
 * innerhalb der Paint-Methode von {@link ModelSurfacePanel} aufgerufen wird.
 * @author Alexander Herzog
 * @see ModelSurfacePanel#setAdditionalUserPaint(UserPaint)
 */
public interface UserPaint {
	/**
	 * Callback, welches innerhalb von {@link ModelSurfacePanel#paint(Graphics)} aufgerufen wird
	 * @param g	{@link Graphics}-Objhekt in das die Ausgabe erfolgen soll
	 * @param zoom	Gewählter Zoomlevel (1.0==100%)
	 */
	void paint(final Graphics g, final double zoom);
}
