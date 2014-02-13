package org.evosuite.statistics;

/**
 * <p>
 * This enumeration defines all the runtime variables we want to store in
 * the CSV files. Note, it is perfectly fine to add new ones, in any
 * position. Just be sure to define a proper mapper in {@code getCSVvalue}.
 * </p>
 * 
 * <p>
 * WARNING: do not change the name of any variable! If you do, current R
 * scripts will break. If you really need to change a name, please first
 * contact Andrea Arcuri.
 * </p>
 * 
 * @author arcuri
 * 
 */
public enum RuntimeVariable {
	
	/** Class under test */  
	Class,  
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
	/** the actual covered line numbers */
	Covered_Lines,
	/** Total number of methods in CUT */
	Total_Methods,       
	/** Number of methods without any predicates */
	Branchless_Methods, 
	/** Number of methods covered */
	Covered_Methods,    
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
	Tests_Executed, 
	Fitness_Evaluations,
	Generations,
	/** Obtained coverage of the chosen testing criterion */
	Coverage,            
	/** Fitness value of the best individual */
	Fitness,            
	/** Number of tests in resulting test suite */
	Size,                
	/** Total number of statements in final test suite */
	Length,              
	/** Obtained coverage (of the chosen testing criterion) at different points in time  */
	CoverageTimeline,
	FitnessTimeline,
	SizeTimeline,
	LengthTimeline,
	/** Not only the covered branches ratio, but also including the branchless methods */
	BranchCoverage,
	NumberOfGeneratedTestCases,
	/** The number of serialized objects that EvoSuite is going to use for seeding strategies */
	NumberOfInputPoolObjects,
	AllDefCoverage,
	DefUseCoverage,
	WeakMutationScore,
	Creation_Time,
	Minimization_Time,
	Total_Time,
	Test_Execution_Time,
	Goal_Computation_Time,
	Result_Size,
	Result_Length,
	Minimized_Size,
	Minimized_Length,
	Chromosome_Length,
	Population_Size,
	Random_Seed,
	Budget,
	Lines,
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
	Threads,
	CoveredBranchesBitString,
	StatementCoverage,
	MutationScore,
	Explicit_MethodExceptions,
	Explicit_TypeExceptions,
	Implicit_MethodExceptions,
	Implicit_TypeExceptions,
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
	/*
	 * Dataflow stuff
	 */
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
	CoveredAliasParameterPairs,
	CarvedTests,
	CarvedCoverage,
	HadUnstableTests
};
