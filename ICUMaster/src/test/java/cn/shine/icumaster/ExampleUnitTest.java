package cn.shine.icumaster;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        String s = "234";
        int radix=10;
        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    System.out.println("err 1");
//                    throw NumberFormatException.forInputString(s);

                if (len == 1) // Cannot have lone "+" or "-"
//                    throw NumberFormatException.forInputString(s);
                    System.out.println("err 2");

                i++;
            }
            multmin = limit / radix;
            System.out.println("multmin"+multmin);
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++),radix);
                System.out.println("digit "+digit);
                if (digit < 0) {
                    System.err.println("digit <0");
//                    throw NumberFormatException.forInputString(s);
                }
                if (result < multmin) {
//                    throw NumberFormatException.forInputString(s);
                    System.err.println("result < multmin");

                }
                result *= radix;
                System.out.println(" result *= radix "+result);
                if (result < limit + digit) {
                    System.err.println("result < limit + digit");
//                    throw NumberFormatException.forInputString(s);
                }
                result -= digit;
                System.out.println("result -= digit "+result);

            }
        } else {
//            throw NumberFormatException.forInputString(s);
            System.err.println("len<0");

        }
        System.out.println( negative ? result : -result);
    }

    @Test
    public void test() {
        List<Integer> integers = Arrays.asList(1, 3, 4);
        List<? extends Number> list =integers;
//        list.add(4.0);//编译错误
//        list.add(3);//编译错误
        list.add(null);
//        System.out.println(list.toString());
    }
}