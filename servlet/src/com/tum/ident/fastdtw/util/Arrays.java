/*
 * Arrays.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.tum.ident.fastdtw.util;

import java.util.ArrayList;
import java.util.Collection;


/**
 * This class...
 *
 * @author Stan Salvador, stansalvador@hotmail.com
 * @version last changed: 06/01/2004
 * @see
 * @since 06/01/2004
 */
public class Arrays
{
   public static int[] toPrimitiveArray(Integer[] objArr)
   {
      final int[] primArr = new int[objArr.length];
      for (int x=0; x<objArr.length; x++)
         primArr[x] = objArr[x].intValue();

      return primArr;
   }


   public static int[] toIntArray(Collection<?> c)
   {
      return Arrays.toPrimitiveArray((Integer[])c.toArray(new Integer[0]));
   }


   public static Collection<Boolean> toCollection(boolean arr[])
   {
      final ArrayList<Boolean> collection = new ArrayList<Boolean>(arr.length);
      for (int x=0; x<arr.length; x++)
         collection.add(new Boolean(arr[x]));

      return collection;
   }


   public static Collection<Byte> toCollection(byte arr[])
   {
      final ArrayList<Byte> collection = new ArrayList<Byte>(arr.length);
      for (int x=0; x<arr.length; x++)
         collection.add(new Byte(arr[x]));

      return collection;
   }


   public static Collection<Character> toCollection(char arr[])
   {
      final ArrayList<Character> collection = new ArrayList<Character>(arr.length);
      for (int x=0; x<arr.length; x++)
         collection.add(new Character(arr[x]));

      return collection;
   }


   public static Collection<Double> toCollection(double arr[])
   {
      final ArrayList<Double> collection = new ArrayList<Double>(arr.length);
      for (int x=0; x<arr.length; x++)
         collection.add(new Double(arr[x]));

      return collection;
   }


   public static Collection<Float> toCollection(float arr[])
   {
      final ArrayList<Float> collection = new ArrayList<Float>(arr.length);
      for (int x=0; x<arr.length; x++)
         collection.add(new Float(arr[x]));

      return collection;
   }


   public static Collection<Integer> toCollection(int arr[])
   {
      final ArrayList<Integer> collection = new ArrayList<Integer>(arr.length);
      for (int x=0; x<arr.length; x++)
         collection.add(new Integer(arr[x]));

      return collection;
   }


   public static Collection<Long> toCollection(long arr[])
   {
      final ArrayList<Long> collection = new ArrayList<Long>(arr.length);
      for (int x=0; x<arr.length; x++)
         collection.add(new Long(arr[x]));

      return collection;
   }


   public static Collection<Short> toCollection(short arr[])
   {
      final ArrayList<Short> collection = new ArrayList<Short>(arr.length);
      for (int x=0; x<arr.length; x++)
         collection.add(new Short(arr[x]));

      return collection;
   }


   public static Collection<String> toCollection(String arr[])
   {
      final ArrayList<String> collection = new ArrayList<String>(arr.length);
      for (int x=0; x<arr.length; x++)
         collection.add(new String(arr[x]));

      return collection;
   }

   public static boolean contains(boolean arr[], boolean val)
   {
      for (int x=0; x<arr.length; x++)
         if (arr[x] == val)
            return true;

      return false;
   }


   public static boolean contains(byte arr[], byte val)
   {
      for (int x=0; x<arr.length; x++)
         if (arr[x] == val)
            return true;

      return false;
   }


   public static boolean contains(char arr[], char val)
   {
      for (int x=0; x<arr.length; x++)
         if (arr[x] == val)
            return true;

      return false;
   }


   public static boolean contains(double arr[], double val)
   {
      for (int x=0; x<arr.length; x++)
         if (arr[x] == val)
            return true;

      return false;
   }


   public static boolean contains(float arr[], float val)
   {
      for (int x=0; x<arr.length; x++)
         if (arr[x] == val)
            return true;

      return false;
   }


   public static boolean contains(int arr[], int val)
   {
      for (int x=0; x<arr.length; x++)
         if (arr[x] == val)
            return true;

      return false;
   }


   public static boolean contains(long arr[], long val)
   {
      for (int x=0; x<arr.length; x++)
         if (arr[x] == val)
            return true;

      return false;
   }


   public static boolean contains(short arr[], short val)
   {
      for (int x=0; x<arr.length; x++)
         if (arr[x] == val)
            return true;

      return false;
   }


   public static boolean contains(String arr[], String val)
   {
      for (int x=0; x<arr.length; x++)
         if (arr[x] == val)
            return true;

      return false;
   }



}  // end class Arrays
