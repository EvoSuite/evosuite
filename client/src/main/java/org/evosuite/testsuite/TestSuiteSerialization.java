package org.evosuite.testsuite;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.utils.DebuggingObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrea Arcuri on 08/06/15.
 */
public class TestSuiteSerialization {

    private static final Logger logger = LoggerFactory.getLogger(TestSuiteSerialization.class);

    public static boolean saveTests(TestSuiteChromosome ts, File folder, String fileName) throws IllegalArgumentException{
        if(ts==null || folder==null || fileName==null){
            throw new IllegalArgumentException("Null inputs");
        }

        if(!folder.exists()){
            folder.mkdirs();
        }

        File target = new File(folder,fileName);

        try(ObjectOutputStream out = new DebuggingObjectOutputStream(new FileOutputStream(target));){

            for (TestChromosome tc : ts.getTestChromosomes()) {
                //tc.getTestCase().removeAssertions();
                out.writeObject(tc);
            }
        }catch (IOException e){
            logger.error("Failed to open/handle "+target.getAbsolutePath()+" for writing: "+e.getMessage());
            return false;
        }

        return true;
    }

    public static List<TestChromosome> loadTests(File folder, String fileName) throws IllegalArgumentException{
        if(folder==null){
            throw new IllegalArgumentException("Null inputs");
        }

        List<TestChromosome> list = new ArrayList<>();
        File target = new File(folder,fileName);

        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(target)) ){

            try {
                Object obj = in.readObject();
                while(obj !=null){
                    if(obj instanceof TestChromosome){
                        //this check might fail if old version is used, and EvoSuite got updated
                        TestChromosome tc = (TestChromosome) obj;
                        list.add(tc);
                    }
                }
            } catch (Exception e) {
                logger.warn("Problems when reading a serialized test from " + target.getAbsolutePath() + " : " + e.getMessage());
            }

        } catch (FileNotFoundException e) {
            logger.warn("Cannot load tests because folder does not exist: "+folder.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to open/handle " + target.getAbsolutePath() + " for reading: " + e.getMessage());
        }

        return list;
    }
}
