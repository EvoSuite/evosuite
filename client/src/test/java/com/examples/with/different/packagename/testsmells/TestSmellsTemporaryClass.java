package com.examples.with.different.packagename.testsmells;

import java.util.HashMap;

public class TestSmellsTemporaryClass {

    private String serverName;
    private int serverID;
    private String admin;
    private HashMap<String, String> users = new HashMap<>();

    public TestSmellsTemporaryClass(String serverName, String admin, String password) {
        this.serverName = serverName;
        this.admin = admin;
        this.serverID = 12345;
        users.put(this.admin, password);
    }

    public boolean addUser (String adminName, String adminCode, String name, String code) {
        if(this.admin.equals(adminName) && users.get(adminName).equals(adminCode) && !users.containsKey(name)){
            users.put(name, code);
            return true;
        }
        return false;
    }

    public boolean removeUser (String adminName, String adminCode, String name) {
        if(this.admin.equals(adminName) && !this.admin.equals(name) &&
                users.get(adminName).equals(adminCode) && users.containsKey(name)){
            users.remove(name);
            return true;
        }
        return false;
    }

    public boolean updateServerName (String adminName, String adminCode, String serverName){
        if(this.admin.equals(adminName) && users.get(adminName).equals(adminCode) &&
                this.serverName.equals(serverName)){
            this.serverName = serverName;
            return true;
        }
        return false;
    }

    public String getServerName (){
        return this.serverName;
    }

    public int getServerID() {
        return this.serverID;
    }
}
