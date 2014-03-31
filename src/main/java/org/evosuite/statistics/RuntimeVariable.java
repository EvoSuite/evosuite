package org.evosuite.statistics;

/**
 * <p>
 * This enumeration defines all the runtime variables we want to store in
 * the CSV files.
 * A runtime variable is either an output of the search (e.g., obtained branch coverage) 
 * or something that can only be determined once the CUT is analyzed (e.g., the number of branches) 
 * 
 * <p>
 * Note, it is perfectly fine to add new runtime variables in this enum, in any position.
 * But it is essential to provide JavaDoc <b>descriptions</b> for each new variable 
 * 
 * <p>
 * WARNING: do not change the name of any variable! If you do, current R
 * scripts will break. If you really need to change a name, please first
 * contact Andrea Arcuri.
 * 
 * @author arcuri
 * 
 */
public enum RuntimeVariable {
	
	/** Number of predicates in CUT */
	Predicates,         
	/** Number of classes in classpath  */
	Classpath_Classes,   
	/**  Number of classes analyzed for test cluster */
	Analyzed_Classes,   
	/** Total number of generators */
	Generators,         
	/** Total number of modifiers */
	Modifiers,          
	/** Total number of branches in CUT */
	Total_Branches,     
	/** Number of covered branches in CUT */
	Covered_Branches,
	/** The number of lines in the CUT */
	Lines,
	/** The actual covered line numbers */
	Covered_Lines,
	/** Total number of methods in CUT */
	Total_Methods,       
	/** Number of methods covered */
	Covered_Methods,    
	/** Number of methods without any predicates */
	Branchless_Methods, 
	/** Number of methods without predicates covered */
	Covered_Branchless_Methods, 
	/** Total number of coverage goals for current criterion */
	Total_Goals,         
	/** Total number of covered goals */
	Covered_Goals,       
	/** Number of mutants */
	Mutants,            
	/** Total number of statements executed */
	Statements_Executed, 
	/** The total number of tests executed during the search */
	Tests_Executed, 
	/** The total number of fitness evaluations during the search */
	Fitness_Evaluations,
	/** Number of generations the search algorithm has been evolving */
	Generations,
	/** Obtained coverage of the chosen testing criterion */
	Coverage,            
	/** Fitness value of the best individual */
	Fitness,            
	/** Obtained coverage (of the chosen testing criterion) at different points in time  */
	CoverageTimeline,
	/** Obtained fitness values at different points in time */
	FitnessTimeline,
	/** Obtained size values at different points in time */
	SizeTimeline,
	/** Obtained length values at different points in time */
	LengthTimeline,
	/** The obtained statement coverage */
	StatementCoverage,
	/** Not only the covered branches ratio, but also including the branchless methods. FIXME: this will need to be changed */
	BranchCoverage,
	/** A bit string (0/1) representing whether branches (in order) are covered */
	CoveredBranchesBitString,
	/** The number of serialized objects that EvoSuite is going to use for seeding strategies */
	NumberOfInputPoolObjects,
	/** The obtained score for weak mutation testing */
	WeakMutationScore,
	/** The obtained score for (strong) mutation testing*/
	MutationScore,
	/** The total time EvoSuite spent generating the test cases */
	Total_Time,
	/** Number of tests in resulting test suite */
	Size,                
	/** Total number of statements in final test suite */
	Length,   
	/** TODO */
	Result_Size,
	/** TODO */
	Result_Length,
	/** TODO */
	Minimized_Size,
	/** TODO */
	Minimized_Length,
	/** The random seed used during the search. A random one was used if none was specified at the beginning */
	Random_Seed,
	/** TODO */
	CarvedTests,
	/** TODO */
	CarvedCoverage, 
	/** TODO */
	HadUnstableTests, 
	/** An estimate (ie not precise) of the maximum number of threads running at the same time in the CUT */
	Threads,
	/** TODO */
	Explicit_MethodExceptions, 
	/** TODO */
	Explicit_TypeExceptions, 
	/** TODO */
	Implicit_MethodExceptions, 
	/** TODO */
	Implicit_TypeExceptions, 
	/* ----- number of unique permissions that were denied for each kind --- */
	AllPermission,
	SecurityPermission,
	UnresolvedPermission,
	AWTPermission,
	FilePermission,
	SerializablePermission,
	ReflectPermission,
	RuntimePermission,
	NetPermission,
	SocketPermission,
	SQLPermission,
	PropertyPermission,
	LoggingPermission,
	SSLPermission,
	AuthPermission,
	AudioPermission,
	OtherPermission,
	/* -------------------------------------------------------------------- */
	
	
	/* TODO following needs to be implemented/updated. Currently they are not (necessarily) supported */
	Error_Predicates,
	Error_Branches_Covered,
	Error_Branchless_Methods,
	Error_Branchless_Methods_Covered,
	AssertionContract,
	EqualsContract,
	EqualsHashcodeContract,
	EqualsNullContract,
	EqualsSymmetricContract,
	HashCodeReturnsNormallyContract,
	JCrasherExceptionContract,
	NullPointerExceptionContract,
	ToStringReturnsNormallyContract,
	UndeclaredExceptionContract,
	Contract_Violations,
	Unique_Violations,
	Data_File,
	/* --- Dataflow stuff. FIXME: Is this stuff still valid? --- */
	AllDefCoverage,
	DefUseCoverage,	
	Definitions,
	Uses,
	DefUsePairs,
	IntraMethodPairs,
	InterMethodPairs,
	IntraClassPairs,
	ParameterPairs,
	LCSAJs,
	AliasingIntraMethodPairs,
	AliasingInterMethodPairs,
	AliasingIntraClassPairs,
	AliasingParameterPairs,
	CoveredIntraMethodPairs,
	CoveredInterMethodPairs,
	CoveredIntraClassPairs,
	CoveredParameterPairs,
	CoveredAliasIntraMethodPairs,
	CoveredAliasInterMethodPairs,
	CoveredAliasIntraClassPairs,
	CoveredAliasParameterPairs
	/* -------------------------------------------------- */
};
