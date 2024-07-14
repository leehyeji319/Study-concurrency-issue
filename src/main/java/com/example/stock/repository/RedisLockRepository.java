package com.example.stock.repository;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

//redis의 명령어를 실행할 수 있어야 하기 때문에 redis용 repository 추가, redis template을 변수로 추가
@Component
public class RedisLockRepository {

    private RedisTemplate<String, String> redisTemplate;

    public RedisLockRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    //lock 메서드
    //key에 사용할 변수를 받아야하기 때문에 매개변수에 key 추가.
    //그리고 아래에서 setnx 명령어 사용
    public Boolean lock(Long key) {
        return redisTemplate
                .opsForValue()
                // key에는 stockId를 넣어주고 value에는 lock 문자열 넣어주기
                .setIfAbsent(generateKey(key), "lock", Duration.ofMillis(3_000));
    }
    //key와 setnx를 활용해서 lock을 하고
    //로직이 끝나면 언락메서드를 통해 락을 해제

    //redis를 활용하는 방식도 로직실행 전후로 락획득, 락해제를 해야하기 때문에 Facade 클래스 생성해주자.

    //unlock 메서드
    public Boolean unlock(Long key) {
        return redisTemplate.delete(generateKey(key));
    }

    private String generateKey(Long key) {
        return key.toString();
    }
}
