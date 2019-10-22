package sandboxes;

import csic.iiia.ftl.base.core.*;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.base.utils.SingletonFeatureTermException;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.lazymethods.similarity.AUDistance;

import java.io.IOException;
import java.util.*;

public class SandboxThesisExample {
	
	public static void main(String[] args) throws FeatureTermException, IOException {
		
		Ontology o = new Ontology();
		FTKBase dm = new FTKBase();

		Random r = new Random();
		TrainingSetProperties out = new TrainingSetProperties();

		// Generate Ontology structure
		List<Symbol> attributes = new ArrayList<>();
		List<TermFeatureTerm> values = new ArrayList<>();
		Map<String,Symbol> string2attribute = new HashMap<>();

		// Create the sorts
		Sort any = o.getSort("any");
		any.setDataType(3);
		Sort ontology = new Sort(new Symbol("test-ontology"), any, o);
		Sort object = new Sort(new Symbol("object"), ontology,o);
		Sort feature = new Sort(new Symbol("feature"),ontology,o);
		// List of attributes
		List<String> attribute_list = List.of("6_legs", "vertebra", "eggs","warm_blood","fly","carnivorous","terrestrial","social","4_legs","nocturnal","tail","brown","grey","transparent","small");
		for(String s : attribute_list) {
			Symbol symbol = new Symbol(s);
			attributes.add(symbol);
		}
		// Create the different values
		for(Symbol att : attributes) {
			values.add(new TermFeatureTerm(new Symbol("0"), feature));
			values.add(new TermFeatureTerm(new Symbol("1"), feature));
		}
		for(FeatureTerm ft : values)
			dm.addFT(ft);
		// Set up objects
		for(int i=0; i<attribute_list.size(); i++){
			object.addFeature(attributes.get(i), feature, null, false);
		}
		// Create rules for generalizations
		FeatureTerm C31 = createFT("C31", attributes, values, List.of(-1,1,-1,-1,1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1), object, feature );
		FeatureTerm C32 = createFT("C32",attributes, values, List.of(-1,1,1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1), object, feature );
		FeatureTerm C4 = createFT("C4", attributes, values, List.of(-1,1,-1,1,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1), object, feature );
		// Create examples
		FeatureTerm brownBat = createFT("brownBat",attributes, values, List.of(0,1,0,1,1,1,0,0,0,1,1,1,0,0,0), object, feature );
		FeatureTerm pipistrelle = createFT("pipistrelle", attributes, values, List.of(0,1,0,1,1,1,0,0,0,1,1,0,1,0,1), object, feature );
		FeatureTerm hoaryBat = createFT("hoaryBat", attributes, values, List.of(0,1,0,1,1,1,0,1,0,1,1,0,1,0,1), object, feature );
		FeatureTerm myotis = createFT("myotis", attributes, values, List.of(0,1,0,1,1,1,0,1,0,1,1,1,0,0,1), object, feature );
		// Lists
		List<FeatureTerm> rules = List.of(C31,C32,C4);
		List<FeatureTerm> examples = List.of(brownBat,pipistrelle,hoaryBat,myotis);
		// Display
		for(FeatureTerm f : rules){
			for(FeatureTerm t : examples){
				System.out.println(f.getName()+" subsumes "+t.getName()+": "+f.subsumes(t)+"    ");
			}
			System.out.println("");
		}
		AUDistance au = new AUDistance();
		for(FeatureTerm f : rules){
			for(FeatureTerm t : examples){
				System.out.println(f.getName()+" distance vs "+t.getName()+": "+au.distance(f,t,o,dm) +"    ");
			}
			System.out.println("");
		}
	}

	private static FeatureTerm createFT(String name, List<Symbol> features, List<TermFeatureTerm> values,  List<Integer> array, Sort object, Sort feature) throws FeatureTermException {
		TermFeatureTerm out = new TermFeatureTerm(object);
		for(int i=0; i<features.size(); i++){
			try {
				if(array.get(i) > -1)
					out.addFeatureValue(features.get(i), values.get(array.get(i)));
			} catch (SingletonFeatureTermException e) {
				e.printStackTrace();
			}
		}
		return out;
	}

}
