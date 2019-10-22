package evaluation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ExpFileManager {

    private static class Block<T>{
        private String name;
        private T value;

        Block(String name){
            this.name = name;
        }

        Block(String name, T value){
            this.name = name;
            this.value = value;
        }

        public String type(){
            return this.name;
        }

        public String toString(){
            if(this.value == null)
                return "NC";
            return this.value.toString();
        }
    }

    private static final ArrayList<String> order = new ArrayList<>();
    private static final Map<String, ArrayList<Block>> blocks = new HashMap<>();

    public static void addBlock(Block block){
        if(!order.contains(block.name))
            order.add(block.name);
        blocks.putIfAbsent(block.type(), new ArrayList<>());
        blocks.get(block.type()).add(block);
    }

    public static void addBlock(String name, Object value){
        if(!order.contains(name))
            order.add(name);
        Block<Object> block = new Block<>(name, value);
        blocks.putIfAbsent(block.type(), new ArrayList<>());
        blocks.get(block.type()).add(block);
    }

    public static void nextLine(){
        HashSet<Integer> sizes = new HashSet<>();
        for(List l : blocks.values())
            sizes.add(l.size());
        int max = Collections.max(sizes);
        for(Map.Entry<String,ArrayList<Block>> ent : blocks.entrySet()){
            while(ent.getValue().size() <max){
                ent.getValue().add(new Block(ent.getKey()));
            }
        }
    }

    public static void createDraft() {
        // Create new folder address
        Calendar cal = Calendar.getInstance();
        String address = System.getProperty("user.dir")+"/Results";
        address += "/Experiment_"+cal.get(Calendar.YEAR)+"_"+cal.get(Calendar.MONTH)+"_"+cal.get(Calendar.DAY_OF_MONTH)+
                "_"+cal.get(Calendar.HOUR_OF_DAY)+"h"+cal.get(Calendar.MINUTE)+"m"+cal.get(Calendar.SECOND);
        // Create new folder
        try {
            Files.createDirectories(Paths.get(address));
            System.out.println(">>> Result directory = "+address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Write the results
        BufferedWriter writerExp;
        try {
            writerExp = new BufferedWriter(new FileWriter(address + "/exp_info.csv", true));
            // Write header
            for (int i=0; i < order.size(); i++) {
                writerExp.append(order.get(i));
                if (i < order.size() - 1)
                    writerExp.append(";");
                else
                    writerExp.append("\n");
            }
            // Get the size of the data-set
            HashSet<Integer> sizes = new HashSet<>();
            for(List l : blocks.values())
                sizes.add(l.size());
            int min = Collections.min(sizes);
            // Write the results
            for(int i=0; i<min; i++) {
                for(int j=0; j < order.size(); j++) {
                    writerExp.append(blocks.get(order.get(j)).get(i).toString());
                    if (j < order.size() - 1)
                        writerExp.append(";");
                    else
                        writerExp.append("\n");
                }
            }
            writerExp.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
