package sum_numbers;

public interface LineCallback<T> {
    T doSomethingWithLine(String line, T value);
}
