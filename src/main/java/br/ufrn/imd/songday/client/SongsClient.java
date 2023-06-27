package br.ufrn.imd.songday.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "songs")
public interface SongsClient {
    @GetMapping(value = "/songs/{id}")
    ResponseEntity<Object> findById(@PathVariable String id);

    @PutMapping(value = "/songpopularities/score")
    ResponseEntity<Void> updateScore(@RequestParam String songId);
}
