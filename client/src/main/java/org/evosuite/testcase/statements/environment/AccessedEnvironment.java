package org.evosuite.testcase.statements.environment;

import org.evosuite.runtime.vnet.EndPointInfo;

import java.io.Serializable;
import java.util.*;

/**
 * Class used to keep track of what environment components (local files, remote URLs, etc)
 * a test case has accessed to
 *
 * Created by arcuri on 12/12/14.
 */
public class AccessedEnvironment implements Serializable {

	private static final long serialVersionUID = 2653568611955383431L;

    /**
     * Paths of accessed local files
     */
	private final Set<String> localFiles;


    /**
     * URL of remote resources
     */
    private final Set<String> remoteURLs;

    /**
     *  TCP/UDP sockets opened by the SUT
     */
    private final Set<EndPointInfo> localListeningPorts;


    /**
     * Remote addr/ports the SUT has contacted (ie initialized communication)
     * via TCP/UDP
     */
    private final Set<EndPointInfo> remoteContactedPorts;


    public AccessedEnvironment(){
        localFiles = new LinkedHashSet<>();
        remoteURLs = new LinkedHashSet<>();
        localListeningPorts = new LinkedHashSet<>();
        remoteContactedPorts = new LinkedHashSet<>();
    }

    public void copyFrom(AccessedEnvironment other){
        clear();
        this.localFiles.addAll(other.localFiles);
        this.remoteURLs.addAll(other.remoteURLs);
        this.localListeningPorts.addAll(other.localListeningPorts);
        this.remoteContactedPorts.addAll(other.remoteContactedPorts);
    }

    public void clear(){
        localFiles.clear();
        remoteURLs.clear();
        localListeningPorts.clear();
        remoteContactedPorts.clear();
    }

    public void addRemoteContactedPorts(Collection<EndPointInfo> ports){
        remoteContactedPorts.addAll(ports);
    }

    public Set<EndPointInfo> getViewOfRemoteContactedPorts(){
        return Collections.unmodifiableSet(remoteContactedPorts);
    }

    public void addLocalListeningPorts(Collection<EndPointInfo> ports){
        localListeningPorts.addAll(ports);
    }

    public Set<EndPointInfo> getViewOfLocalListeningPorts(){
        return Collections.unmodifiableSet(localListeningPorts);
    }

    public void addLocalFiles(Collection<String> files){
        localFiles.addAll(files);
    }

    public Set<String> getViewOfAccessedFiles(){
        return Collections.unmodifiableSet(localFiles);
    }

    public void addRemoteURLs(Collection<String> urls){
        remoteURLs.addAll(urls);
    }

    public Set<String> getViewOfRemoteURLs(){
        return Collections.unmodifiableSet(remoteURLs);
    }
    
    public boolean isNetworkAccessed() {
    	return !remoteURLs.isEmpty() || !localListeningPorts.isEmpty() || !remoteContactedPorts.isEmpty();
    }
    
    public boolean isFileSystemAccessed() {
    	return !localFiles.isEmpty();
    }
}
