/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.eclipse.popup.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
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
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.evosuite.Properties;
import org.evosuite.eclipse.Activator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;

public class TestExtensionJob extends TestGenerationJob {

    private File tempDir;
    private ArrayList<String> newTests;
    private List<MethodDeclaration> newMethods = null;
    private List<ImportDeclaration> newImports = null;

    public TestExtensionJob(Shell shell, final IResource target, String targetClass,
                            String testClass) {
        super(shell, target, targetClass, testClass);
        newTests = new ArrayList<String>();
        try {
            tempDir = setupTempDir();
        } catch (IOException e) {
            e.printStackTrace();
            tempDir = new File("/tmp");
        }

    }

    protected static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    protected CompilationUnit parseJavaFile(String unitName, String fileName)
            throws IOException {
        String fileContents = readFile(fileName, Charset.defaultCharset());
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setStatementsRecovery(true);

        Map<String, String> COMPILER_OPTIONS = new HashMap<String, String>(JavaCore.getOptions());
        COMPILER_OPTIONS.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
        COMPILER_OPTIONS.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_7);
        COMPILER_OPTIONS.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);

        //parser.setResolveBindings(true);
        //parser.setBindingsRecovery(true);
        parser.setUnitName(unitName);
        String[] encodings = {ENCODING};
        String[] classpaths = {classPath};
        String[] sources = {new File(suiteClass).getParent()};
        parser.setEnvironment(classpaths, sources, encodings, true);
        parser.setSource(fileContents.toCharArray());
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        Set<String> problems = new HashSet<String>();
        for (IProblem problem : compilationUnit.getProblems()) {
            problems.add(problem.getSourceLineNumber() + ": " + problem.toString());
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
        path += targetClass.replace('.', '/');
        // int pos = path.lastIndexOf(File.separator);
        // path = path.substring(0, pos+1) + "Test" + path.substring(pos+1);
        path += Properties.JUNIT_SUFFIX;
        path += ".java";
        return path;
    }

    protected void loadTestSuiteContent(String fileName) throws IOException {
        System.out.println("Trying to parse file: " + fileName);

        CompilationUnit compilationUnit = parseJavaFile(suiteClass, fileName);
        MethodExtractingVisitor methodVisitor = new MethodExtractingVisitor();
        ImportDeclarationVisitor importVisitor = new ImportDeclarationVisitor();
        compilationUnit.accept(methodVisitor);
        compilationUnit.accept(importVisitor);
        newMethods = methodVisitor.getMethods();
        newImports = importVisitor.getImports();
    }

    protected File setupTempDir() throws IOException {
        File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
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
        parameters.add("-Djunit_extend=" + suiteClass);
        parameters.add("-Dselected_junit=" + suiteClass);
        parameters.add("-Dtest_factory=JUnit");
        System.out.println("Providing output dir: " + tempDir.getAbsolutePath());
        return parameters;
    }

    private boolean hasMethod(IType classType, String methodName) throws JavaModelException {
        for (IMethod method : classType.getMethods()) {
            if (method.getElementName().equals(methodName))
                return true;
        }
        return false;
    }

    private boolean hasImport(ICompilationUnit compilationUnit, ImportDeclaration importDecl) throws JavaModelException {
        IImportDeclaration[] imports = compilationUnit.getImports();
        String importName = importDecl.getName().toString();
        for (IImportDeclaration imp : imports) {
            if (imp.getElementName().equals(importName))
                return true;
        }
        return false;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        IStatus status = super.run(monitor);

        newTests.clear();
        IJavaElement element = JavaCore.create(target);
        if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
            ICompilationUnit compilationUnit = (ICompilationUnit) element;
            CodeFormatter formatter = ToolFactory.createCodeFormatter(null);

            try {
                if (compilationUnit.getTypes().length == 0) {
                    System.out.println("The compilation unit is empty :|");
                    return status;
                }
                IType classType = compilationUnit.getTypes()[0];
                // new tests
                loadTestSuiteContent(getTestClassName());

                for (ImportDeclaration newImport : newImports) {
                    if (!hasImport(compilationUnit, newImport)) {
                        int flag = newImport.isStatic() ? Flags.AccStatic : Flags.AccDefault;
                        String strImport = newImport.getName().toString();
                        // Names of onDemand import declarations do not contain the '*'
                        if (newImport.isOnDemand()) strImport += ".*";
                        compilationUnit.createImport(strImport, null, flag, null);
                    }
                }

                for (MethodDeclaration newMethod : newMethods) {

                    if (hasMethod(classType, newMethod.getName().toString())) {

                        System.out.println("Test suite already contains method called: " + newMethod.getName());
                        int num = 1;
                        newMethod.setName(newMethod.getAST().newSimpleName(newMethod.getName().toString() + "_" + num));
                        while (hasMethod(classType, newMethod.getName().toString())) {
                            num += 1;
                            String name = newMethod.getName().toString();
                            newMethod.setName(newMethod.getAST().newSimpleName(name.substring(0, name.length() - 2) + "_" + num));
                        }
                    }
                    String testContent = newMethod.toString();

                    IMethod methodToAdd = classType.createMethod(testContent, null, false,
                            new NullProgressMonitor());
                    ISourceRange range = methodToAdd.getSourceRange();
                    TextEdit indent_edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT,
                            classType.getCompilationUnit().getSource(), range.getOffset(), range.getLength(), 0, null);
                    classType.getCompilationUnit().applyTextEdit(indent_edit, null);
                    newTests.add(newMethod.getName().toString());
                }
                classType.getCompilationUnit().commitWorkingCopy(false, null);

            } catch (JavaModelException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return status;
    }
}
