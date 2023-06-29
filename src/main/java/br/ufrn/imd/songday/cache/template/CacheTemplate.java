package br.ufrn.imd.songday.cache.template;

public abstract class CacheTemplate<KEY, ENTITY> {

    public ENTITY get(KEY key) {
        System.out.println("Obtendo chave " + key + "...");
        ENTITY entity = getFromCache(key);

        if (entity == null) {
            System.out.println("Chave " + key + " não existe no cache");
            entity = getFromSource(key);
            updateCache(key, entity);
        }

        return entity;
    }

    public ENTITY update(KEY key, ENTITY entity) {
        System.out.println("Atualizando chave " + key + "...");
        updateSource(key, entity);
        deleteFromCache(key);
        return entity;
    }

    public void delete(KEY key) {
        System.out.println("Excluindo chave " + key + "...");
        deleteFromSource(key);
        deleteFromCache(key);
    }

    abstract protected ENTITY getFromSource(KEY key);
    abstract protected ENTITY getFromCache(KEY key);
    abstract protected ENTITY updateSource(KEY key, ENTITY entity);
    abstract protected ENTITY updateCache(KEY key, ENTITY entity);
    abstract protected void deleteFromSource(KEY key);
    abstract protected void deleteFromCache(KEY key);

}
