package sandboxes;

import java.util.Iterator;

import tools.Loop;
public class SandboxLoop {
	
	public static void main(String[] arg) throws Exception{
		
		Loop<Integer> loop = new Loop<Integer>();
		loop.add(1);
		loop.add(2);
		loop.add(3);
		loop.add(5);
		loop.add(8);
		loop.add(13);
		Iterator<Integer> iterator = loop.listIterator();
		for(int i=0; i<100; i++) {
			System.out.println(iterator.next());
		}
	}

}
