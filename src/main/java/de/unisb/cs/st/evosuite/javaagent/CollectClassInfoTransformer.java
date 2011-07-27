package de.unisb.cs.st.evosuite.javaagent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import de.unisb.cs.st.evosuite.Properties;


/**
 * Collects information about all classes used in the given class 
 * 
 * @author Andrey Tarasevich
 *
 */
public class CollectClassInfoTransformer implements ClassFileTransformer {
	
	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		String classNameWithDots = className.replace('/', '.');

		if (!classNameWithDots.startsWith(Properties.PROJECT_PREFIX))
				return null;
		ClassReader reader = new ClassReader(classfileBuffer);
		ClassWriter writer = new ClassWriter(reader, 0);
		CIClassAdapter ca = new CIClassAdapter(writer);
		reader.accept(ca, 0);

		saveCItoFile(ca.getClassesReferenced(), className);
		
		return classfileBuffer;
	}
	
	private void saveCItoFile(Set<String> cr, String className){
		try {
			cr.remove(className);
			FileWriter fw = new FileWriter("evosuite-files/" + className.replace("/", ".") + ".CIs");
			BufferedWriter bw = new BufferedWriter(fw);
			String lineSeparator = System.getProperty("line.separator");
			for(String s : cr)
				bw.write(s + lineSeparator);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}