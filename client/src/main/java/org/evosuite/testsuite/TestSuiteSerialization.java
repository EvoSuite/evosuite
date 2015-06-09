package org.evosuite.testsuite;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.utils.DebuggingObjectOutputStream;
import org.evosuite.utils.Inputs;
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


    public static boolean saveTests(List<TestSuiteChromosome> list, File target) throws IllegalArgumentException{
        Inputs.checkNull(list,target);

        File parent = target.getParentFile();
        if(!parent.exists()){
            parent.mkdirs();
        }

        try(ObjectOutputStream out = new DebuggingObjectOutputStream(new FileOutputStream(target));){
            for(TestSuiteChromosome ts : list) {
                for (TestChromosome tc : ts.getTestChromosomes()) {
                    out.writeObject(tc);
                }
            }
        }catch (IOException e){
            logger.error("Failed to open/handle "+target.getAbsolutePath()+" for writing: "+e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean saveTests(List<TestSuiteChromosome> ts, File folder, String fileName) throws IllegalArgumentException{
        Inputs.checkNull(ts,folder,fileName);

        if(!folder.exists()){
            folder.mkdirs();
        }

        File target = new File(folder,fileName);
        return saveTests(ts,target);
    }

    public static List<TestChromosome> loadTests(File folder, String fileName) throws IllegalArgumentException {
        Inputs.checkNull(folder, fileName);
        File target = new File(folder,fileName);
        return loadTests(target);
    }

    public static List<TestChromosome> loadTests(String target) throws IllegalArgumentException {
        return loadTests(new File(target));
    }

    public static List<TestChromosome> loadTests(File target) throws IllegalArgumentException{
        Inputs.checkNull(target);

        List<TestChromosome> list = new ArrayList<>();

        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(target)) ){

            try {
                Object obj = in.readObject();
                while(obj !=null){
                    if(obj instanceof TestChromosome){
                        //this check might fail if old version is used, and EvoSuite got updated
                        TestChromosome tc = (TestChromosome) obj;
                        list.add(tc);
                    }
                    obj = in.readObject();
                }
            } catch (EOFException e) {
                //fine
            }catch (Exception e) {
                logger.warn("Problems when reading a serialized test from " + target.getAbsolutePath() + " : " + e.getMessage());
            }

        } catch (FileNotFoundException e) {
            logger.warn("Cannot load tests because file does not exist: "+target.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to open/handle " + target.getAbsolutePath() + " for reading: " + e.getMessage());
        }

        return list;
    }
}
