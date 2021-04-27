package io.djigger.store;

import io.djigger.ql.Filter;
import io.djigger.ql.Filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

public class StoreCollection<T> implements Serializable {

    private static final long serialVersionUID = 8070327606826703180L;

    List<T> collection = new ArrayList<T>();

    transient StoreCollectionListener<T> listener;

    private ReadWriteLock lock;

    public StoreCollection(ReadWriteLock lock) {
        super();
        this.lock = lock;
    }

    public StoreCollection(StoreCollectionListener<T> listener) {
        super();
        this.listener = listener;
    }

    public void setListener(StoreCollectionListener<T> listener) {
        this.listener = listener;
    }

    public boolean add(T e) {
        boolean add = false;
        lock.writeLock().lock();
        try {
            onAdd(e);
            add = collection.add(e);
        } finally {
            lock.writeLock().unlock();
        }
        return add;
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean add = false;
        lock.writeLock().lock();
        try {
            for (T t : c) {
                onAdd(t);
            }
            add = collection.addAll(c);
        } finally {
            lock.writeLock().unlock();
        }
        return add;
    }

    private void onAdd(T e) {
        if (listener != null) {
            listener.onAdd(e);
        }
    }

    private void onClear() {
        if (listener != null) {
            listener.onClear();
        }
    }

    public void drainTo(StoreCollection<T> target) {
        lock.writeLock().lock();
        try {
            target.addAll(collection);
            clear();
        } finally {
            lock.writeLock().unlock();
        }

    }

    public void clear() {
        lock.writeLock().lock();
        try {
            collection.clear();
            onClear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<T> query(Filter<T> filter) {
        List<T> apply = null;
        lock.readLock().lock();
        try {
            apply = Filters.apply(filter, collection);
        } finally {
            lock.readLock().unlock();
        }
        return apply;
    }

    public void remove(final Filter<T> filter) {
        lock.writeLock().lock();
        try {
            if (filter != null) {
                collection = Filters.apply(new Filter<T>() {

                    @Override
                    public boolean isValid(T input) {
                        return !filter.isValid(input);
                    }

                }, collection);
            }
            if (listener != null) {
                listener.onRemove(filter);
            }
        } finally {
            lock.writeLock().unlock();
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
