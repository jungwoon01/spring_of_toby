package sum_numbers;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CalcSumTest {
    // 메서드에서 사용할 클래스의 오브젝트와 파일 이름이 공유되어 @Before 메소드에서 미리 픽스처로 만들어둔다.
    Calculator calculator;
    String numFilepath;

    @Before
    public void setUp(){
        this.calculator = new Calculator();
        this.numFilepath = getClass().getResource("numbers.txt").getPath();
    }

    @Test
    public void sumOfNumbers() throws IOException{
        assertThat(calculator.calcSum(numFilepath), is(10)); // 1+2+3+4 = 10 으로 예상
    }

    @Test
    public void multiplyOfNumbers() throws IOException {
        assertThat(calculator.calcMultiply(this.numFilepath), is(24));
    }

    @Test
    public void concatenateStrings() throws IOException {
        assertThat(calculator.concatenate(this.numFilepath), is("1234"));
    }
}
