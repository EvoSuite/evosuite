/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package com.examples.with.different.packagename.dse.invokedynamic.dsc.instrument;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * Warning: Some of the X numbers in lambda$X are not correct!
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public class BasicLambda {
	
	interface CheckParam 
	{
		boolean test(int p);
	}
	
	interface CheckCapture 
	{
		boolean test();
	}

	interface CheckNested 
	{
		CheckCapture test();
	}
	
	
	static boolean foo(boolean b) {
		return b;
	}
		
	
	static boolean foo(int x) {
		return x==42;
	}
		
	static boolean foo(int x, CheckParam tester) {
		return tester.test(x);
	}
	
	static boolean foo(CheckCapture tester) {
		return tester.test();
	}	
	
	static boolean foo(CheckNested tester) {
		return tester.test().test();
	}		
	
	
	public static int _____classicBindLate(int x)
	{
		if (foo(x)) 
			return 1;
		else
			return 0;
	}

	
	/*
	class forroops/instrument/BasicLambda$1 implements forroops/instrument/BasicLambda$CheckCapture  {
	
	OUTERCLASS forroops/instrument/BasicLambda anonymousClassBindEarly (I)V
	INNERCLASS forroops/instrument/BasicLambda$1  
	static abstract INNERCLASS forroops/instrument/BasicLambda$CheckCapture forroops/instrument/BasicLambda CheckCapture
	
	private final synthetic I val$x
	
	<init>(I)V
	{
	  ALOAD 0
	  ILOAD 1
	  PUTFIELD forroops/instrument/BasicLambda$1.val$x : I
	  ALOAD 0
	  INVOKESPECIAL java/lang/Object.<init> ()V
	  RETURN
	}
	 */
	
	
	/**
	 * No "magic" -- straightforward inner class
	 */
	public static int namedClassBindLate(int x)
	{
		class NL implements CheckParam
		{
			@Override
			public boolean test(int p) {
				return p == 42;
			}
		}
		
		if (foo(x, new NL()))
			return 1;
		else
			return 0;
	}


	/**
	 * No "magic" -- straightforward inner class
	 */
	public static int anonymousClassBindLate(int x)
	{
		if (foo(
				x,
				new CheckParam() {
					@Override
					public boolean test(int p) {
						return p == 42;
					}
				}
				))
			return 1;
		else
			return 0;

	}
	/*
	class forroops/instrument/BasicLambda$1 implements forroops/instrument/BasicLambda$CheckParam  {
 
  OUTERCLASS forroops/instrument/BasicLambda anonymousClassBindLate (I)V
  INNERCLASS forroops/instrument/BasicLambda$1  
  static abstract INNERCLASS forroops/instrument/BasicLambda$CheckParam forroops/instrument/BasicLambda CheckParam

  <init>()V
  {
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
  }
	 */

	public static int lambdaBindLate(int x)
	{
		/*
    ILOAD 0
    INVOKEDYNAMIC test()Lforroops/instrument/BasicLambda$CheckParam; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      (I)Z, 
      forroops/instrument/BasicLambda.lambda$0(I)Z, 
      (I)Z
    ]
    INVOKESTATIC forroops/instrument/BasicLambda.foo (ILforroops/instrument/BasicLambda$CheckParam;)Z
		 */
		if (foo(x, p -> p==42))
			return 1;
		else
			return 0;
	}	
	/*
  private static synthetic lambda$0(I)Z
  {
    ILOAD 0
    BIPUSH 42
    IF_ICMPNE L1
    ICONST_1
    GOTO L2
   L1
    ICONST_0
   L2
    IRETURN
  }
	 */	
	
	
	/**
	 * invokedynamic returns a non-null instance of type CheckParam
	 */
	static CheckParam getLambda()
	{
		/*
    INVOKEDYNAMIC test()Lforroops/instrument/BasicLambda$CheckParam; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      (I)Z, 
      forroops/instrument/BasicLambda.lambda$0(I)Z, 
      (I)Z
    ]
    ARETURN
		 */
		// CheckParam res = (int p) -> p==42;
		CheckParam res = (p -> p==42);
		return res; 
	}		
	
	

	public static int applyLambda(int x)
	{
		CheckParam lambda1 = getLambda();	// reference to an instance of "subtype of CheckParam"
		CheckParam lambda2 = getLambda();
		
		if (lambda1 == lambda2)
			System.out.println("Two lambdas are the SAME reference");		// <-- they are the same
		else
			System.out.println("Two lambdas are DIFFERENT references");
		
		boolean res = lambda1.test(x);
		
		if (res)
			return 1;
		else
			return 0;
	}		
	
	
	
	
	/*
	private static synthetic lambda$2(I)Z
	  ILOAD 0
	  BIPUSH 42
	  IF_ICMPNE L1
	  ICONST_1
	  GOTO L2
	 L1
	  ICONST_0
	 L2
	  IRETURN
	 */
	
	

	public static int _____classicBindEarly(int x)
	{
		boolean res = x==42;
		if (foo(res))
			return 1;
		else
			return 0;
	}



	public static int namedClassBindEarlyExplicit(int x)
	{
		class CC implements CheckCapture
		{
			private final int f;
			CC(int p) {
				f = p;
			}
			@Override
			public boolean test() {
				return f == 42;
			}
		}
		
		if (foo(new CC(x)))
			return 1;
		else
			return 0;
	}
	
	
	/**
	 * javac expands this example to the above explicit version
	 * {@link #namedClassBindEarlyExplicit(int)}
	 */

	public static int namedClassBindEarlyImplicit(int x)
	{
		class CCD implements CheckCapture 
		{
			/*
		  private final synthetic I val$x
		
		  <init>(I)V		// <-- Constructor "magically" takes an int param
		  {
		    ALOAD 0
		    ILOAD 1
		    PUTFIELD forroops/instrument/BasicLambda$1CCD.val$x : I
		    ALOAD 0
		    INVOKESPECIAL java/lang/Object.<init> ()V
		    RETURN
		   }
			 */
			@Override
			public boolean test() {
				return x == 42;
			}
		}
		
		if (foo(new CCD()))	// <-- Constructor does not take an int param
			return 1;
		else
			return 0;
	}
	
	
	/**
	 * javac expands this example to the below explicit version
	 * {@link #namedClassBindEarlyExplicit(int)}
	 */

	public static int anonymousClassBindEarly(int x)
	{
		if (foo(
				new CheckCapture() {
					@Override
					public boolean test() {
						return x == 42;
					}
				}
				))
			return 1;
		else
			return 0;
	}
	/*
	class forroops/instrument/BasicLambda$1 implements forroops/instrument/BasicLambda$CheckCapture  {

  OUTERCLASS forroops/instrument/BasicLambda anonymousClassBindEarly (I)V
  INNERCLASS forroops/instrument/BasicLambda$1  
  static abstract INNERCLASS forroops/instrument/BasicLambda$CheckCapture forroops/instrument/BasicLambda CheckCapture

  private final synthetic I val$x

  <init>(I)V
  {
    ALOAD 0
    ILOAD 1
    PUTFIELD forroops/instrument/BasicLambda$1.val$x : I
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
  }
	 */
	
	

	
	
	/**
	 * TODO: Why does javac emit an invokedynamic here?
	 */

	public static int lambdaBindEarlyIgnoreParam(int x)
	{		
		/*
    ILOAD 0
    ILOAD 0
    INVOKEDYNAMIC test(I)Lforroops/instrument/BasicLambda$CheckParam; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      (I)Z, 
      forroops/instrument/BasicLambda.lambda$1(II)Z, 
      (I)Z
    ]
    INVOKESTATIC forroops/instrument/BasicLambda.foo (ILforroops/instrument/BasicLambda$CheckParam;)Z    
		 */
		//if (foo(x, (int p) -> x==42))
		if (foo(x, p -> x==42))
			return 1;
		else
			return 0;
	}
	/*
  private static synthetic lambda$1(II)Z
  {
    ILOAD 0
    BIPUSH 42
    IF_ICMPNE L1
    ICONST_1
    GOTO L2
   L1
    ICONST_0
   L2
    IRETURN
	 */
	
	
	/**
	 * TODO: Why does javac emit an invokedynamic here?
	 */

	public static int lambdaBindEarly(int x)
	{
		/*
		ILOAD 0
    INVOKEDYNAMIC test(I)Lforroops/instrument/BasicLambda$CheckCapture; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      ()Z, 
      forroops/instrument/BasicLambda.lambda$2(I)Z, 
      ()Z
    ]
    INVOKESTATIC forroops/instrument/BasicLambda.foo (Lforroops/instrument/BasicLambda$CheckCapture;)Z
		 */
		if (foo(() -> x==42))
			return 1;
		else
			return 0;
	}
	/*
  private static synthetic lambda$2(I)Z
    ILOAD 0
    BIPUSH 42
    IF_ICMPNE L1
    ICONST_1
    GOTO L2
   L1
    ICONST_0
   L2
    IRETURN
	 */

	
	

	public static int lambdaBindEarlyExplicit(int x)
	{
		/*
		ILOAD 0
    INVOKEDYNAMIC test(I)Lforroops/instrument/BasicLambda$CheckCapture; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      ()Z, 
      forroops/instrument/BasicLambda.lambda$4(I)Z, 
      ()Z
    ]
		 */
		CheckCapture cc = () -> x==42; 
		
		if (foo(cc))
			return 1;
		else
			return 0;
	}
	/*
  private static synthetic lambda$4(I)Z
    ILOAD 0
    BIPUSH 42
    IF_ICMPNE L1
    ICONST_1
    GOTO L2
   L1
    ICONST_0
   L2
    IRETURN
	 */
	
	

	/**
	 * TODO: Why does javac not use the same lambda as for following?
	 * {@link #lambdaBindEarly(int)}
	 */

	public static int lambdaBindAsStmt(int x)
	{
		/*
    ILOAD 0
    INVOKEDYNAMIC test(I)Lforroops/instrument/BasicLambda$CheckCapture; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      ()Z, 
      forroops/instrument/BasicLambda.lambda$4(I)Z, 
      ()Z
    ]
    INVOKESTATIC forroops/instrument/BasicLambda.foo (Lforroops/instrument/BasicLambda$CheckCapture;)Z
		 */
		if (foo(() -> {return x==42;}))
			return 1;
		else
			return 0;
	}
	/*
  private static synthetic lambda$4(I)Z
    ILOAD 0
    BIPUSH 42
    IF_ICMPNE L1
    ICONST_1
    IRETURN
   L1
    ICONST_0
    IRETURN
	 */

	
	//
	public static int lambdaCallsMeth(int x, CheckParam cp)
	{
		/*
    ALOAD 1
    ILOAD 0
    INVOKEDYNAMIC test(Lforroops/instrument/BasicLambda$CheckParam;I)Lforroops/instrument/BasicLambda$CheckCapture; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      ()Z, 
      forroops/instrument/BasicLambda.lambda$5(Lforroops/instrument/BasicLambda$CheckParam;I)Z, 
      ()Z
    ]
    INVOKESTATIC forroops/instrument/BasicLambda.foo (Lforroops/instrument/BasicLambda$CheckCapture;)Z
		 */
		if (foo(() -> cp.test(x)))
			return 1;
		else
			return 0;
	}
	/*
 	private static synthetic lambda$5(Lforroops/instrument/BasicLambda$CheckParam;I)Z
    ALOAD 0
    ILOAD 1
    INVOKEINTERFACE forroops/instrument/BasicLambda$CheckParam.test (I)Z
    IRETURN
	 */
	


	public static int lambdaNested(int x)
	{
		/*
		ILOAD 0
    INVOKEDYNAMIC test(I)Lforroops/instrument/BasicLambda$CheckNested; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      ()Lforroops/instrument/BasicLambda$CheckCapture;, 
      forroops/instrument/BasicLambda.lambda$6(I)Lforroops/instrument/BasicLambda$CheckCapture;, 
      ()Lforroops/instrument/BasicLambda$CheckCapture;
    ]
    INVOKESTATIC forroops/instrument/BasicLambda.foo (Lforroops/instrument/BasicLambda$CheckNested;)Z
		 */
		if (foo(() -> (() -> x==42)))
			return 1;
		else
			return 0;
	}
	/*
  private static synthetic lambda$6(I)Lforroops/instrument/BasicLambda$CheckCapture;
    ILOAD 0
    INVOKEDYNAMIC test(I)Lforroops/instrument/BasicLambda$CheckCapture; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      ()Z, 
      forroops/instrument/BasicLambda.lambda$7(I)Z, 
      ()Z
    ]
    ARETURN

  private static synthetic lambda$7(I)Z
    ILOAD 0
    BIPUSH 42
    IF_ICMPNE L1
    ICONST_1
    GOTO L2
   L1
    ICONST_0
   L2
    IRETURN
	 */
	
	
	//
	public static int lambdaCallsMeth(int x, CheckParam[] cp)
	{
		if (cp.length > 3)
			return -1;
		
		/*
    ALOAD 2
    ILOAD 0
    INVOKEDYNAMIC test(Lforroops/instrument/BasicLambda$CheckParam;I)Lforroops/instrument/BasicLambda$CheckCapture; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      ()Z, 
      forroops/instrument/BasicLambda.lambda$8(Lforroops/instrument/BasicLambda$CheckParam;I)Z, 
      ()Z
    ]
		 */		
		for (CheckParam c: cp)
			if (foo(() -> c.test(x)))
				return 1;
			else
				return 0;
		return 2;
	}
	/*
  private static synthetic lambda$8(Lforroops/instrument/BasicLambda$CheckParam;I)Z
    ALOAD 0
    ILOAD 1
    INVOKEINTERFACE forroops/instrument/BasicLambda$CheckParam.test (I)Z
    IRETURN
	 */

	
	/**
	 * TODO: The "quasi-final" property seems only to be enforced directly,
	 * e.g.: adding "x+=1" will cause a compile error.
	 * But for "int[] ar", we can change individual array components :-)
	 */
	//
	public static int quasiFinal(int[] ar)
	{
		if (ar.length > 3)
			return -1;
		
		int i = 0;
		for (int x: ar)
		{
			if (i<ar.length-1)
				ar[i+1] += ar[i];
			i += 1;
			if (foo(() -> x==42))
				return 1;
			else
				return 0;
		}
		return 2;
	}	
	

	/**
	 * static lambda parameters:
	 * - first captured ones (supplied via fields)
	 * - then formal ones
	 */
	public static int compareLambdas(int[] ar, boolean b)
	{
		if (ar.length > 3)
			return -1;
		
		for (int x: ar)
		{
			CheckParam cc = (p) -> p==42 && b;
			/* cc is a reference to an object with field: {private int arg$1 = ar[i];} */
			boolean res = cc.test(x);
			System.out.println(cc);
		}

		/*
			forroops.instrument.BasicLambda$$Lambda$1/834600351@1218025c
			forroops.instrument.BasicLambda$$Lambda$1/834600351@816f27d
			forroops.instrument.BasicLambda$$Lambda$1/834600351@87aac27
		 */
		return 1;
	}
	
	
	public static void main(String[] args)
	{
		compareLambdas(new int[]{1,1,2}, false);
	}
}
