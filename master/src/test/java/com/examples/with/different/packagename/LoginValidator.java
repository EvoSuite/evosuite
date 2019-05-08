package com.examples.with.different.packagename;

public class LoginValidator {

    public boolean tryLogin(Account acc){
        if(acc.getId()==99 && acc.getDob().equals("01.01.1990") && acc.getSecret().equals("Aston Martin")){
            // valid user
            return true;
        }else
            return false;
    }

}

class Account {
    private int id; // account id
    private String dob = "01.01.2019"; // date of birth
    private String secret = "default"; // some secret

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}


