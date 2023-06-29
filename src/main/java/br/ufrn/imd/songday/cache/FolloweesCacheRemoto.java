package br.ufrn.imd.songday.cache;

import java.util.Set;

import org.redisson.api.RMapCache;
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
public class FolloweesCacheRemoto extends CacheTemplate<String, Set<String>> {
    @Autowired
    private UserRepository repository;
    private RMapCache<String, Set<String>> map;

    public FolloweesCacheRemoto(RedissonClient redissonClient) {
        Codec codec = new TypedJsonJacksonCodec(String.class, Set.class);
        this.map = redissonClient.getMapCache("/followees-remoto/", codec);
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
        System.out.println("Buscando chave " + key + " no cache (remoto)...");
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
        System.out.println("Atualizando chave " + key + " no cache (remoto)");
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
        System.out.println("Excluindo chave " + key + " do cache (remoto)");
        this.map.fastRemove(key);
    }
}
