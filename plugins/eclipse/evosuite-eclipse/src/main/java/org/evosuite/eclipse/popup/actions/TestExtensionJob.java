package org.evosuite.eclipse.popup.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.evosuite.Properties;
import org.evosuite.utils.Utils;

public class TestExtensionJob extends TestGenerationJob {

	private final String testClass;

	private final String ENCODING = "UTF-8";

	private String classPath;

	private File tempDir;

	public TestExtensionJob(Shell shell, final IResource target, String targetClass,
	        String testClass) {
		super(shell, target, targetClass);
		this.testClass = testClass;
		IJavaProject jProject = JavaCore.create(target.getProject());
		try {
			classPath = target.getWorkspace().getRoot().findMember(jProject.getOutputLocation()).getLocation().toOSString();
			tempDir = setupTempDir();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			classPath = "";
			tempDir = new File("/tmp");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			tempDir = new File("/tmp");
		}

	}

	protected CompilationUnit parseJavaFile(String unitName, String fileName)
	        throws IOException {
		String fileContents = Utils.readFileToString(fileName);
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		
		Map<String, String> COMPILER_OPTIONS = new HashMap<String, String>(JavaCore.getOptions());
		 COMPILER_OPTIONS.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
		    COMPILER_OPTIONS.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
		    COMPILER_OPTIONS.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
		
		//parser.setResolveBindings(true);
		//parser.setBindingsRecovery(true);
		parser.setUnitName(unitName);
		//@SuppressWarnings("unchecked")
		//Hashtable<String, String> options = JavaCore.getDefaultOptions();
		//options.put(JavaCore.COMPILER_SOURCE, SOURCE_JAVA_VERSION);
		//parser.setCompilerOptions(options);
		String[] encodings = new String[1];
		encodings[0] = ENCODING;
		String[] classpath = new String[1];
		classpath[0] = classPath;
		String[] sources = new String[1];
		File sourceFile = new File(fileName);
		sources[0] = sourceFile.getParent();
		//parser.setEnvironment(classpath, sources, encodings, true);
		parser.setSource(fileContents.toCharArray());
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		Set<String> problems = new HashSet<String>();
		for (IProblem problem : compilationUnit.getProblems()) {
			
			problems.add(problem.getSourceLineNumber() +": "+problem.toString());
		}
		if (!problems.isEmpty()) {
			System.out.println("Got " + problems.size()
			        + " problems compiling the source file: ");
			for (String problem : problems) {
				System.out.println(problem);
			}
		}
		return compilationUnit;
	}

	protected String getTestClassName() {
		String path = tempDir.getAbsolutePath();
		path += File.separator;
		path += targetClass.replace(".", "/");
		int pos = path.lastIndexOf(File.separator);
		// path = path.substring(0, pos+1) + "Test" + path.substring(pos+1);
		path += Properties.JUNIT_SUFFIX;
		path += ".java";
		return path;
	}

	protected List<MethodDeclaration> getTestContent(String fileName) throws IOException {
		System.out.println("Trying to parse file: "+fileName);

		CompilationUnit compilationUnit = parseJavaFile(testClass, fileName);
		MethodExtractingVisitor visitor = new MethodExtractingVisitor();
		compilationUnit.accept(visitor);
		return visitor.getMethods();
	}

	protected File setupTempDir() throws IOException {
		File temp;

		temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

		if (!(temp.delete())) {
			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		}

		if (!(temp.mkdir())) {
			throw new IOException("Could not create temp directory: "
			        + temp.getAbsolutePath());
		}
		return temp;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.eclipse.popup.actions.TestGenerationJob#getAdditionalParameters()
	 */
	@Override
	public List<String> getAdditionalParameters() {

		List<String> parameters = new ArrayList<String>();
		parameters.add("-Dtest_dir=" + tempDir.getAbsolutePath());
		parameters.add("-Djunit_extend="+testClass);
		parameters.add("-Dselected_junit="+testClass);
		parameters.add("-Dtest_factory=JUnit");
		System.out.println("Providing output dir: "+tempDir.getAbsolutePath());
		return parameters;
	}
	
	private boolean hasMethod(IType classType, String methodName) throws JavaModelException {
		for(IMethod method : classType.getMethods()) {
			if(method.getElementName().equals(methodName))
				return true;
		}
		return false;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus status = super.run(monitor);

		IJavaElement element = JavaCore.create(target);
		if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
			ICompilationUnit compilationUnit = (ICompilationUnit) element;
			CodeFormatter formatter = ToolFactory.createCodeFormatter(null);

			try {
				IType classType = compilationUnit.getTypes()[0];
				List<MethodDeclaration> methods = getTestContent(getTestClassName());
				for(MethodDeclaration method : methods) {
					
					if(hasMethod(classType, method.getName().toString())) {
						if(method.getName().toString().equals("initEvoSuiteFramework"))
							continue;
						
						System.out.println("Already have method: "+method.getName());
						int num = 1;
						method.setName(method.getAST().newSimpleName(method.getName().toString()+"_"+num));
						while(hasMethod(classType, method.getName().toString())) {
							num += 1;
							String name = method.getName().toString();
							method.setName(method.getAST().newSimpleName(name.substring(0, name.length() - 2)+"_"+num));
						}
					} //else {
						String testContent = method.toString();
						
						IMethod newMethod = classType.createMethod(testContent, null, false,
								new NullProgressMonitor());
						ISourceRange range = newMethod.getSourceRange();
						TextEdit indent_edit =
						  formatter.format(CodeFormatter.K_COMPILATION_UNIT, 
						    classType.getCompilationUnit().getSource(), range.getOffset(), range.getLength(), 0, null);
						classType.getCompilationUnit().applyTextEdit(indent_edit, null);
					//}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return status;
	}

}
