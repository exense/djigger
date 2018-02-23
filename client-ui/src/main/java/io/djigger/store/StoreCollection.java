package io.djigger.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.djigger.ql.Filter;
import io.djigger.ql.Filters;

public class StoreCollection<T> implements Serializable {

	private static final long serialVersionUID = 8070327606826703180L;

	List<T> collection = new ArrayList<T>();

	transient StoreCollectionListener<T> listener;
	
	public StoreCollection() {
		super();		
	}

	public StoreCollection(StoreCollectionListener<T> listener) {
		super();
		this.listener = listener;
	}

	public void setListener(StoreCollectionListener<T> listener) {
		this.listener = listener;
	}

	public synchronized boolean add(T e) {
		onAdd(e);
		return collection.add(e);			
	}

	public synchronized boolean addAll(Collection<? extends T> c) {
		for (T t : c) {
			onAdd(t);
		}
		return collection.addAll(c);
	}
	
	private void onAdd(T e) {
		if(listener!=null) {
			listener.onAdd(e);
		}
	}
	
	private void onClear() {
		if(listener!=null) {
			listener.onClear();
		}
	}
	
	public synchronized void drainTo(StoreCollection<T> target) {
		target.addAll(collection);
		clear();
	}

	public synchronized void clear() {
		collection.clear();			
		onClear();
	}
	
	public synchronized List<T> query(Filter<T> filter) {
		return Filters.apply(filter, collection);
	}
	
	public synchronized void remove(final Filter<T> filter) {
		if(filter!=null) {
			collection = Filters.apply(new Filter<T>() {
	
				@Override
				public boolean isValid(T input) {
					return !filter.isValid(input);
				}
				
			}, collection);
		}
		if(listener!=null) {
			listener.onRemove(filter);			
		}
	}
	
	public List<T> queryAll() {
		return query(null);
	}
	
	public interface StoreCollectionListener<T> {
		
		public void onAdd(T entry);
		
		public void onClear();
		
		public void onRemove(Filter<T> filter);
	}

	
//	private Object readResolve() throws ObjectStreamException {
//		return this;
//	}
}
