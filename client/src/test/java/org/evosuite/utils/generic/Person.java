package org.evosuite.utils.generic;
/**
 * Simple test class of simple setters and getters.
 * Used for testing variable name generation
 */
public class Person {
    String name;
    Long id;
    int age;
    Person () {
    }
    Person (String name, Long id) {
        this.name = name;
        this.id = id;
    }
    public void setId(long id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setAge(int actualAge) {this.age = actualAge;}
    public String getName() { return name; }
    public Long getId() { return id; }
    public int getAge() { return age; }

    public boolean isAdult() { return age>=18; }
    public int getFixedId() {return 18;}
}

