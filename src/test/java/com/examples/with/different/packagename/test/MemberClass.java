package com.examples.with.different.packagename.test;

public class MemberClass {
  public static class Member1 {
    private int x = 0;
    public int getX() {
      return x;
    }
    public void setX(int x) {
      this.x = x;
    }
    public void rubbish() {
    }
  }
  
  public class Member2 {
    public int x = 0;
  }
/*
  public Member2 getMember(int x) {
    Member2 mem = new Member2();
    mem.x = x;
    return mem;
  }
 */ 
  public void target(Member1 member1, Member2 member2) {
    if(member1.getX() == member2.x && member1.getX() > 0)
      System.out.println("Hooray");
  }
}