package test.pointcut;

import org.junit.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PointcutTest {

    @Test
    public void methodSignaturePointcut() throws SecurityException, NoSuchMethodException {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        // Target 클래스 minus() 메소드 시그니처
        pointcut.setExpression("execution(public int " + "test.pointcut.Target.minus(int,int) " + "throws java.lang.RuntimeException)");

        // Target.minus()
        // 클래스 필터와 메소드 매처를 가져와 각각 비교한다.
        assertThat(
                pointcut.getClassFilter().matches(Target.class) && pointcut.getMethodMatcher().matches(Target.class.getMethod("minus", int.class, int.class), null),
                is(true)
        );


        // Target.plus()
        assertThat(
                pointcut.getClassFilter().matches(Target.class) && pointcut.getMethodMatcher().matches(Target.class.getMethod("plus", int.class, int.class), null),
                is(false)
        );

        // Bean.method()
        assertThat(
                pointcut.getClassFilter().matches(Bean.class) && pointcut.getMethodMatcher().matches(Target.class.getMethod("method"), null),
                is(false)
        );
    }

    @Test
    public void pointcut() throws Exception {
        // 모든 메소드를 다 허용하는 표현식
        targetClassPointcutMatches("execution(* *(..))", true, true, true, true, true, true);
        // 책 496쪽의 표를 참고하면 여러가지 테스트를 추가해볼 수 있다.
    }

    // 타깃 클래스의 메소드 6개에 대해 포인트컷 선정 여부를 검사하는 헬퍼 메소드
    public void targetClassPointcutMatches(String expression, boolean... expected) throws Exception {
        pointcutMatches(expression, expected[0], Target.class, "hello");
        pointcutMatches(expression, expected[1], Target.class, "hello", String.class);
        pointcutMatches(expression, expected[2], Target.class, "plus", int.class, int.class);
        pointcutMatches(expression, expected[3], Target.class, "minus", int.class, int.class);
        pointcutMatches(expression, expected[4], Target.class, "method");
        pointcutMatches(expression, expected[5], Bean.class, "method");
    }

    // 포인트컷과 메소드를 비교해주는 테스트 헬퍼 메소드
    public void pointcutMatches(String expression, Boolean expected, Class<?> clazz,
                                String methodName, Class<?>... args) throws Exception {

        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(expression);

        assertThat(
                pointcut.getClassFilter().matches(clazz) && pointcut.getMethodMatcher().matches(clazz.getMethod(methodName, args), null),
                is(expected)
        );
    }
}
