package sandboxes;

import java.io.IOException;

import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Sort;
import csic.iiia.ftl.base.core.Symbol;
import csic.iiia.ftl.base.core.SymbolFeatureTerm;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;

public class SandboxFeatureTerms {
	
	public static void main(String[] args) throws FeatureTermException, IOException {
		
		Ontology o = new Ontology();
		FTKBase dm = new FTKBase();
		
		Symbol sy1 = new Symbol("first");
		Symbol sy2 = new Symbol("last");
		Symbol sy3 = new Symbol("city");
		
		Symbol sy4 = new Symbol("name");
		Symbol sy5 = new Symbol("lives-at");
		Symbol sy6 = new Symbol("father");
		
		SymbolFeatureTerm first = new SymbolFeatureTerm(sy1, new Symbol("john"), o);
		SymbolFeatureTerm last = new SymbolFeatureTerm(sy2, new Symbol("Smith"), o);
		SymbolFeatureTerm city = new SymbolFeatureTerm(sy3, new Symbol("NYCity"), o);
		
		Sort s1 = new Sort(new Symbol("test-ontology"), null, o);
		Sort s2 = new Sort(new Symbol("person"), s1, o);
		Sort s3 = new Sort(new Symbol("name"),s2,o);
		Sort s4 = new Sort(new Symbol("address"),s1,o);
		
		TermFeatureTerm name = new TermFeatureTerm(s3);
		TermFeatureTerm lives_at = new TermFeatureTerm(s4);
		TermFeatureTerm father = new TermFeatureTerm(s2);
		TermFeatureTerm fatherName = new TermFeatureTerm(s3);
		TermFeatureTerm case_1 = new TermFeatureTerm("",s2);
		TermFeatureTerm case_2 = new TermFeatureTerm("case-2",s2);
		case_1.setName(null);
		case_2.setName(null);
		name.addFeatureValue(sy1, first);
		name.addFeatureValue(sy2, last);
		lives_at.addFeatureValue(sy3, city);
		father.addFeatureValue(sy4, fatherName);
		fatherName.addFeatureValue(sy2, last);
		case_1.addFeatureValue(sy4, name);
		case_1.addFeatureValue(sy5, lives_at);
		case_1.addFeatureValue(sy6, father);
		
		case_2.addFeatureValue(sy4, name);
		case_2.addFeatureValue(sy5, lives_at);
		case_2.addFeatureValue(sy6, father);
		
		//System.out.println(case_1.toStringNOOS(dm)+"\n");
		System.out.println(case_2.toStringNOOS(dm)+"\n");
		//System.out.println(case_1.equivalents(case_2));
		
		System.out.println(first.getName());
		System.out.println(first.getValue());
		
	}

}
