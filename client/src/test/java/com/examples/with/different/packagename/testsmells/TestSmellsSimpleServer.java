package com.examples.with.different.packagename.testsmells;

import java.util.ArrayList;
import java.util.List;

public class TestSmellsSimpleServer {

    private List<TestSmellsSimpleUser> users = new ArrayList<>();
    private TestSmellsSimpleUser admin;

    public TestSmellsSimpleServer(TestSmellsSimpleUser admin){
        this.admin = admin;
        users.add(admin);
    }

    public TestSmellsSimpleUser getAdmin () {
        return this.admin;
    }

    public List<TestSmellsSimpleUser> getUsers () {
        return this.users;
    }

    public boolean addUser (TestSmellsSimpleUser user) {
        return this.users.add(user);
    }

    public void addMultipleUsers (TestSmellsSimpleUser[] arrayOfUsers) {
        for(TestSmellsSimpleUser user : arrayOfUsers){
            this.users.add(user);
        }
    }
}
