package test.factory_bean_test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test/factory_bean_test/messageContext.xml")
public class FactoryBeanTest {
    // 학습 테스트를 작성하고 있는 지금은 message 빈의 타입이 무엇인지 확실하지 않으므로
    // ApplicationContext 를 이용해 getBean() 메소드를 사용하게 했다.
    // getBean() 메소드는 빈의 타입을 지정하지 않으면 Object 타입으로 리턴한다.
    @Autowired
    ApplicationContext context;

    @Autowired
    Object message;

    @Test
    public void getMessageFromFactoryBean() {
        //message = context.getBean("message"); // ("&message") 를 해주면 팩토리 빈 자체를 돌려준다.
        assertThat(message, is(Message.class)); // 타입 확인
        assertThat(((Message)message).getText(), is("Factory Bean")); // 설정과 기능 확인
    }
}
