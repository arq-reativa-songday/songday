package br.ufrn.imd.songday.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufrn.imd.songday.dto.post.PostInput;
import br.ufrn.imd.songday.dto.post.PostMapper;
import br.ufrn.imd.songday.dto.post.PostSearchDto;
import br.ufrn.imd.songday.dto.post.SearchPostsCountDto;
import br.ufrn.imd.songday.dto.post.SearchPostsDto;
import br.ufrn.imd.songday.model.Post;
import br.ufrn.imd.songday.service.PostService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("posts")
public class PostController {
    @Autowired
    private PostService service;

    @Autowired
    private PostMapper mapper;

    @PostMapping
    public ResponseEntity<Post> save(@Valid @RequestBody PostInput postInput) {
        Post post = mapper.toPost(postInput);
        return ResponseEntity.ok(service.createPost(post));
    }

    @PostMapping("/search")
    public ResponseEntity<List<PostSearchDto>> getAll(@Valid @RequestBody SearchPostsDto search) {
        return ResponseEntity.ok(service.findAll(search));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<String> follow(@PathVariable String id, @RequestBody String userId) {
        service.like(id, userId);
        return ResponseEntity.ok("Publicação curtida com sucesso");
    }

    @PostMapping("/{id}/unlike")
    public ResponseEntity<String> unfollow(@PathVariable String id, @RequestBody String userId) {
        service.unlike(id, userId);
        return ResponseEntity.ok("Deixou de curtir com sucesso");
    }

    @PostMapping("/search/count")
    public ResponseEntity<Long> searchPostsCount(@Valid @RequestBody SearchPostsCountDto search) {
        return ResponseEntity.ok(service.searchPostsCount(search));
    }
}
