package br.ufrn.imd.songday.service;

import br.ufrn.imd.songday.client.SongsClient;
import br.ufrn.imd.songday.dto.post.PostSearchDto;
import br.ufrn.imd.songday.dto.post.SearchPostsCountDto;
import br.ufrn.imd.songday.dto.post.SearchPostsDto;
import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.exception.ServicesCommunicationException;
import br.ufrn.imd.songday.exception.ValidationException;
import br.ufrn.imd.songday.model.Post;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.repository.PostRepository;
import br.ufrn.imd.songday.repository.UserReadOnlyRepository;
import br.ufrn.imd.songday.util.DateUtil;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository repository;

    @Autowired
    private SongsClient songsClient;

    @Autowired
    private UserReadOnlyRepository userReadOnlyRepository;

    @Autowired
    private RedissonClient redisClient;

    public Post createPost(Post newPost) {
        User user = userReadOnlyRepository.findById(newPost.getUserId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (!existsSongById(newPost.getSongId())) {
            throw new NotFoundException("Música não encontrada");
        }

        boolean hasPostToday = repository.existsByUserIdAndCreatedAtBetween(user.getId(), DateUtil.getTodayStartDate(),
                DateUtil.getTodayEndDate());
        if (hasPostToday) {
            throw new ValidationException("Só é possível escolher uma música por dia");
        }

        Post postSaved = repository.save(newPost);
        updateSongScore(postSaved.getSongId());

        return postSaved;
    }

    public List<PostSearchDto> findAll(SearchPostsDto search) {
        List<PostSearchDto> posts = repository.findPosts(search.getFollowees(), search.getOffset(), search.getLimit());

        if (posts.isEmpty()) {
            throw new NotFoundException("Nehuma publicação encontrada");
        }

        return posts;
    }

    public Post findById(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
    }

    public Post like(String idPost, String userId) {
        Post post = findById(idPost);

        User user = userReadOnlyRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        boolean hasIdUser = post.getUserLikes().contains(user.getId());
        if (hasIdUser) {
            throw new ValidationException("Não é possível curtir uma publicação mais de uma vez");
        }

        post.getUserLikes().add(user.getId());
        return repository.save(post);
    }

    public Post unlike(String idPost, String userId) {
        Post post = findById(idPost);

        boolean hasIdUser = post.getUserLikes().contains(userId);
        if (!hasIdUser) {
            throw new ValidationException("Publicação não curtida");
        }

        post.getUserLikes().remove(userId);
        return repository.save(post);
    }

    public Long searchPostsCount(SearchPostsCountDto search) {
        return repository.countByUserIdInAndCreatedAtBetween(search.getFollowees(), search.getStart(), search.getEnd());
    }

    private Boolean existsSongById(String songId) {
        var song = songsClient.findById(songId)
                .doOnError(e -> {
                    if (e.getLocalizedMessage().contains("404 Not Found")) {
                        throw new NotFoundException("Música não encontrada");
                    }
                    throw new ServicesCommunicationException(
                            "Erro durante a comunicação com Songs para recuperar a música por id: "
                                    + e.getLocalizedMessage());
                }).block();
        return song != null;
    }

    private void updateSongScore(String songId) {
        RTopic songPopularityTopic = redisClient.getTopic("songPopularityTopic", StringCodec.INSTANCE);
        long subNumber = songPopularityTopic.publish(songId);
        System.out.println("Consumidores: " + subNumber);
//        return songsClient.updateScore(songId)
//                .doOnError(e -> {
//                    throw new ServicesCommunicationException(
//                            "Erro durante a comunicação com Songs para atualizar o score da música: "
//                                    + e.getLocalizedMessage());
//                }).block();
    }
}
