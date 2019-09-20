package sandboxes;

import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Sort;
import csic.iiia.ftl.base.core.Symbol;
import csic.iiia.ftl.base.core.SymbolFeatureTerm;
import csic.iiia.ftl.base.core.TermFeatureTerm;
public class SimpleMain {
	
	public static void main(String[] arg) throws Exception{
		
		Ontology o = new Ontology();
		FTKBase dm = new FTKBase();
		
		Sort ontology = new Sort(new Symbol("test-ontology"), null, o);
		Sort cas = new Sort(new Symbol("case"), ontology,o);
		Sort object = new Sort(new Symbol("object"), cas,o);
		
		Symbol description = new Symbol("description");
		Symbol solution = new Symbol("solution");
		Symbol attribute_1 = new Symbol("attribute_1") ;
		Symbol attribute_2 = new Symbol("attribute_2") ;
		
		SymbolFeatureTerm label_1 = new SymbolFeatureTerm(new Symbol("label_1"), o);
		SymbolFeatureTerm label_2 = new SymbolFeatureTerm(new Symbol("label_2"), o);
		SymbolFeatureTerm value_1 = new SymbolFeatureTerm(new Symbol("value_1"), o);
		SymbolFeatureTerm value_2 = new SymbolFeatureTerm(new Symbol("value_2"), o);
		
		
		TermFeatureTerm case1 = new TermFeatureTerm("case-1", cas);
		TermFeatureTerm case1_description = new TermFeatureTerm(object);
		case1_description.addFeatureValue(attribute_1, value_1);
		case1_description.addFeatureValue(attribute_2, value_2);
		case1.addFeatureValue(description, case1_description);
		case1.addFeatureValue (solution, label_1);
		
		TermFeatureTerm case2 = new TermFeatureTerm("case-2", cas);
		TermFeatureTerm case2_description = new TermFeatureTerm(object);
		case2_description.addFeatureValue(attribute_1, value_1);
		case2_description.addFeatureValue(attribute_2, value_2);
		case2.addFeatureValue(description, case2_description);
		case2.addFeatureValue (solution, label_1);
		
		TermFeatureTerm case3 = new TermFeatureTerm("case-3", cas);
		TermFeatureTerm case3_description = new TermFeatureTerm(object);
		case3_description.addFeatureValue(attribute_1, value_1);
		case3_description.addFeatureValue(attribute_2, value_2);
		case3.addFeatureValue(description, case3_description);
		case3.addFeatureValue (solution, label_2);
	
		System.out.println(case1.toStringNOOS(dm)+"\n");
		System.out.println(case2.toStringNOOS(dm)+"\n");
		System.out.println(case3.toStringNOOS(dm)+"\n");
		System.out.println(case1.featureValue(description).equivalents(case2.featureValue(description)));
		System.out.println(case1.featureValue(description).equivalents(case3.featureValue(description)));
		
	}

}
