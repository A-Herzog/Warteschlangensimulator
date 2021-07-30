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
package ui.modeleditor.elements;

import java.util.Arrays;

/**
 * Animationselemente, die ein oder mehrere Skript (über {@link AnimationExpression}-Objekte)
 * enthalten können, implementieren dieses Interface. Darüber können die Skripte abgerufen werden,
 * ohne dass der Aufrufer wissen muss, um was für eine Element es sich genau handelt.
 * @author Alexander Herzog
 * @see AnimationExpression
 */
public interface ElementWithAnimationScripts {
	/**
	 * Liefert alle {@link AnimationExpression}-Objekte.<br>
	 * Das Array darf leer, aber nicht <code>null</code> sein.
	 * Die Einträge müssen nicht notwendig Skripte sein, auch
	 * Rechenausdrücke können hier zurückgeliefert werden.
	 * @return	{@link AnimationExpression}-Objekte
	 * @see AnimationExpression
	 */

	AnimationExpression[] getAnimationExpressions();

	/**
	 * Liefert die Einträge aus {@link #getAnimationExpressions()},
	 * bei denen es sich um Skripte (und nicht um Rechenausdrücke) handelt.<br>
	 * Das Array kann leer sein, ist aber nie <code>null</code>.
	 * @return {@link AnimationExpression}-Objekte
	 */
	default AnimationExpression[] getAnimationScripts() {
		return Arrays.asList(getAnimationExpressions()).stream().filter(expression->expression.getMode()!=AnimationExpression.ExpressionMode.Expression).toArray(AnimationExpression[]::new);
	}
}
