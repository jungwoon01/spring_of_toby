package sum_numbers;

import java.io.*;
import java.net.URLDecoder;

public class Calculator {
    // 템플릿 콜백을 적용한 calcSum() 메소드
    public Integer calcSum(String filepath) throws IOException {
        LineCallback<Integer> lineCallback = new LineCallback<>() {
            @Override
            public Integer doSomethingWithLine(String line, Integer value) {
                return value + Integer.valueOf(line);
            }
        };
        return lineReadTemplate(filepath, lineCallback, 0);
    }

    // 곱을 계산하는 콜백을 가진 calcMultiply() 메소드
    public Integer calcMultiply(String filepath) throws IOException {
        LineCallback<Integer> lineCallback = new LineCallback<>() {
            @Override
            public Integer doSomethingWithLine(String line, Integer value) {
                return value * Integer.valueOf(line);
            }
        };
        return lineReadTemplate(filepath, lineCallback, 1);
    }

    // 문자열 연결 기능 콜백을 이용해 만든 concatenate() 메소드
    public String concatenate(String filepath) throws IOException {
        LineCallback<String> lineCallback = new LineCallback<String>() {
            @Override
            public String doSomethingWithLine(String line, String value) {
                return value + line;
            }
        };
        return lineReadTemplate(filepath, lineCallback, "");
    }

    public Integer fileReadTemplate(String filepath, BufferedReaderCallback callback) throws IOException {
        String path = URLDecoder.decode(filepath, "UTF-8"); // 한글로된 파일명 때문에 오류가 생겨 UTF-8로 디코딩

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(path)); // 한 줄씩 읽기 편하게  BufferedReader 로 가져온다.
            // 콜백 오브젝트 호출. 템플릿에서 만든 컨텍스트 정보인 BufferedReader 를 전달해주고 콜백의 작업 결과를 받아둔다.
            int ret = callback.doSomethingWithReader(br);
            return ret;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (br != null) { // BufferedReader 오브젝트가 생성되기 전에 예외가 발생할 수 도 있으므로 반드시 null 체크를 먼저 해야한다.
                try { br.close(); } catch (IOException e) { System.out.println(e.getMessage()); }
            }
        }
    }

    // LineCallback 을 사용하는 템플릿
    public <T> T lineReadTemplate(String filepath, LineCallback<T> callback, T initVal) throws IOException {
        String path = URLDecoder.decode(filepath, "UTF-8"); // 한글로된 파일명 때문에 오류가 생겨 UTF-8로 디코딩

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(path)); // 한 줄씩 읽기 편하게  BufferedReader 로 가져온다.
            // 콜백 오브젝트 호출. 템플릿에서 만든 컨텍스트 정보인 BufferedReader 를 전달해주고 콜백의 작업 결과를 받아둔다.
            T res = initVal;
            String line = null;
            while ((line = br.readLine()) != null) {
                res = callback.doSomethingWithLine(line, res);
            }
            return res;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (br != null) { // BufferedReader 오브젝트가 생성되기 전에 예외가 발생할 수 도 있으므로 반드시 null 체크를 먼저 해야한다.
                try { br.close(); } catch (IOException e) { System.out.println(e.getMessage()); }
            }
        }
    }
}
