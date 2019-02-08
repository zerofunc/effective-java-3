package Chapter6.item37;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Phase {
    SOLID, LIQUID, GAS, PLAZMA;

    public enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID)
        , BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID)
        , SUBLIME(SOLID, GAS), DEDPOSIT(GAS, SOLID)
        , IONIZE(GAS, PLAZMA), DEIONIZE(PLAZMA, GAS);

//        // 행은 from의 ordinal을, 열은 to의 ordinal을 인덱스로 사용
//        private static final Transition[][] TRANSITIONS = {
//                {null, MELT, SUBLIME},
//                {FREEZE, null, BOIL},
//                {DEDPOSIT, CONDENSE, null}
//        };

//        public static Transition from(Phase from, Phase to) {
//            return TRANSITIONS[from.ordinal()][to.ordinal()];
//        }

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

    public static void main(String[] args) {
        System.out.println(Phase.Transition.from(Phase.GAS,  Phase.LIQUID));
    }
}
