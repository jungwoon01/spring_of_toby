package test.dynamic_proxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;

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

    // 스프링 ProxyFactoryBean 을 이용한 다이나믹 프록시 테스트
    @Test
    public void proxyFactoryBean() {
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget()); // 타깃 설정
        // 부가기능을 담은 어드바이스를 추가한다 여러 개를 추가할 수도 있다,
        pfBean.addAdvice(new UppercaseAdvice());

        // FactoryBean 이므로 getObject() 로 생성된 프록시를 가져온다.
        Hello proxiedHello = (Hello) pfBean.getObject();

        assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
        assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
        assertThat(proxiedHello.sayThankYou("Toby"), is("THANK YOU TOBY"));
    }

    // 포인트컷까지 적용한 ProxyFactoryBean
    @Test
    public void pointcutAdvisor() {
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());

        // 메소드 이름을 비교해서 대상을 선정하는 알고리즘을 제공하는 포인트컷 생성
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedName("sayH*"); // 이름 비교조건 설정. sayH로 시작하는 모든 메소드들 선택하게 한다.

        // 포인트컷과 어드바이스를 Advisor 로 묶어서 한 번에 추가
        pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));

        Hello proxiedHello = (Hello) pfBean.getObject();

        assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
        assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
        // 메소드 이름이 포인트컷의 선정조건에 맞지 않으므로, 부가기능(대문자변환)이 적용되지 않는다.
        assertThat(proxiedHello.sayThankYou("Toby"), is("Thank You Toby"));
    }

    // Uppercase 어드바이스
    static class UppercaseAdvice implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable {
            // 리플렉션의 Method 와 달리 메소드 실행 시 타깃 오브젝트를 전달할 필요가 없다.
            // MethodInvocation 은 메소드 정보와 함께 타깃 오브젝트를 알고 있기 때문
            String ret = (String)methodInvocation.proceed();
            return ret.toUpperCase(); // 부가기능
        }
    }
}
