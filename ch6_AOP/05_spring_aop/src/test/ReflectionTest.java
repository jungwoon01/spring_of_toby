package test;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ReflectionTest {
    @Test
    public void invokeMethod() throws Exception {
        String name = "Spring";

        // length()
        assertThat(name.length(), is(6)); // length() 메소드를 직접 호출하는 방법

        Method lengthMethod = String.class.getMethod("length"); // 리플렉션 방식으로 length() 메소드 호출 방식
        assertThat((Integer) lengthMethod.invoke(name), is(6));

        // charAt()
        assertThat(name.charAt(0), is('S'));

        Method charAtMethod = String.class.getMethod("charAt", int.class);
        assertThat((Character)charAtMethod.invoke(name, 0), is('S'));
    }
}