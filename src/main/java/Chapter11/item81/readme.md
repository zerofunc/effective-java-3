# 아이템 81 wait와 notify보다는 동시성 유틸리티를 애용하라
- java 5에서 도입된 동시성 유틸리티가 wait와 notify로 하드코딩해야 했떤 전형적인 일들을 대신 처리해준다
- **wait와 notify는 올바르게 사용하기가 아주 까다로우니 고수준 동시성 유틸리티를 사용하자**
- `java.util.concurrent`의 고수준 유틸리티
    - 실행자 프레임 워크 (아이템 80)
    - 동시성 컬렉션(concurrent collection)
    - 동기화 장치(synchronizer)
## 동시성 컬렉션
- List, Queue, Map 같은 표준 컬렉션 인터페이스에 동시성을 가미해 구현한 고성능 컬렉션
- 동기화를 가작의 내부에서 수행한다
- **동시성 컬렉션에서 동시성을 무력화하는 건 불가능하며, 외부에서 락을 추가로 사용하면 오히려 속도가 느려진다.**
- 동시성을 무력화하는 것이 불가능 하므로 여러 메서드를 원자적으로 호출하는 일 역시 불가능하다.
    - 여러 기본 동작을 하나의 원자적 동작으로 묶는 '상태 의존적 수정' 메서드들이 추가됨    
    - java 8 컬렉션 인터페이스에도 디폴트 메소드 형태로 추가됨
- 코드 81-1 ConcurrentMap으로 구현한 동시성 정규화 맵 - 최적은 아니다
```java
private static final ConcurrentMap<String, String> map =
            new ConcurrentHashMap<>();

    public static String intern(String s) {
        String previousValue = map.putIfAbsent(s, s);
        return previousValue == null ? s : previousValue;
    }

```    
- ConcurrentHashMap은 get 같은 검색 기능에 최적화 되었다. get을 먼저 호출하고 필요할 때마 putIfAbsent를 호출하면 더 빠름
- 코드 81-2 ConcurrentMap으로 구현한 동시성 정규화 맵 - 더 빠르다!
```java
public static String intern(String s) {
    String result = map.get(s);
    if (result == null) {
        result = map.putIfAbsent(s, s);
        if (result == null)
            result = s;
    }
    return result;
}
```
- 동시성 컬렉션은 동기화한 컬렉션을 낡은 유산으로 만들어 버렸다    
    - 이제는 **Collections.synchronizedMap 보다는 ConcurrentMap을 사용하는 게 훨씬 좋다**
    - 동기화된 맵을 동시성 맵으로 교체하는 것만으로 동시성 애플리케이션의 성능은 극적으로 개선된다
- 컬렉션 인터페이스 중 일부는 작업이 성공적으로 완료될 때 까지 기다리도록(차단되도록) 확장되었따.
    - BlockingQueue.task() 는 큐의 첫 원소르 꺼낸다. 큐가 비어있으면 새로운 원소가 추가될 때 까지 기다린다.
    - 작업 큐(생산자-소비자 큐)로 쓰기에 적합하다
    - 작업 큐 ? 하나 이상의 생산자(producer) 스레드가 작업을 큐에 추가하고, 하나 이상의 소비자(consumer) 스레드가 큐에ㅔ 있는 작업을 꺼내 처리함
- 동기화 장치는 스레드가 다른 스레드를 기다릴 수 있게 하여, 서로 작업을 조유할 수 있게 해준다
    - 가장 자주 쓰이는 동기화 장치는 CountDonwLatch와 Semaphore
    - CyclieBarrier와 Exchanger는 그보다 덜 쓰임
    - 가장 강력한 동기화 장치는 Phaser
- 카운트다운 래치
    - 하나 이상의 스레드가 또 다른 하나 이상의 스레드 작업이 끝날 때 까지 기다리게 한다.
    - CountDownLatch의 유일한 생성자는 int값을 받음. 래치의 countDown 메서드를 몇 번 호출해야 대기 중인 스레드들을 깨우는지를 결정함
    - 코드 81-3 동시 실행 시간을 재는 간단한 프레임워크
    ```java
    public class ConcurrentTimer {
        private ConcurrentTimer() { } // Noninstantiable
    
        public static long time(Executor executor, int concurrency,
                                Runnable action) throws InterruptedException {
            CountDownLatch ready = new CountDownLatch(concurrency);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done  = new CountDownLatch(concurrency);
    
            for (int i = 0; i < concurrency; i++) {
                executor.execute(() -> {
                    ready.countDown(); // 타이머에게 준비를 마쳤음을 알린다.
                    try {
                        start.await(); // 모든 작업자 스레드가 준비될 때까지 기다린다
                        action.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();  // 타이머에게 작업을 마쳤음을 알린다
                    }
                });
            }
    
            ready.await();     // 모든 작업자가 준비될 때까지 기다린다
            long startNanos = System.nanoTime();
            start.countDown(); // 작업자들을 깨운다
            done.await();      // 모든 작업자가 일을 끝마치기를 기다린다
            return System.nanoTime() - startNanos;
        }
    }

    ```         
    - time 메서드에 남겨진 실행자(executor)는 concurrency 매개변수로 지정한 동시성 수준만큼의 스레드를 생성할 수 있어야 한다. 그렇지 못하면 이 메서드는 결고 끝나지 않을 것이다
    - 이런 상태를 기아 교착상태(thread starvation deadlock)라 한다
    - **시간 간격을 잴 때는 항상 System.currentTimeMillis가 아닌 System.nanoTime을 사용하자**
        - System.nanoTime은 더 정확하고 정밀하며 시스템의 실시간 시계의 시간 보정에 영향 받지 않는다.
    - 정밀한 시간 측정은 매우 어려운 작업이라, 꼭 해야 한다면 jmh 같은 특수 프레임 워크를 사용해야 한다.
- wait 메서드는 스레드가 어떤 조건이 충족되기를 기다리게 할 때 사용한다.
- 락 객체의 wait메서드는 반드시 그 객체를 잠근 동기화 영역 안에서 호출해야 한다.
- 코드 81-4 wait 메서드를 사용하는 표준 방식
```java
synchronized (ob) {
    while (<조건이 충족되지 않았다>) {
        obj.wait(); // (락을 놓고, 깨어나면 다시 잡는다. )
    }
    
    ... // 조건이 충족됐을 때의 동작을 수행한다.
}
```    
- **wait메서드를 사용할 때는 반드시 대기 반복문(wait loop) 관용구를 사용하라. 반복문 밖에서는 절대로 호출하지 말자**
- 대기 후에 조건을 검사하여 조건이 충족되지 않았다면 다시 대기하게 하는 것은 안전 실패는 막는 조치다
- 조건이 만족되지 않아도 스레드가 깨어날 수 있는 상황
    - 스레드가 notify를 호출한 다음 대기 중이던 스레드가 깨어나는 사이에 다른 스레드가 락을 얻어 그 락이 보호하는 상태를 변경한다.
    - 조건이 만족되지 않았음에도 다른 스레드가 실수로 혹은 악의적으로 notify를 호출한다. 공개된 객체를 락으로 사용해 대기하는 클래스는 이런 위험에 노출된다. 외부에 노출된 객체의 동기화된 메서드 안에서 호출하는
    wait는 모두 이 문제에 영향을 받는다.
    - 깨우는 스레드는 지나치게 관대해서, 대기 중인 스레드 중 일부만 조건이 충족되어도 nofiyAll을 호출해 모든 스레드를 깨울 수도 있다.
    - 대기 중인 스레드가 (드물게) notify 없이도 깨어나는 경우가 있다. 허위 각성(spurious wakeup)이라는 현상이다
- notify와 notifyAll
    - 일반적으로 언제나 notifyAll을 사용하라는 게 합리적이고 안전한 조언이 될 것이다
    - notifyAll을 사용하면 관련 없는 스레드가 실수로 혹은 악의적으로 wait를 호출하는 공격응로부터 보호할 수 있다.
    - 그런 스레드가 중요한 notify를 삼켜버린다면 꼭 깨어났어야 할 스레드들이 영원히 대기하게 될 숭 ㅣㅆ다.