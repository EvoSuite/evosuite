package com.examples.with.different.packagename.testsmells;

public class TestSmellsNormalUser extends TestSmellsUser {

    final int MAX_FRIENDS = 5;

    public TestSmellsNormalUser(String username, String password) {
        super(username, password);
    }

    @Override
    public boolean addFriend (String password, TestSmellsUser user){
        if(verifyPassword(password) && !friends.contains(user) &&
                !user.equals(this) && this.friends.size() < MAX_FRIENDS){
            return this.friends.add(user);
        }
        return false;
    }
}
