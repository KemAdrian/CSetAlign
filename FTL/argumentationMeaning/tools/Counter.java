package tools;

import interfaces.Agent;

import java.util.HashMap;
import java.util.Map;

public class Counter {

    private static final Map<Agent,Pair<MutableInt,MutableInt>> counters = new HashMap<>();

    public static void set(Agent a, MutableInt e, MutableInt g){
        counters.put(a,new Pair<>(e,g));
    }

    public static MutableInt getExampleCounter(Agent a){
        return counters.get(a).getLeft();
    }

    public static MutableInt getGeneralizationCounter(Agent a){
        return counters.get(a).getRight();
    }

}
