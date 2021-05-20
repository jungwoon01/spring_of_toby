package domain;

public enum Level {
    // enum 선언에 DB에 저장할 값과 함께 다음 단계의 레벨 정보도 추가한다.
    GOLD(3, null), SILVER(2, GOLD), BASIC(1, SILVER); // 세 개의 이늄 오브젝트 정의

    private final int value;
    private final Level next; //  다음 단계의 레벨 정보를 스스로 갖고 있도록 Level 타입의 next 변수를 추가한다.

    Level(int value, Level next) { // DB 에 저장할 값을 넣어줄 생성자를 만들어둔다.
        this.value = value;
        this.next = next;
    }

    public int intValue(){
        return value;
    }

    public Level nextLevel() {
        return this.next;
    }

    public static Level valueOf(int value) { // 값으로 부터 Level 타입 오브젝트를 가져오도록 만든 스테틱 메소드
        switch (value) {
            case 1: return BASIC;
            case 2: return SILVER;
            case 3: return GOLD;
            default: throw new AssertionError("Unknown value: " + value);
        }
    }
}
