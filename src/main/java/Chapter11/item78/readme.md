# 아이템 78 공유 중인 가변 데이터는 동기화해 사용하라
- synchronized 키워드는 해당 메서드나 블록을 한 번에 한 스레드씩 수행하도록 보장한다.
- 락을 건 메서드는 객체를 하나의 일관된 상태에서 다른 일관된 상태로 변화시킨다.
- 동기화의 중요한 기능
    - 동기화 없이는 한 스레드가 만든 변화를 다른 스레드에서 확인하지 못할 수 있다
- 언어 명세상 long과 double 외의 변수를 읽고 쓰는 동작은 원자적(atomic)이다
- "성능을 높이려면 원자적 데이터를 읽고 쓸 때는 동기화하지 말아야겠다" => 위험한 생각
    - 스레드가 필드를 읽을 때 항상 '수정이 완전히 반영된' 값을 얻는다고 보장하지만, 한 스레드가 저장한 값이 다른 스레드에게 '보이는가'는 보장하지 않는다.
    - **동기화는 배타적 실행뿐 아니라 스레드 사이의 안정적인 통신에 꼭 필요하다**
- **Thread.stop 은 사용하지 말자**
    - 데이터가 훼손될 수 있다
    - Thread.stop(Throwable obj) 자바 11에서 제거됨
- 코드 78-1 잘못된 코드 - 이 프로그램은 얼마나 오래 실행될까?
```java
public class StopThread {
    private static boolean stopRequested;
    
    public static void main(String[] args) {
        Thread backgroundThread = new Thread(() -> {
           int i = 0;
           while (!stopRequested) {
               i++;
           }
        });
        
        backgroundThread.start();
        
        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
    }
}
```  
- 동기화하지 않으면 메인 스레드가 수정한 값을 백그라운드 스레드가 언제쯤에 보게될지 보증할 수 없다.
- 동기화가 빠지면 가상 머신이 다음과 같은 최적화를 수행할 수도 있다
    ```java
    // 원래 코드
    while (!stopRequested){
      i++;
    }
  
    // 최적화한 코드
    if (!stopRequested) {
      while (true) {  
        i++;  
      }
    }
    ```
    
- 코드 782-2 적절히 동기화해 스레드가 정상 종료된다
```java
public class StopThread {
    private static boolean stopRequested;
    
    private static synchronized void requestStop() {
        stopRequested = true;
    }
    
    private static synchronized boolean stopRequested() {
        return stopRequested;
    }
    
    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(() -> {
           int i = 0;
           while (!stopRequested()) {
               i++;
           }
        });
        backgroundThread.start();
        
        TimeUnit.SECONDS.sleep();
        requestStop();
    } 
}
```
- 쓰기 메서드와 읽기 메서드 모두를 동기화 했다.
    - 쓰기와 읽기 모두가 동기화되지 않으면 동작을 보장하지 않는다.
- 속도가 더 빠른 대안. 필드를 volatile으로 선언하면 동기화를 생략해도 된다.
    - 배타적 수행과는 상관없지만 항상 가장 최근에 기록된 값을 읽게 됨을 보장한다.
    - 코드 78-3 volatile 필드를 사용해 스레드가 정상 종료한다.
    ```java
    public class StopThread {
      private static volatile boolean stopRequested;
    
      public static void main(String[] args) throws InterruptedException {
          Thread backgroundThread = new Thread(() -> {
              int i = 0;
              while (!stopRequested) {
                i++;
              }
          });  
          backgroundThread.start();
        
          TimeUnit.SECONDS.sleep(1);
          stopRequested = true;
      }
    }
    ```     
- 코드 78-4 잘못된 코드 - 동기화가 필요하다!
```java
private static volatile int nextSerialNumber = 0;

public static int generateSerialNumber() {
    return nextSerialNumber++;
}
```
- 증가 연산자는 코드상으로는 하나지만 실제로는 nextSerialNumber 필드에 두 번 접근한다.
- 값을 읽고 증가한 새로운 값을 저장하는 사이에 값을 읽어갈 수 있다. 
- 프로그램이 잘못된 결과를 계산해내는 오류를 안전 실패(safety failure)라고 한다

- synchronize한정자를 붙이면 동시에 호출해도 서로 간섭하지 않으면 이전 호출이 변경한 값을 읽게 된다.
- `java.util.concurrent.atomic` 패키지에는 스레드 안전한 프로그래밍을 지원하는 클래스들이 담겨 있다.
    - volatile은 동기화의 두 효과 중 통신 쪽만 지원
    - 이 패키지는 원자성(배타적 실행)까지 지원
    - 코드 78-5 java.util.concurrent.atomic을 이욯한 락-프리 동기화
    ```java
    private static final AtomicLong nextSerialNum = new AtomicLong();
    
    public static long generateSerialNumber() {
        return nextSerialNum.getAndIncrement();
    }
    ```    
- 이번 아이템에서 나온 문제점을 피하는 가장 좋은 방법은 **가변 데이터는 단일 스레드에서만 쓰도록 하자**
    - 이런 정책을 받아들였다면 문서에 남겨 유지보수 과정에서도 정책이 계속 지켜지도록 하는게 중요하다
- 사실상 불변(effectively immutable)
    - 한 스레드가 데이터를 다 수정한 후 다른 스레드에 공유할 때는 해당 객체에서 공유하는 부부만 동기화해도 된다
    - 다른 스레드에 이런 객체를 건내는 행위를 안전 발행(safe publication)이라 한다
- 객체를 안전한게 발행하는 방법들
    - 클래스 초기화 과정에서 객체를 정적 필드, volatile 필드, final 필드, 혹은 보통의 락을 통해 접근하는 필드에 저장
    - 동시성 컬렉션에 저장
    
- 핵심정리
    - **여러 스레드가 가변 데이터를 공유한다면 그 데이터를 읽고 쓰는 동작은 반드시 동기화 해야 한다**
    - 동기화 하지 않으면 한 스레드가 수행한 변경을 다른 스레드가 보지 못할 수도 있다.
    - 공유되는 가변 데이터를 동기화하는 데 실패하면 응답 불가 상태에 빠지거나 안전 실패로 이어질 수 있다.
    - 배타적 실행은 필요 없고 스레드끼리의 통신만 필요하다면 volatile 한정자만으로 동기화할 수 있다             
    
     