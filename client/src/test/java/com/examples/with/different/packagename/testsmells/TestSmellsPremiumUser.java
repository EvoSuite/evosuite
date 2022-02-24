package com.examples.with.different.packagename.testsmells;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

public class TestSmellsPremiumUser extends TestSmellsUser {

    private HashMap<String, File> personalFiles = new HashMap<>();

    public TestSmellsPremiumUser(String username, String password) {
        super(username, password);
    }

    @Override
    public boolean addFriend (String password, TestSmellsUser user){
        if(verifyPassword(password) && !friends.contains(user) && !user.equals(this)){
            return this.friends.add(user);
        }
        return false;
    }

    public Collection<File> getPersonalFiles (String password){
        if(verifyPassword(password)){
            return this.personalFiles.values();
        }
        return null;
    }

    public File removeFile (String password, String fileName){
        if(verifyPassword(password)){
            return personalFiles.remove(fileName);
        }
        return null;
    }

    public File getFile (String fileName){
        return personalFiles.get(fileName);
    }

    public boolean addFile (String password, File newFile){
        if(verifyPassword(password) && !personalFiles.containsKey(newFile.getName())){
            personalFiles.put(newFile.getName(), newFile);
            return true;
        }
        return false;
    }
}
