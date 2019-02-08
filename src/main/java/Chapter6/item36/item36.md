# 아이템 36 비트 필드 대신 EnumSet을 사용하라
- 열거한 값들이 집합으로 사용될 경우, 예전에는 2의 거듭제곱 값을 할당한 정수 열거 패턴을 사용해옴 (아이템 34)
```java
public class Text {
    public static final int STYLE_BOLD          = 1 << 0;   // 1
    public static final int STYLE_ITALIC        = 1 << 1;   // 2
    public static final int STYLE_UNDERLINE     = 1 << 2;   // 4
    public static final int STYLE_STRIKETHROUGH = 1 << 3;   // 8
    // 매개변수 styles는 0개 이상의 STYLE_ 상수를 비트별 OR 한 값이다
    public void applyStyles(int styles) {...}
}
  
```
- 비트필드
    - 비트별 OR를 사용해 여러 상수를 모아서 만든 하나의 집합
    - ex)`text.applyStyles(STYLE_BOLD | STYLE_ITALIC)`  
    - 장점 : 합집합과 교집합 같은 집합 연산을 효율적으로 수행
    - 단점 
        - 정수 열거 상수의 단점을 그대로 지님.
        - 정수 열거 상수 출력할 때보다 해석하기가 훨씬 어려움
        - 모든 원소를 순회하기 까다로움
        - API 작성시 적절한 타입을 선택해야함 (int, long ..) 
- EnumSet
    - `java.util.EnumSet`
    - 열거 타입 상수의 값으로 구성된 집합을 효과적으로 표현해줌
    - Set인터페이스를 완벽히 구현
    - 타입 안전
    - 다른 Set 구현제와 함께 사용 가능
    - 내부 구현은 비트 벡터
    - 대량 작업은 비트를 효율적으로 처리할 수 있는 산술 연산을 써서 구현함
        - ex) removeAll, retailAll
    - 비트를 직접 다룰 때 흔히 겪는 오류들에서 해방
        - 난해한 작업은 EnumSet이 다 처리해줌
    ```java
    public class Text {
      public enum Style { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH}
    
      // 어떤 Set을 넘겨도 되나, EnumSet이 가장 좋다.
      public void applyStyles(Set<STyle> styles) {...}
    }
    ``` 
    - 집합 생성 정적 패토리
        - text.applyStyles(**EnumSet.of(Style.BOLD, Style.ITALIC)**)
- 핵심 정리
    - 열거할 수 있는 타입을 한데 모아 집합 형태로 사용한다고 해도 비트 필드를 사용할 이유는 없음
    - EnumSet 클래스가 비트 필드 수준의 명료함과 성능을 제공하고 아이템 34에서 설명한 열거 타입의 장점까지 선사하기 때문
    - EnumSet의 유일한 단점이라면 불변 EnumSet을 만들 수 없음
    - 수정될 때 까지는 Collections.unmodifiableSet으로 EnumSet을 감싸 사용할 수 있음
    - 구아바 라이브러리의 불변 EnumSet https://bit.ly/2NlxW6O                     
         - 내부적으로는 EnumSet을 사용해 구현했으므로 성능 면에서는 손해
         - 컴포지션으로 구현함. (아이템 18)
        
    