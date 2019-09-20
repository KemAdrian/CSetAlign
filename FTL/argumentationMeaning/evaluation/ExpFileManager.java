package evaluation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import interfaces.Agent;
import tools.FTGen;
import tools.MutableInt;

public class ExpFileManager {
	
	public static int SAVE_FTGEN = 0;
	public static int RECORD = 0;
	
	// Dependent Variables
	public static int success = -1;
	public static int nb_examples = 0;
	public static int nb_concerned = 0;
	public static double exchange_ratio = 0.;
	public static double i_amail_sync_agreement = 0;
	public static double f_amail_sync_agreement = 0;
	public static double final_sync_agreement = 0;
	public static double initial_sync_agreement = 0;
	public static int expected_concepts = 0;
	public static int observed_concepts = 0;
	// Thresholds
	public static int threshold = 0;
	public static float abui_threshold = (float) 0.75;
	// Counters for diacronic disagreement
	public static Map<Agent,Double> i_cover = new HashMap<>();
	public static Map<Agent,Double> f_cover = new HashMap<>();
	public static Map<Agent,Double> final_diac_agreement = new HashMap<>();
	public static Map<Agent,Double> initial_diac_agreement = new HashMap<>();
	public static Map<Agent,Double> local_final_sync_agreement = new HashMap<>();
	public static Map<Agent,Double> local_initial_sync_agreement = new HashMap<>();
	// Local disagreement score (initial)
	public static Map<Agent,Integer> i_total_l = new HashMap<>();
	public static Map<Agent,Integer> i_self_l = new HashMap<>();
	public static Map<Agent,Integer> i_overlap_l = new HashMap<>();
	public static Map<Agent,Integer> i_hypohyper_l = new HashMap<>();
	public static Map<Agent,Integer> i_synonym_l = new HashMap<>();
	public static Map<Agent,Integer> i_homonym_l = new HashMap<>();
	public static Map<Agent,Integer> i_blind_l = new HashMap<>();
	public static Map<Agent,Integer> i_untrans_l = new HashMap<>();
	// Local disagreement score (final)
	public static Map<Agent,Integer> f_total_l = new HashMap<>();
	public static Map<Agent,Integer> f_self_l = new HashMap<>();
	public static Map<Agent,Integer> f_overlap_l = new HashMap<>();
	public static Map<Agent,Integer> f_hypohyper_l = new HashMap<>();
	public static Map<Agent,Integer> f_synonym_l = new HashMap<>();
	public static Map<Agent,Integer> f_homonym_l = new HashMap<>();
	public static Map<Agent,Integer> f_blind_l = new HashMap<>();
	public static Map<Agent,Integer> f_untrans_l = new HashMap<>();
	// Counters for exchanger elements
	public static Map<Agent,MutableInt> e_count = new HashMap<>();
	public static Map<Agent,MutableInt> g_count = new HashMap<>();
	// Counter for shared feature value on the rule structure
	public static int shared_feature = 0;
	// Sizes of intensional definitions
	public static String dimensions = "NC";
	
	// Independent variables
	public static int nb_domain = 0;
	public static String domain = "NC";
	public static String strategy = "NC";
	public static int n = 0;
	public static int redundancy = 0;
	public static int nb_overlap = 0;
	public static int nb_hyponym = 0;
	public static int nb_homonym = 0;
	public static int nb_synonym = 0;
	public static int initial_concepts = 0;
	
	// Observed initial disagreements
	public static int i_total = 0;
	public static int i_self = 0;
	public static int i_overlap = 0;
	public static int i_hypohyper = 0;
	public static int i_synonym = 0;
	public static int i_homonym = 0;
	public static int i_blind = 0;
	public static int i_untrans = 0;
	
	// Observed final disagreements
	public static int f_total = 0;
	public static int f_self = 0;
	public static int f_overlap = 0;
	public static int f_hypohyper = 0;
	public static int f_synonym = 0;
	public static int f_homonym = 0;
	public static int f_blind = 0;
	public static int f_untrans = 0;
	
	
	// To write on file
	private static String address;
	private static String address_p;
	private static List<String> ExpDraft;
	private static List<String> ExpDraft_p;
	
	public static void createDraft() {
		// Create initial drafts
		ExpDraft = new ArrayList<String>();
		// Create new folder address
		Calendar cal = Calendar.getInstance();
		address = System.getProperty("user.dir")+"/Results";
		address += "/Experiment_"+cal.get(Calendar.YEAR)+"_"+cal.get(Calendar.MONTH)+"_"+cal.get(Calendar.DAY_OF_MONTH)+"_"+cal.get(Calendar.HOUR_OF_DAY)+"h"+cal.get(Calendar.MINUTE)+"m"+cal.get(Calendar.SECOND);
		if(RECORD > 1) {
			ExpDraft_p = new ArrayList<String>();
			address_p = System.getProperty("user.dir")+"/Results_parameter";
			address_p += "/Experiment_"+cal.get(Calendar.YEAR)+"_"+cal.get(Calendar.MONTH)+"_"+cal.get(Calendar.DAY_OF_MONTH)+"_"+cal.get(Calendar.HOUR_OF_DAY)+"h"+cal.get(Calendar.MINUTE)+"m"+cal.get(Calendar.SECOND);
		}
		// Create new folder
		try {
			Files.createDirectories(Paths.get(address));
			if(RECORD > 1)
				Files.createDirectories(Paths.get(address_p));
			System.out.println(">>> Result directory = "+address);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveDraft() {
		BufferedWriter writerExp;
		BufferedWriter writerExp_p;
		try {
			writerExp = new BufferedWriter(new FileWriter(address+"/exp_info.csv", true));
			writerExp.append("success ; initial_sync_agreement ; final_sync_agreement ; i_amail_sync_agreement ; f_amail_sync_agreement ;"
					+ "initial_diac_agreement_1 ; final_diac_agreement_1 ; e_count_1 ; g_count_1 ; l_ini_sync_1 ; i_cover_1 ; l_fin_sync_1 ; f_cover_1 ; "
					+ "initial_diac_agreement_2 ; final_diac_agreement_2 ; e_count_2 ; g_count_2 ; l_ini_sync_2 ; i_cover_2 ; l_fin_sync_2 ; f_cover_2 ; "
					+ "strategy ; domain ; threshold ; abui_t ; iterations ; nb_examples ; nb_concerned ; concerned_rt ; redundancy ; "
					+ "exch_ratio ; nb_overlap ; nb_hyponym ; nb_synonym ; nb_homonym ; "
					+ "i_total ; i_self ; i_overlap ; i_hypohyper ; i_syn ; i_hom ; i_blind ; i_un ; "
					+ "f_total ; f_self ; f_overlap ; f_hypohyper ; f_syn ; f_hom ; f_blind ; f_un ; "
					+ "i_total_1 ; i_self_1 ; i_overlap_1 ; i_hypohyper_1 ; i_syn_1 ; i_hom_1 ; i_blind_1 ; i_un_1 ; "
					+ "f_total_1 ; f_self_1 ; f_overlap_1 ; f_hypohyper_1 ; f_syn_1 ; f_hom_1 ; f_blind_1 ; f_un_1 ; "
					+ "i_total_2 ; i_self_2 ; i_overlap_2 ; i_hypohyper_2 ; i_syn_2 ; i_hom_2 ; i_blind_2 ; i_un_2 ; "
					+ "f_total_2 ; f_self_2 ; f_overlap_2 ; f_hypohyper_2 ; f_syn_2 ; f_hom_2 ; f_blind_2 ; f_un_2 ; "
					+ "initial concepts ; expected concepts ; observed concepts");
			if(SAVE_FTGEN > 0)
				writerExp.append(" ; ft_number ; m_sort_size ; sd_sort_size ; m_id_size ; sd_id_size ; m_gen_size ; sd_gen_size ; shared_ft ; dimensions");
			writerExp.append("\n");
			for(String s : ExpDraft)
				writerExp.append(s);
			writerExp.close();
			if(RECORD > 1) {
				writerExp_p = new BufferedWriter(new FileWriter(address_p+"/exp_info.csv", true));
				for(String s : ExpDraft_p)
					writerExp_p.append(s);
				writerExp_p.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeDraft(String s) {
		ExpDraft.add(s+"\n");
	}
	
	public static void writeDraft_p(String s) {
		ExpDraft_p.add(s+"\n");
	}
	
	public static void writeDraft() {
		ExpDraft.add(success+" ; "+initial_sync_agreement+" ; "+final_sync_agreement+" ; "+i_amail_sync_agreement+" ; "+f_amail_sync_agreement+" ; ");
		// First agent-dependant set of variables
		for(Agent a : initial_diac_agreement.keySet())
			ExpDraft.add(initial_diac_agreement.get(a)+" ; "+final_diac_agreement.get(a)+" ; "+e_count.get(a)+" ; "+g_count.get(a)+" ; "+local_initial_sync_agreement.get(a)+" ; "+i_cover.get(a)+" ; "+local_final_sync_agreement.get(a)+" ; "+f_cover.get(a)+" ; ");
		ExpDraft.add(strategy+" ; "+domain+" ; "+threshold+" ; "+abui_threshold+" ; "+n+" ; "+nb_examples+" ; "+nb_concerned+" ; "+((double) nb_concerned)/nb_examples+" ; "+redundancy+" ; "+exchange_ratio+" ; "+nb_overlap+" ; "+nb_hyponym+" ; "+nb_synonym+" ; "+nb_homonym+" ; "+
		i_total+" ; "+i_self+" ; "+i_overlap+" ; "+i_hypohyper+" ; "+i_synonym+" ; "+i_homonym+" ; "+i_blind+" ; "+i_untrans+" ; "+
		f_total+" ; "+f_self+" ; "+f_overlap+" ; "+f_hypohyper+" ; "+f_synonym+" ; "+f_homonym+" ; "+f_blind+" ; "+f_untrans+" ; ");
		// Second agent-dependant set of variables
		for(Agent a : i_total_l.keySet()) {
			ExpDraft.add(i_total_l.get(a)+" ; "+i_self_l.get(a)+" ; "+i_overlap_l.get(a)+" ; "+i_hypohyper_l.get(a)+" ; "+i_synonym_l.get(a)+" ; "+i_homonym_l.get(a)+" ; "+i_blind_l.get(a)+" ; "+i_untrans_l.get(a)+" ; ");
			ExpDraft.add(f_total_l.get(a)+" ; "+f_self_l.get(a)+" ; "+f_overlap_l.get(a)+" ; "+f_hypohyper_l.get(a)+" ; "+f_synonym_l.get(a)+" ; "+f_homonym_l.get(a)+" ; "+f_blind_l.get(a)+" ; "+f_untrans_l.get(a)+" ; ");
		}
		ExpDraft.add(initial_concepts+" ; "+expected_concepts+" ; "+observed_concepts);
		if(SAVE_FTGEN > 0)
			ExpDraft.add(" ; "+FTGen.ft_number+" ; "+FTGen.m_sort_size+" ; "+FTGen.sd_sort_size+" ; "+FTGen.m_id_size+" ; "+FTGen.sd_id_size+" ; "+FTGen.m_gen_size+" ; "+FTGen.sd_gen_size+" ; "+shared_feature+" ; "+dimensions);
		ExpDraft.add("\n");
	}
	
	public static void reset() {
		// Dependent Variables
		success = -1;
		nb_examples = 0;
		nb_concerned = 0;
		exchange_ratio = 0.;
		i_amail_sync_agreement = 0;
		f_amail_sync_agreement = 0;
		final_sync_agreement = 0;
		initial_sync_agreement = 0;
		threshold = 0;
		expected_concepts = 0;
		observed_concepts = 0;
		// Counters for diacronic disagreement
		i_cover.clear();
		f_cover.clear();
		final_diac_agreement.clear();
		initial_diac_agreement.clear();
		local_final_sync_agreement.clear();
		local_initial_sync_agreement.clear();
		// Counters for exchanger elements
		e_count.clear();
		g_count.clear();
		
		// Independent variables
		nb_overlap = 0;
		nb_hyponym = 0;
		nb_homonym = 0;
		nb_synonym = 0;
		initial_concepts = 0;
		
		// Observed initial disagreements
		 i_total = 0;
		 i_self = 0;
		 i_overlap = 0;
		 i_hypohyper = 0;
		 i_synonym = 0;
		 i_homonym = 0;
		 i_blind = 0;
		 i_untrans = 0;
		
		// Observed final disagreements
		 f_total = 0;
		 f_self = 0;
		 f_overlap = 0;
		 f_hypohyper = 0;
		 f_synonym = 0;
		 f_homonym = 0;
		 f_blind = 0;
		 f_untrans = 0;
		 
		// Local disagreement score (initial)
		 i_total_l.clear();
		 i_self_l.clear();
		 i_overlap_l.clear();
		 i_hypohyper_l.clear();
		 i_synonym_l.clear();
		 i_homonym_l.clear();
		 i_blind_l.clear();
		 i_untrans_l.clear();
		// Local disagreement score (final)
		 f_total_l.clear();
		 f_self_l.clear();
		 f_overlap_l.clear();
		 f_hypohyper_l.clear();
		 f_synonym_l.clear();
		 f_homonym_l.clear();
		 f_blind_l.clear();
		 f_untrans_l.clear();
		 
		 // Shared features
		 shared_feature = 0;
		 // Dimensions
		 dimensions = "NC";
	}

}
