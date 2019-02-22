package Chapter8.item53;

public class Item53 {
    private static int min(int... args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("인수가 1개 이상 필요합니다.");
        }
        int min = args[0];
        for (int i = 0; i < args.length; i++) {
            if (args[i] < min) {
                min = args[i];
            }
        }
        return min;
    }

    public static void main(String[] args) {
        System.out.println(min(1, 2, 3));
        System.out.println(min());
    }
}
