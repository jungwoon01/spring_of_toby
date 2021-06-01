package test.dynamic_proxy;

import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProxyTest {
    @Test
    public void simpleProxy() {
        Hello proxiedHello = (Hello) Proxy.newProxyInstance(
                // 동적으로 생성되는 다이나믹 프록시 클래스의 로딩에 사용할 클래스 로더.
                getClass().getClassLoader(),
                // 구현할 인터페이스
                // 다이나믹 프록시는 한 번에 하나 이상의 인터페이스를 구현할 수도 있다.
                // 따라서 인터페이스의 배열을 사용한다.
                new Class[] {Hello.class},
                new UppercaseHandler(new HelloTarget()) // 부가기능과 위임 코드를 담은 InvocationHandler
        );
        assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
        assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
        assertThat(proxiedHello.sayThankYou("Toby"), is("THANK YOU TOBY"));
    }
}
