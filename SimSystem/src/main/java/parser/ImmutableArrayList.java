/**
 * Copyright 2022 Alexander Herzog
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
package parser;

import java.util.AbstractList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Extrem leichtgewichtiger Rad-Only-Wrapper um ein Array.
 * @author Alexander Herzog
 * @param <E> Beliebiger Typ
 */
public class ImmutableArrayList<E> extends AbstractList<E> {
	/**
	 * Internes Datenobjekt.<br>
	 * Zugriffe erfolgen immer direkt, das Objekt wird nie kopiert.
	 */
	private final E[] data;

	/**
	 * Konstruktor der Klasse
	 * @param array	Array, welches als Liste (strikt Read-Only) zur Verfügung gestellt werden soll
	 */
	public ImmutableArrayList(final E[] array) {
		data=array;
	}

	@Override
	public int size() {
		return data.length;
	}

	@Override
	public Object[] toArray() {
		return data;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if (!data.getClass().equals(a.getClass())) throw new IllegalAccessError();
		return a;
	}

	@Override
	public E get(int index) {
		return data[index];
	}

	@Override
	public E set(final int index, final E element) {
		throw new IllegalAccessError();
	}

	@Override
	public int indexOf(final Object o) {
		final E[] a=this.data;
		if (o==null) {
			for (int i=0;i<a.length;i++) if (a[i]==null) return i;
		} else {
			for (int i=0;i<a.length;i++) if (o.equals(a[i])) return i;
		}
		return -1;
	}

	@Override
	public boolean contains(final Object o) {
		return indexOf(o)>=0;
	}

	@Override
	public Spliterator<E> spliterator() {
		return Spliterators.spliterator(data, Spliterator.ORDERED);
	}

	@Override
	public void forEach(Consumer<? super E> action) {
		Objects.requireNonNull(action);
		for (E e: data) action.accept(e);
	}

	@Override
	public void replaceAll(UnaryOperator<E> operator) {
		throw new IllegalAccessError();
	}

	@Override
	public void sort(Comparator<? super E> c) {
		throw new IllegalAccessError();
	}

	@Override
	public Iterator<E> iterator() {
		return new ImmutableArrayListIterator<>(data);
	}

	/**
	 * Iterator für {@link ImmutableArrayList}
	 * @param <E>	Beliebiger Typ
	 */
	private static class ImmutableArrayListIterator<E> implements Iterator<E> {
		/**
		 * Aktuelle Position
		 */
		private int cursor;

		/**
		 * Referenz auf das Array-Objekt in {@link ImmutableArrayList}
		 */
		private final E[] data;

		/**
		 * Konstruktor der Klasse
		 * @param array	Referenz auf das Array-Objekt in {@link ImmutableArrayList}
		 */
		private ImmutableArrayListIterator(final E[] array) {
			this.data=array;
		}

		@Override
		public boolean hasNext() {
			return cursor<data.length;
		}

		@Override
		public E next() {
			if (cursor>=data.length) throw new NoSuchElementException();
			return data[cursor++];
		}
	}
}
