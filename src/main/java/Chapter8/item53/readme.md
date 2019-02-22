# 아이템 53 가변인수는 신중히 사용하라
- 가변 인수 메서드는 명시한 타입의 인수를 0개 이상 받을 수 있다
- 가변 인수 메서드 호출 시 먼저 인수의 개수와 길이가 같은 배열을 만들고 인수들을 이 배열에 저장하여 가변인수 메서드에 건내줌
- 인수를 0개만 넣어도 가변인수 메서드를 실행할 수 있고 런타임에 에러가 발생한다
    - 코드 53-2 인수가 1개이상이어야 하는 가변인수 메서드 - 잘못 구현한 예!
    ```java
    private static int min(int... args) {
        if(args.length == 0) {
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
    ```
- 매개변수를 2개 받도록 해 1번 째는 평범한 매개변수를 받고, 가변인수는 두 번째로 받으면 53-2의 문제를 해결 가능함
- 가변인수는 **인수 개수가 정해지지 않았을 때** 아주 유용하다
- 성능에 민감한 상황이라면 가변인수가 걸림돌이 될 수 있다
    - 호출 될 때마다 배열을 새로 하나 할당하고 초기화함
    - 성능을 최적화하는 해결방법
        - 인수가 0개인 것부터 4개인 것까지, 총 5개를 다중 정의하자.
        ```java
        public void foo() {}
        public void foo(int a1) {}
        public void foo(int a1, int a2) {}
        public void foo(int a1, int a2, int a3) {}
        public void foo(int a1, int a2, int a3, int... rest) {}
        ```
        - ex) EnumSet의 정적 팩토리, EnumSet의 정적 팩토리
        
- 핵심 정리
    - 인수 개수가 일정하지 않은 메서드를 정의해야 한다면 가변인수가 반드시 필요하다
    - 메서드를 정의할 때 필수 매개변수는 가변인수 앞에 두고, 가변인수를 사용할 때는 성능 문제까지 고려하자.
                