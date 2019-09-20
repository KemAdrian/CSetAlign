package arguments;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import identifiers.ArgID;
import interfaces.Node;
import tools.Pair;

public class ArgTree {

	public Node root;
	public Map<ArgID, Node> nodes;
	public Set<Pair<ArgID, ArgID>> segments;
	public boolean agreed_upon;

	public ArgTree() {
		this.root = null;
		this.nodes = new HashMap<>();
		this.segments = new HashSet<>();
		this.agreed_upon = false;
	}

	public boolean contains(ArgID id) {
		if (root == null)
			return false;
		if (root.getid().equals(id))
			return true;
		return nodes.keySet().contains(id);
	}

	public ArgID rootID() {
		if (root == null)
			return null;
		return root.getid();
	}

	public boolean addRoot(Node b) {
		this.root = b;
		return true;
	}

	public boolean addNode(Node n) {
		if (root == null) {
			System.out.println("      > Problem: cannot add node to a tree that is rootless");
			return false;
		}
		if (root.getid().equals(n.attacks()) || nodes.containsKey(n.attacks())) {
			nodes.put(n.getid(), n);
			segments.add(new Pair<ArgID, ArgID>(n.attacks(), n.getid()));
			return true;
		}
		return false;
	}

	public Node getNode(ArgID id) {
		if (root == null)
			return null;
		if (root.getid().equals(id))
			return root;
		return nodes.get(id);
	}
	
	public Node getAttackNode(ArgID id) {
		if (root == null)
			return null;
		return nodes.get(id);
	}

	public boolean deleteNode(ArgID n) {
		if (root.getid().equals(n)) {
			root = null;
			nodes = new HashMap<ArgID, Node>();
			segments = new HashSet<Pair<ArgID, ArgID>>();
		}
		if (!nodes.containsKey(n))
			return false;
		for (Pair<ArgID, ArgID> s : new HashSet<>(segments)) {
			if (s.getLeft().equals(n)) {
				deleteNode(nodes.get(s.getRight()).getid());
			}
			if (s.getRight().equals(n)) {
				segments.remove(s);
			}
		}
		nodes.remove(n);
		return true;
	}

	public Set<Node> getLeaves() {
		Set<Node> leaves = new HashSet<>();
		if (root != null)
			leaves = getLeaves(root.getid());
		if(leaves.isEmpty() && root != null)
			leaves.add(root);
		return leaves;
	}

	public Set<Node> getLeaves(ArgID node) {
		Set<Node> leaves = new HashSet<>();
		boolean found_son = false;
		for (Pair<ArgID, ArgID> s : segments) {
			if (s.getLeft().equals(node)) {
				found_son = true;
				leaves.addAll(getLeaves(s.getRight()));
			}
		}
		if (!found_son && nodes.containsKey(node)) {
			leaves.add(nodes.get(node));
		}
		return leaves;
	}

	public String toString(ArgID node) {
		return toString(node, 0);
	}

	public String toString(ArgID node, int d) {
		String tab = "";
		for (int i = 0; i < d; i++) {
			tab += "   ";
		}
		String output = (tab + node + "\n");
		for (Pair<ArgID, ArgID> s : segments) {
			if (s.getLeft().equals(node)) {
				output += toString(s.getRight(), d + 1);
			}
		}
		return output;
	}

	public String toString() {
		if (root == null)
			return "empty tree";
		return toString(root.getid());
	}

}
