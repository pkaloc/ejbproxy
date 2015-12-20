package ejbproxy.runtime.basic.util.collection;

import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LoopingIterator<E> implements Iterator<E> {
	private final Collection<E> collection;

	private Iterator<E> iterator;

	public LoopingIterator(Collection<E> collection) {
		this.collection = Validate.notNull(collection);
		reset();
	}

	@Override
	public boolean hasNext() {
		return !collection.isEmpty();
	}

	@Override
	public E next() {
		if (collection.isEmpty()) {
			throw new NoSuchElementException();
		}
		if (!iterator.hasNext()) {
			reset();
		}
		return iterator.next();
	}

	@Override
	public void remove() {
		iterator.remove();
	}

	private void reset() {
		iterator = collection.iterator();
	}
}
