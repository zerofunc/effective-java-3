package Chapter10.item71;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class item71 {
    public static void main(String[] args) {
//        List<Integer> collect = Arrays.asList(1, 2, 4).stream()
//                .filter(item71::checkedException)
//                .collect(Collectors.toList());
    }

    public static boolean checkedException() throws IOException {
        throw new IOException("ioException");
    }
}
