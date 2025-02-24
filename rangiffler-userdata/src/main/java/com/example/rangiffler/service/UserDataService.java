package com.example.rangiffler.service;

import com.example.rangiffler.data.UserEntity;
import com.example.rangiffler.data.repository.UserRepository;
import com.example.rangiffler.model.UserJson;
import jakarta.annotation.Nonnull;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class UserDataService {

    private static final Logger LOG = LoggerFactory.getLogger(UserDataService.class);
    private final UserRepository userRepository;

    @Autowired
    public UserDataService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @KafkaListener(topics = "users", groupId = "userdata")
    public void listener(@Payload UserJson user, ConsumerRecord<String, UserJson> cr) {
        LOG.info("### Kafka topic [users] received message: " + user.username());
        LOG.info("### Kafka consumer record: " + cr.toString());
        UserEntity userDataEntity = new UserEntity();
        userDataEntity.setUsername(user.username());
        userDataEntity.setCurrency(DEFAULT_USER_CURRENCY);
        UserEntity userEntity = userRepository.save(userDataEntity);
        LOG.info(String.format(
                "### User '%s' successfully saved to database with id: %s",
                user.username(),
                userEntity.getId()
        ));
    }

    @Transactional
    public @Nonnull
    UserJson update(@Nonnull UserJson user) {
        UserEntity userEntity = getRequiredUser(user.username());
        userEntity.setFirstname(user.firstname());
        userEntity.setSurname(user.surname());
        userEntity.setCurrency(user.currency());
        userEntity.setPhoto(user.photo() != null ? user.photo().getBytes(StandardCharsets.UTF_8) : null);
        UserEntity saved = userRepository.save(userEntity);
        return UserJson.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public @Nonnull
    UserJson getCurrentUser(@Nonnull String username) {
        return UserJson.fromEntity(getRequiredUser(username));
    }

    @Transactional(readOnly = true)
    public @Nonnull
    List<UserJson> allUsers(@Nonnull String username) {
        Set<UserJson> result = new HashSet<>();
        for (UserEntity user : userRepository.findByUsernameNot(username)) {
            List<FriendsEntity> sendInvites = user.getFriends();
            List<FriendsEntity> receivedInvites = user.getInvites();

            if (!sendInvites.isEmpty() || !receivedInvites.isEmpty()) {
                Optional<FriendsEntity> inviteToMe = sendInvites.stream()
                        .filter(i -> i.getFriend().getUsername().equals(username))
                        .findFirst();

                Optional<FriendsEntity> inviteFromMe = receivedInvites.stream()
                        .filter(i -> i.getUser().getUsername().equals(username))
                        .findFirst();

                if (inviteToMe.isPresent()) {
                    FriendsEntity invite = inviteToMe.get();
                    result.add(UserJson.fromEntity(user, invite.isPending()
                            ? FriendState.INVITE_RECEIVED
                            : FriendState.FRIEND));
                }
                if (inviteFromMe.isPresent()) {
                    FriendsEntity invite = inviteFromMe.get();
                    result.add(UserJson.fromEntity(user, invite.isPending()
                            ? FriendState.INVITE_SENT
                            : FriendState.FRIEND));
                }
            } else {
                result.add(UserJson.fromEntity(user));
            }
        }
        return new ArrayList<>(result);
    }

    @Transactional(readOnly = true)
    public @Nonnull
    List<UserJson> friends(@Nonnull String username, boolean includePending) {
        return getRequiredUser(username)
                .getFriends()
                .stream()
                .filter(fe -> includePending || !fe.isPending())
                .map(fe -> UserJson.fromEntity(fe.getFriend(), fe.isPending()
                        ? FriendState.INVITE_SENT
                        : FriendState.FRIEND))
                .toList();
    }

    @Transactional(readOnly = true)
    public @Nonnull
    List<UserJson> invitations(@Nonnull String username) {
        return getRequiredUser(username)
                .getInvites()
                .stream()
                .filter(FriendsEntity::isPending)
                .map(fe -> UserJson.fromEntity(fe.getUser(), FriendState.INVITE_RECEIVED))
                .toList();
    }

    @Transactional
    public UserJson addFriend(@Nonnull String username, @Nonnull FriendJson friend) {
        UserEntity currentUser = getRequiredUser(username);
        UserEntity friendEntity = getRequiredUser(friend.username());

        currentUser.addFriends(true, friendEntity);
        userRepository.save(currentUser);
        return UserJson.fromEntity(friendEntity, FriendState.INVITE_SENT);
    }

    @Transactional
    public @Nonnull
    List<UserJson> acceptInvitation(@Nonnull String username, @Nonnull FriendJson invitation) {
        UserEntity currentUser = getRequiredUser(username);
        UserEntity inviteUser = getRequiredUser(invitation.username());

        FriendsEntity invite = currentUser.getInvites()
                .stream()
                .filter(fe -> fe.getUser().getUsername().equals(inviteUser.getUsername()))
                .findFirst()
                .orElseThrow();

        invite.setPending(false);
        currentUser.addFriends(false, inviteUser);
        userRepository.save(currentUser);

        return currentUser
                .getFriends()
                .stream()
                .map(fe -> UserJson.fromEntity(fe.getFriend(), fe.isPending()
                        ? FriendState.INVITE_SENT
                        : FriendState.FRIEND))
                .toList();
    }

    @Transactional
    public @Nonnull
    List<UserJson> declineInvitation(@Nonnull String username, @Nonnull FriendJson invitation) {
        UserEntity currentUser = getRequiredUser(username);
        UserEntity friendToDecline = getRequiredUser(invitation.username());

        currentUser.removeInvites(friendToDecline);
        friendToDecline.removeFriends(currentUser);

        userRepository.save(currentUser);
        userRepository.save(friendToDecline);

        return currentUser.getInvites()
                .stream()
                .filter(FriendsEntity::isPending)
                .map(fe -> UserJson.fromEntity(fe.getUser(), FriendState.INVITE_RECEIVED))
                .toList();
    }

    @Transactional
    public @Nonnull
    List<UserJson> removeFriend(@Nonnull String username, @Nonnull String friendUsername) {
        UserEntity currentUser = getRequiredUser(username);
        UserEntity friendToRemove = getRequiredUser(friendUsername);

        currentUser.removeFriends(friendToRemove);
        currentUser.removeInvites(friendToRemove);
        friendToRemove.removeFriends(currentUser);
        friendToRemove.removeInvites(currentUser);

        userRepository.save(currentUser);
        userRepository.save(friendToRemove);

        return currentUser
                .getFriends()
                .stream()
                .map(fe -> UserJson.fromEntity(fe.getFriend(), fe.isPending()
                        ? FriendState.INVITE_SENT
                        : FriendState.FRIEND))
                .toList();
    }

    @Nonnull
    UserEntity getRequiredUser(@Nonnull String username) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("Can`t find user by username: " + username);
        }
        return user;
    }
}
