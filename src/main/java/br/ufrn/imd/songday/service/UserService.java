package br.ufrn.imd.songday.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.ufrn.imd.songday.exception.NotFoundException;
import br.ufrn.imd.songday.exception.ValidationException;
import br.ufrn.imd.songday.model.User;
import br.ufrn.imd.songday.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(User newUser) {
        Optional<User> search = repository.findByUsername(newUser.getUsername());

        if (search.isPresent()) {
            throw new ValidationException("Nome de usuário já existe");
        }
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        return repository.save(newUser);
    }

    public Page<User> findAll(Pageable pageable) {
        Page<User> userPage = repository.findAll(pageable);

        if (!userPage.hasContent()) {
            throw new NotFoundException("Nenhum usuário encontrado");
        }

        return userPage;
    }

    public User findById(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    public User follow(String idFollowee, String userId) {
        User user = findById(userId);
        if (idFollowee.equals(user.getId())) {
            throw new ValidationException("Não é possível seguir seu próprio usuário");
        }

        boolean hasIdFollowee = user.getFollowees().contains(idFollowee);
        if (hasIdFollowee) {
            throw new ValidationException("Não é possível seguir um usuário mais de uma vez");
        }

        // verifica se o usuário a ser seguido existe
        findById(idFollowee);

        user.getFollowees().add(idFollowee);
        return repository.save(user);
    }

    public User unfollow(String idFollowee, String userId) {
        User user = findById(userId);
        boolean hasIdFollowee = user.getFollowees().contains(idFollowee);
        if (!hasIdFollowee) {
            throw new ValidationException("Usuário não seguido");
        }

        user.getFollowees().remove(idFollowee);
        return repository.save(user);
    }

    public User findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(String.format("O usuário '%s' não foi encontrado", username)));
    }
}
