package sum_numbers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;

public class Calculator {
    public Integer calcSum(String filepath) throws IOException {
        String path = URLDecoder.decode(filepath, "UTF-8"); // 한글로된 파일명 때문에 오류가 생겨 UTF-8로 디코딩

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(path)); // 한 줄씩 읽기 편하게  BufferedReader 로 가져온다.
            Integer sum = 0;
            String line = null;
            while ((line = br.readLine()) != null) { // 마지막 라인까지 한 줄씩 읽어가면서 숫자를 더한다.
                sum += Integer.valueOf(line);
            }
            return sum;
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
