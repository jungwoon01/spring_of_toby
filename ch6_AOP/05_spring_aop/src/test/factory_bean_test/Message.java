package test.factory_bean_test;

public class Message{
    String text;

    // 생성자가 private 으로 선언되어 있어서 외부에서 생성자를 통해 오브젝트를 만들 수 없다.
    // 사실 스프링은 private 생성자를 가진 클래스도 빈으로 등록해주면 리플렉션을 이용해 오브젝트를 만들어준다.
    // 리플렉션은 private 으로 선언된 접근 규약을 위반할 수 있는 기능이 있다.
    // 일반적으로 private 생성자를 가진 클래스를 빈으로 등록하는 일은 권장되지 않는다.
    private Message(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    // 생성자 대신 사용할 수 있는 스태틱 팩토리 메소드를 제공한다.
    public static Message newMessage(String text) {
        return new Message(text);
    }
}
