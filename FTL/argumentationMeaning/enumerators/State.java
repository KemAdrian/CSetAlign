package enumerators;

import interfaces.Agent;

/**
 *  The different phases of an argumentation, that defines the {@link Agent}'s behaviour when he uses the method {@link interfaces.Agent#turn()}.
 * 
 * @author kemoadrian
 *
 */
public enum State {

	// Initial state in general approach
	Initial,
	// Handle the presentation of a sign in lazy approach
	WaitExample, NameExample,
	// Evaluate the names of an example
	EvaluateSignsState,
	// First exchange of intensional definitions in the general approach
	SendIDState, ReceiveIDState,
	// Chose disagreement
	ChooseDisagreementState,
	// Delet blind concepts
	DeleteBlindState,
	// Fix Boundaries
	FixBoundariesState,
	// Creation of a new extensional definition
	BuildEDState, CheckEDState,
	// Creation of a new intensional definition and concept
	BuildSignState, BuildIDState, BuildIdsState, CheckIdsState, BuildConState, DeleteConState,
	// Create the set of connected disagreements and eventualy expend it in lazy approach
	ExchangeRelatedState, EvaluateReadinessState,
	// Remove the concepts of the set of connected disagreements from the final contrast set in lazy approach
	TransferConceptState, 
	// Evaluating new concepts
	SendEvaluationState, DraftEvaluationState, UpdateEvaluationState, MakeEvaluationState,
	// Check internal equalities
	SendSelfInternalEqualitiesState, CheckOtherInternalEqualitiesState, CheckSelfInternalEqualitiesState, ValidOtherInternalEqualitiesState,
	// Check external equalities
	SendExternalEqualitiesState,
	// Update containers
	UpdateContrastSetState, UpdateHypothesisState,
	// Update disagreements,
	UpdateDisagreementsState,
	// Update signs
	UpdateSignsState,
	// Refactor signs
	VoteForSignsState, ElectSignsState,
	// Put the concepts of the current contrast set in the final contrast set in lazy approach
	UpgradeKnowledgeState,
	// End
	Stop;
}
