package edu.eci.arsw.typefight.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import edu.eci.arsw.typefight.model.TypeFight;

public class CacheServiceTest {

     @InjectMocks
    private CacheService cacheService;

    @Mock
    private RedisTemplate<String, TypeFight> redisTemplate;

    @Mock
    private ValueOperations<String, TypeFight> valueOperations;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testLoadOrCreateTypeFight() {
        when(valueOperations.get(anyString())).thenReturn(null);
        TypeFight result = cacheService.loadOrCreateTypeFight();
        verify(valueOperations).get("sharedTypeFight");
        verify(valueOperations).set(eq("sharedTypeFight"), any(TypeFight.class));
    }

    @Test
    public void testSaveSharedTypeFight() {
        TypeFight typeFight = new TypeFight();
        cacheService.saveSharedTypeFight(typeFight);
        verify(valueOperations).set(eq("sharedTypeFight"), eq(typeFight));

    }
}
