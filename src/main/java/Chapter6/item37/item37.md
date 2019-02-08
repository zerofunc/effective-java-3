# 아이템 37 ordinal 인덱싱 대신 EnumMap을 사용하라
- orndinal()을 배열 인덱스로 사용 - 따라 하지 말 것!
    ```java
    // 226 page. 코드 37-1
    Set<Plant>[] plantsByLifeCycle = (Set<Plant>[]) new Set[LifeCycle.values().length];
    for (int i = 0; i < plantsByLifeCycle.length; ++i) {
        plantsByLifeCycle[i] = new HashSet<>();
    }
    
    for (Plant plant : garden) {
        plantsByLifeCycle[plant.lifeCycle.ordinal()].add(plant);
    }
    
    for (int i = 0; i < plantsByLifeCycle.length; i++) {
        System.out.printf("%s : %s%n", Plant.LifeCycle.values()[i], plantsByLifeCycle[i]);
    }
    ```
    `ANNUAL : [딜, 바질]
     PERENNIAL : [로즈마리, 라벤더]
     BIENNIAL : [캐러웨이, 파슬리]`
    - 배열은 제네릭과 호환되지 않아 비검사 형변환을 수행해야함. (아이템 28)
    - 각 인덱스의 의미를 모르니 출력 결과에 레이블을 달아야 함
    - 정확한 정숫값을 사용한다는 것을 직접 보증해야 함
    - 타입 세이프하지 않음. 
    - 잘못된 값 사용시 오동작하거나 ArrayIndexOutOfBoundsException을 던짐
- EnumMap
    - 열거 타입을 기로 사용하도록 설계한 아주 빠른 Map
    - 코드 37-2 EnumMap을 사용해 데이터와 열거 타입을 매핑     
        ```java
        // 227 page, 코드 37-2
        Map<LifeCycle, Set<Plant>> plantsByLifeCycle2 = new EnumMap<>(LifeCycle.class);

        for (LifeCycle lifeCycle : LifeCycle.values()) {
            plantsByLifeCycle2.put(lifeCycle, new HashSet<>());
        }
        for (Plant plant : garden) {
            plantsByLifeCycle2.get(plant.lifeCycle).add(plant);
        }
        System.out.println(plantsByLifeCycle2);

        ```
        `{ANNUAL=[딜, 바질], PERENNIAL=[로즈마리, 라벤더], BIENNIAL=[캐러웨이, 파슬리]}`
    - 37-1 과 성능은 비슷
    - 안전하지 않은 형변환 X
    - 출력 결과에 레이블을 달지 않아도 됨
    - 배열 인덱스 계산을 하지 않으므로 그 과정에서 오류가 날 가능성 X
    - Map의 타입 안정성과 배열이 성능을 모두 얻어냄
        - - 내부에서는 배열을 사용함
    - 생성자에서 받는 키타입의 Class 객체는 한정적 타입 토큰. 런타임 제네릭 타입 정보를 제공함 (아이템 33)        
    - 코드 37-3 스트림을 사용한 코드 1 - EnumMap을 사용하지 않는다
        ```java
        System.out.println(Arrays.stream(garden)
            .collect(Collectors.groupingBy(p -> p.lifeCycle)));
        ```
        - EnumMap을 사용할 때의 공자과 성능 이점이 사라짐
    - 코드 37-4 스트림을 사용한 코드 2- EnumMap을 이용해 데이터와 열거 타입을 매핑했다
        ```java
        System.out.println(Arrays.stream(garden)
            .collect(Collectors.groupingBy(p -> p.lifeCycle, ()-> new EnumMap<>(LifeCycle.class), toSet())));
        ```    
        - mapFactory 매개변수에 원하는 맵 구현체를 명시해 호출
- 스트림만 사용하면 EnumMap만 사용할 때랑 동작이 다름
    - 스트림
        - 해당 생애주기에 속하는 식물이 있을 때만 중첩 맵을 만듦
    - EnumMap
        - 언제나 식물의 생애주기당 하나씩의 중첩 맵을 만듦        
- 두 열거 타입 값들을 매핑하느는라 ordinal을 쓴 배열들의 배열
    - 코드 37-5 배열들의 배열의 인덱스에 ordinal()을 사용 - 따라 하지 말 것!
        ```java
        public enum Phase {
            SOLID, LIQUID, GAS;
        
            public enum Transition {
                MELT, FREEZE, BOIL, CONDENSE, SUBLIME, DEDPOSIT;
        
                // 행은 from의 ordinal을, 열은 to의 ordinal을 인덱스로 사용
                private static final Transition[][] TRANSITIONS = {
                        {null, MELT, SUBLIME},
                        {FREEZE, null, BOIL},
                        {DEDPOSIT, CONDENSE, null}
                };
        
                public static Transition from(Phase from, Phase to) {
                    return TRANSITIONS[from.ordinal()][to.ordinal()];
                }
            }
        }
        ```
        - Phase, Phase.Transition 열거 타입 수정 시 상전이 표 TRANSITIONS를 수정하지 않거나 잘못 수정시 런타임 에러 발생
           - 컴파일러는 ordinal과 배열 인덱스의 관계를 알 수 없음
        - 열거 타입의 갯수가 늘어나면 상전이 표의 크기는 제곱으로 커짐
- EnumMap 사용
    - 코드 37-6 중첩 EnumMap으로 데이터와 열거 타입 쌍을 연결했다.
        ```java
        
        public enum Phase {
            SOLID, LIQUID, GAS;
        
            public enum Transition {
                MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID)
                , BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID)
                , SUBLIME(SOLID, GAS), DEDPOSIT(GAS, SOLID);
        
                private final Phase from;
                private final Phase to;
        
                Transition(Phase from, Phase to) {
                    this.from = from;
                    this.to = to;
        
                }
        
                // 상전이 맵을 초기화 한다.
                private static final Map<Phase, Map<Phase, Transition>> m = Stream.of(values())
                        .collect(Collectors.groupingBy(t-> t.from, ()-> new EnumMap<>(Phase.class)
                                , Collectors.toMap(t -> t.to, t-> t,
                                        (x,y) -> y, () -> new EnumMap<>(Phase.class))));
        
                public static Transition from(Phase from, Phase to) {
                    return m.get(from).get(to);
                }
            }
        }
        ```       
        - 첫 번째 수집기인 groupingBy 에서 전이를 이전 상태를 기준으로 묶음
        - 두 번째 수집기인 toMap에서는 이후 상태를 전이에 대응시키는 EnumMap을 생성함
        - (x, y) -> y는 안쓰임. EnumMap을 얻으려면 맵 팩터리가 필요하고 수집기들은 점층적 팩터리(telescoping)를 제공함
    - 새로운 상태인 플라스마(PLASMA) 추가 시 
        - 기존 버전 : Phase, Phase.Transition, 배열 수정
        - EnumMap 버전 : Phase, Phase.Transition만 수정.                 
    - 내부적으로 배열로 구현되어 공간과 시간 낭비 거의 없이 안전하고 유지보수성이 좋음.
- 핵심 정리
    - **배열의 인덱스를 얻기 위해 ordinal을 쓰는 것은 일반적으로 좋지 않으니, 대신 EnumMap을 사용하라**
    -다차원 관계는 EnumMap<..., EnumMap<...>> 으로 표현하라. 
    - "애플리케이션 프로그래머는 Enum.ordinal을 (웬만해서는) 사용하지 말아야 한다(아이템 35)는 일반 원칙의 특수한 사례"  