package br.ufrn.imd.songday.cache;

import java.util.Set;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.repository.UserRepository;
import br.ufrn.imd.songday.cache.template.CacheTemplate;

@Component
public class FolloweesCacheLocal extends CacheTemplate<String, Set<String>> {
    @Autowired
    private UserRepository repository;
    private RLocalCachedMap<String, Set<String>> map;

    public FolloweesCacheLocal(RedissonClient redissonClient) {
        Codec codec = new TypedJsonJacksonCodec(String.class, Set.class);
        LocalCachedMapOptions<String, Set<String>> options = LocalCachedMapOptions.<String, Set<String>>defaults()
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LFU)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR);

        this.map = redissonClient.getLocalCachedMap("/followees-local/", codec, options);
    }

    @Override
    protected Set<String> getFromSource(String key) {
        System.out.println("Buscando chave " + key + " no banco...");
        User user = this.repository.findByUsername(key)
                .orElseThrow(() -> new NotFoundException(String.format("O usuário '%s' não foi encontrado", key)));
        return user.getFollowees();
    }

    @Override
    protected Set<String> getFromCache(String key) {
        System.out.println("Buscando chave " + key + " no cache (local)...");
        return map.get(key);
    }

    @Override
    protected Set<String> updateSource(String key, Set<String> entity) {
        System.out.println("Atualizando chave " + key + " no banco (???)");
        //
        return entity;
    }

    @Override
    protected Set<String> updateCache(String key, Set<String> entity) {
        System.out.println("Atualizando chave " + key + " no cache (local)");
        this.map.fastPut(key, entity);
        return entity;
    }

    @Override
    protected void deleteFromSource(String key) {
        System.out.println("Excluindo chave " + key + " do banco (???)");
        //
    }

    @Override
    protected void deleteFromCache(String key) {
        System.out.println("Excluindo chave " + key + " do cache (local)");
        this.map.fastRemove(key);
    }
}
