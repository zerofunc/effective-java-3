package Chapter6.item37;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class Plant {
    enum LifeCycle {ANNUAL, PERENNIAL, BIENNIAL}

    final String name;
    final LifeCycle lifeCycle;

    public Plant(String name, LifeCycle lifeCycle) {
        this.name = name;
        this.lifeCycle = lifeCycle;
    }


    @Override
    public String toString() {
        return name;
    }

    public static void main(String[] args) {

        Plant[] garden = {
                new Plant("바질", LifeCycle.ANNUAL),
                new Plant("캐러웨이", LifeCycle.BIENNIAL),
                new Plant("딜", LifeCycle.ANNUAL),
                new Plant("파슬리", LifeCycle.BIENNIAL),
        };

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

        // 227 page, 코드 37-2
        Map<LifeCycle, Set<Plant>> plantsByLifeCycle2 = new EnumMap<>(LifeCycle.class);

        for (LifeCycle lifeCycle : LifeCycle.values()) {
            plantsByLifeCycle2.put(lifeCycle, new HashSet<>());
        }
        for (Plant plant : garden) {
            plantsByLifeCycle2.get(plant.lifeCycle).add(plant);
        }
        System.out.println(plantsByLifeCycle2);

        // 228 page, 코드 37-3
        System.out.println(Arrays.stream(garden)
        .collect(Collectors.groupingBy(p -> p.lifeCycle)));

        // 228 page, 코드 37-4
        System.out.println(Arrays.stream(garden)
                .collect(Collectors.groupingBy(p -> p.lifeCycle, ()-> new EnumMap<>(LifeCycle.class), toSet())));
    }
}
