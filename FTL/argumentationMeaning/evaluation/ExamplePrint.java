package evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import semiotic_elements.Concept;
import semiotic_elements.Example;
import tools.MutableInt;

public class ExamplePrint {
	
	public Map<Concept,String> concept_belonging;
	public Map<Concept,Integer> printMap;
	
	
	public ExamplePrint(Collection<Concept> concepts_adam, Collection<Concept> concepts_boby) {
		this.printMap = new HashMap<>();
		this.concept_belonging = new HashMap<>();
		for(Concept c : concepts_adam) {
			concept_belonging.put(c, "adam");
		}
		for(Concept c : concepts_boby) {
			concept_belonging.put(c, "boby");
		}
		List<Concept> concepts = new ArrayList<>();
		concepts.addAll(concepts_adam);
		concepts.addAll(concepts_boby);
		List<Concept> list = new ArrayList<>(concepts);
		for(int i=0; i<list.size(); i++) {
			printMap.put(list.get(i), (int) Math.pow(2, i));
		}
		System.out.println(printMap);
	}
	
	public int getPrint(Example e) {
		int out = 0;
		for(Entry<Concept, Integer> en : this.printMap.entrySet()) {
			if(en.getKey().covers(e))
				out += en.getValue();
		}
		return out;
	}
	
	public List<String> getConcepts(int i){
		int key = i;
		List<String> out = new ArrayList<>();
		List<Integer> keys = new ArrayList<>(printMap.values());
		keys.sort(Collections.reverseOrder());
		for(Integer j : keys) {
			if(j <= key) {
				out.add(getConcept(j).toString()+"("+concept_belonging.get(getConcept(j))+")");
				key -= j;
			}
		}
		return out;
	}
	
	public Set<Integer> allKeys(){
		return new HashSet<Integer>(printMap.values());
	}
	
	public int maxKey() {
		int max = 0;
		for(Integer i : printMap.values())
			if(i>max)
				max = i;
		return max;
	}
	
	private Concept getConcept(Integer i) {
		for(Entry<Concept,Integer> ent : printMap.entrySet()) {
			if(ent.getValue().equals(i))
				return ent.getKey();
		}
		return null;
	}
	
	public Set<Entry<Integer,MutableInt>> makeCount(Set<Example> examples) {
		Map<Integer, MutableInt> count = new HashMap<>();
		for (Example e : examples) {
			Integer print = this.getPrint(e);
			if (count.get(print) == null)
				count.put(print, new MutableInt());
			count.get(print).increment(1);
		}
		return count.entrySet();
	}

}
