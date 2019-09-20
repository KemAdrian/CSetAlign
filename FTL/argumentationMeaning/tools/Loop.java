package tools;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

public class Loop<T> extends LinkedList<T>{
	
	private static final long serialVersionUID = 1L;
	
	public Loop() {
		super();
	}
	
	public Loop(Collection<T> collection){
		super(collection);
	}

	public ListIterator<T> listIterator(int index) {
		return new LoopIterator<>();
	}
	
	private class LoopIterator<U> implements ListIterator<T>{
		
		private int position = -1;
		
		public boolean hasNext() {
			return true;
		}

		@Override
		public T next() {
			position ++;
			if(position >= Loop.this.size())
				position = 0;
			return Loop.this.get(position);
		}

		@Override
		public void add(T arg0) {
			Loop.this.add(arg0);
		}

		@Override
		public boolean hasPrevious() {
			return true;
		}

		@Override
		public int nextIndex() {
			return  (position < Loop.this.size()-1)? position +1 :  0;
		}

		@Override
		public T previous() {
			position --;
			if(position < 0)
				position = Loop.this.size() - 1;
			return Loop.this.get(position);
		}

		@Override
		public int previousIndex() {
			return (position > 0)? position -1 :  Loop.this.size() - 1;
		}

		@Override
		public void remove() {
			Loop.this.remove(position);
			
		}

		@Override
		public void set(T arg0) {
			Loop.this.set(position, arg0);
		}

	}

}
