package br.ufrn.imd.songday.cache;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FolloweesCacheWrapper {
    @Value("${api.cache.local}")
    private Boolean cacheLocalActive;

    @Autowired
    private FolloweesCacheRemoto cacheRemoto;

    @Autowired
    private FolloweesCacheLocal cacheLocal;

    public Set<String> get(String key) {
        return cacheLocalActive ? cacheLocal.get(key) : cacheRemoto.get(key);
    }

    public void delete(String key) {
        if (cacheLocalActive) {
            cacheLocal.delete(key);
        } else {
            cacheRemoto.delete(key);
        }
    }
}
