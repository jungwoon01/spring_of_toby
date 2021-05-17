package sum_numbers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;

public class Calculator {
    // 템플릿 콜백을 적용한 calcSum() 메소드
    public Integer calcSum(String filepath) throws IOException {
         return fileReadTemplate(filepath, new BufferedReaderCallback() {
             @Override
             public Integer doSomethingWithReader(BufferedReader br) throws IOException {
                 Integer sum = 0;
                 String line = null;
                 while ((line = br.readLine()) !=null) {
                     sum += Integer.valueOf(line);
                 }
                 return sum;
             }
         });
    }

    // 곱을 계산하는 콜백을 가진 calcMultiply() 메소드
    public Integer calcMultiply(String filepath) throws IOException {
        return fileReadTemplate(filepath, new BufferedReaderCallback() {
            @Override
            public Integer doSomethingWithReader(BufferedReader br) throws IOException {
                Integer multiply = 1;
                String line = null;
                while ((line = br.readLine()) != null) {
                    multiply *= Integer.valueOf(line);
                }
                return multiply;
            }
        });
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
}
