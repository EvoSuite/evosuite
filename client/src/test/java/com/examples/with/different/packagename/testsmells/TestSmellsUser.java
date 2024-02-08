package com.examples.with.different.packagename.testsmells;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class TestSmellsUser {

    private String username;
    private String password;
    private final String id;
    protected List<TestSmellsUser> friends = new ArrayList<>();

    public TestSmellsUser (String username, String password){
        this.username = username;
        this.password = password;
        this.id = UUID.randomUUID().toString();
    }

    public String getUsername() {
        return this.username;
    }

    public boolean changeUsername(String password, String username) {
        if(verifyPassword(password)){
            this.username = username;
            return true;
        }
        return false;
    }

    public boolean verifyPassword (String password){
        return this.password.equals(password);
    }

    public boolean changePassword(String originalPassword, String newPassword) {
        if(verifyPassword(originalPassword)){
            this.password = newPassword;
            return true;
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public List<TestSmellsUser> getFriends (String password){
        return verifyPassword(password) ? this.friends : null;
    }

    public abstract boolean addFriend (String password, TestSmellsUser user);

    public boolean removeFriend (String password, TestSmellsUser user){
        if(verifyPassword(password)){
            return this.friends.remove(user);
        }
        return false;
    }

    public static boolean goodPassword (String password){
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.{8,})");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestSmellsUser that = (TestSmellsUser) o;
        return username.equals(that.username) && password.equals(that.password) && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, id);
    }

    @Override
    public String toString() {
        return "TestSmellsUser{" +
                "username='" + username + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
