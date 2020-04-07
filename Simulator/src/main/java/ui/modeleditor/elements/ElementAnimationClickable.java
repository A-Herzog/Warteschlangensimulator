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

/**
 * Elemente, die w�hrend der Animation dar�ber benachrichtigt werden wollen,
 * dass sie angeklickt wurden, m�ssen dieses Interface implementieren.
 * @author Alexander Herzog
 */
public interface ElementAnimationClickable {
	/**
	 * Wird vom Animationssystem aufgerufen, wenn das Element angeklickt wurde.
	 * @param x	x-Position relativ zur linken oberen Ecke des Elements
	 * @param y	y-Position relativ zur linken oberen Ecke des Elements
	 */
	void clicked(final int x, final int y);
}
