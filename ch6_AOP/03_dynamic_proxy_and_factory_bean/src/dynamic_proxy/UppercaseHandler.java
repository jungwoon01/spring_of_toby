package dynamic_proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class UppercaseHandler implements InvocationHandler {
    Object target; // 어떤 종류의 인터페이스를 구현한 타깃에도 적용 간으하도록 Object 타입으로 수정

    public UppercaseHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = method.invoke(target, args);
        // 리턴 타입과 메소드 이름이 일치하는 경우에만 부가기능을 적용한다.
        if (ret instanceof String && method.getName().startsWith("say")) {
            return ((String)ret).toUpperCase();
        }
        else {
            return ret; // 조건이 일치하지 않으면 타깃 오브젝트의 호출 결과를 그대로 리턴한다.
        }
    }

//    @Override
//    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        // 호출한 메소드의 리턴 타입이 String 인 경우에만 대문자 변경 기능을 적용하도록 수정
//        Object ret = method.invoke(target, args);
//        if (ret instanceof String) {
//            return ((String)ret).toUpperCase();
//        }
//        else {
//            return ret;
//        }
//    }
}
