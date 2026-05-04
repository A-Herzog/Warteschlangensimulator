/**
 * Copyright 2026 Alexander Herzog
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
package mathtools.distribution.tools;

/**
 * Interface zu einem Drand48-artigen Generator.<br>
 * Wird von {@link Drand48BitsStreamGenerator} verwendet.
 */
public interface Drand48Interface {

	/**
	 * Seed the generator (like C's srand48).
	 * @param seedval seed value
	 */
	void srand48(long seedval);

	/**
	 * Advance the internal state and return a double in [0.0, 1.0).
	 * Equivalent to C's drand48().
	 * @return uniformly distributed double in [0,1)
	 */
	double drand48();

	/**
	 * Return a 31-bit non-negative integer like C's lrand48 (optional helper).
	 * @return int in [0, 2^31)
	 */
	int lrand48();
}
