package org.evosuite.testcase.environmentdata;

import java.util.*;

/**
 * Class used to keep track of what environment components (local files, remote URLs, etc)
 * a test case has accessed to
 *
 * Created by arcuri on 12/12/14.
 */
public class AccessedEnvironment {

    private final Set<String> accessedFiles;

    public AccessedEnvironment(){
        accessedFiles = new LinkedHashSet<>();
    }

    public void copyFrom(AccessedEnvironment other){
        clear();
        this.accessedFiles.addAll(other.accessedFiles);
    }

    public void clear(){
        accessedFiles.clear();
    }

    public void addLocalFiles(Collection<String> files){
        accessedFiles.addAll(files);
    }

    public Set<String> getViewOfAccessedFiles(){
        return Collections.unmodifiableSet(accessedFiles);
    }
}
