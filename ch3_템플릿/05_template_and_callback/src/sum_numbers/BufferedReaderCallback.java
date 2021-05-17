package sum_numbers;

import java.io.BufferedReader;
import java.io.IOException;

public interface BufferedReaderCallback {
    // BufferedReader 를 만들어서 콜백에게 전달해주고,
    // 콜백이 각 라인을 읽어서 알아서 처리한 후
    // 최종 결과만 템플릿에게 돌려주는 것
    Integer doSomethingWithReader(BufferedReader bufferedReader) throws IOException;
}
