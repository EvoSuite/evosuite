package com.examples.with.different.packagename.defuse;

public class DefUseExample2 {


//       public int testMeFirst(int y){
//           return testMe(y);
//       }


        public int testMe(int x) {
            int y = 0;

            if(x == 27)
                y = y + 3;

            return y;
        }


}
