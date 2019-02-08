# 아이템 34 int 상수 대신 열거 타입을 사용하라
## 열거 타입?
- 일정 개수의 상수 값을 정의하고 그 외의 값을 허용하지 않는 타입
- ex) 사게절, 태양계의 행성, 카드게임의 카드 종류 등..
- **정수 열거 패턴**
    - 열거 타입을 지원하기 이전에 사용함.
    
    ```java
    public static final int APPLE_FUJI            =0;
    public static final int APPLE_PIPPIN          =1;
    public static final int APPLE_GRANNY_SMITH    =2;
  
    public static final int ORANGE_NAVEL  =0;
    public static final int ORANGE_TEMPLE =1;
    public static final int ORANGE_BLOOD  =2;
    ```
    - 단점이 많음
        - 타입 안전 보장 X
        - 표현력 안좋음
        - 오렌지를 건네야 하는 메소드에 사과를 보내고 동등 연산자로 비교 가능. 경고 메시지 X
            ```java
            // 향긋한 오렌지 향의 사과 소스
            int i = (APPLE_FUJI - ORANGE_TEMPLE) / APPLE_PIPPIN;
            ```
        - 네임 스페이스 X. 이름 충돌 방지를 위해 prefix를 붙임. ex) **ORANGE_** NAVEL, **APPLE_** FUJI
        - 상수 값이 바뀌면 다시 컴파일 해야함. 다시 컴파일 하지 않을 시 오동작
        - 정수 상수는 문자열로 출력하기 까다로움
            - 값을 출력하거나 디버거로 보면 숫자로만 보여 도움 X
            - 같은 정수 열거 그룹에 속한 모든 상수 순회 방법이 마땅치 않음. 그 안의 몇 개나 있는지 파악 불가
        - **string enum pattern** 
            - 나쁘다
            - 상수의 의미를 출력
            - 문자열 상수의 이름 대신 문자열 값을 그대로 하드코딩하게 만듦    
            - 오타로 인해 런타임 버그가 생길 수 있음
## 열거 패턴 단점의 대안. **열거 타입(enum type)**
```java
public enum Apple { FUJI, PIPPIN, GRANNY_SMITH }
public enum Orange { NAVEL, TEMPLE, BLOOD }
```            
- 클래스이다.
- 상수 하나당 자신의 인스턴스를 하나씩 만들어 public static final 필드로 공개함
- 열거타입으로 만들어진 인스턴스는 하나인 것을 보장
- 싱글턴을 일반화한 형태
- 타입 안전성 제공
    - ex) Apple 열거 타입을 매개변수로 받는다면 다른 타입을 넘기려 하면 컴파일 오류 발생 
- 열거 타입에 새로운 상수르 추가하거나 순서를 바꿔도 다시 컴파일 하지 않아도 됨
    - 상수 값이 클라이언트로 컴파일 되어 각이되지 않음
- toString 메서드는 출력하기에 적합한 문자열을 내어줌
- 임의의 메서드나 필드 추가 가능
- Object 메서드, Comparable, Serializable을 구현. 직렬화 형태도 잘 구현
    
    