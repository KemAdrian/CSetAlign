package sandboxes;

import arguments.ArgTree;
import arguments.Belief;
import arguments.CounterArgument;
import arguments.CounterExample;

public class SandboxTree {
	
public static void main(String[] arg) throws Exception{
	
		Belief b1 = new Belief.Builder().from("adam").build();
		CounterArgument a2 = new CounterArgument.Builder().from("boby").against(b1.id).build();
		CounterArgument a3 = new CounterArgument.Builder().from("boby").against(b1.id).build();
		CounterArgument a4 = new CounterArgument.Builder().from("adam").against(a2.id).build();
		CounterArgument a5 = new CounterArgument.Builder().from("adam").against(a2.id).build();
		CounterArgument a6 = new CounterArgument.Builder().from("adam").against(a3.id).build();
		CounterArgument a7 = new CounterArgument.Builder().from("boby").against(a4.id).build();
		CounterExample e8 = new CounterExample.Builder().from("boby").against(a4.id).build();
		
		ArgTree tree = new ArgTree();
		tree.addRoot(b1);
		tree.addNode(a2);
		tree.addNode(a3);
		tree.addNode(a4);
		tree.addNode(a5);
		tree.addNode(a6);
		tree.addNode(a7);
		tree.addNode(e8);
		
		System.out.println(tree.toString());
		
		System.out.println(tree.getLeaves(a4.id));
		
		System.out.println(tree.getLeaves());
		
		System.out.println(tree.getLeaves(e8.id));
		
		System.out.println(tree.deleteNode(a4.id));
		
		System.out.println(tree.toString());
		
		
	}

}
