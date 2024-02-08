package com.examples.with.different.packagename.testsmells;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TestSmellsServer {

    private TestSmellsUser admin;
    private String serverID;
    private List<TestSmellsUser> users = new ArrayList<>();
    private HashMap<String, File> sharedFiles = new HashMap<>();
    private HashMap<String, String> fileCreator = new HashMap<>();

    public TestSmellsServer(TestSmellsUser admin, String password){
        if(admin.verifyPassword(password)){
            this.admin = admin;
            this.users.add(admin);
            this.serverID = UUID.randomUUID().toString();
        } else {
            System.out.println("Wrong password!");
        }
    }

    public TestSmellsUser getAdmin(){
        return this.admin;
    }

    public String getServerID (){
        return this.serverID;
    }

    public List<TestSmellsUser> getUsers (TestSmellsUser user){
        return users.contains(user) ? this.users : null;
    }

    public boolean addUser (TestSmellsUser user, String password, TestSmellsUser newUser){
        if(user.equals(admin) && user.verifyPassword(password) && !this.users.contains(newUser)){
            return users.add(newUser);
        }
        return false;
    }

    public boolean removeUser (TestSmellsUser user, String password, TestSmellsUser removedUser){
        if(user.equals(admin) && user.verifyPassword(password) &&
                this.users.contains(removedUser) && !user.equals(removedUser)){

            for (Map.Entry<String, String> entry : fileCreator.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if(value.equals(removedUser.getId())){
                    sharedFiles.remove(key);
                    fileCreator.remove(key);
                }
            }
            return users.remove(removedUser);
        }
        return false;
    }

    public boolean addFile (File file, TestSmellsUser user){

        if(users.contains(user) && !this.sharedFiles.containsKey(file.getName())){
            this.sharedFiles.put(file.getName(), file);
            this.fileCreator.put(file.getName(), user.getId());
            return true;
        }

        return false;
    }

    public boolean addPersonalFile (String fileName, TestSmellsUser user){

        File file;

        if(user instanceof TestSmellsPremiumUser){
            file = ((TestSmellsPremiumUser) user).getFile(fileName);
        } else {
            return false;
        }

        if(users.contains(user) && !this.sharedFiles.containsKey(file.getName())){
            this.sharedFiles.put(file.getName(), file);
            this.fileCreator.put(file.getName(), user.getId());
            return true;
        }

        return false;
    }

    public boolean removeFile (String fileName, TestSmellsUser user){
        if(users.contains(user) && this.sharedFiles.containsKey(fileName) &&
                fileCreator.get(fileName).equals(user.getUsername())){

            this.sharedFiles.remove(fileName);
            return true;
        }
        return false;
    }

    public boolean editFile (String name, String content, TestSmellsUser user){

        if(users.contains(user)){
            try {

                FileWriter myWriter = new FileWriter(name);
                myWriter.write(content);
                myWriter.close();
                return true;

            } catch (IOException e) {

                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
        return false;
    }
}
