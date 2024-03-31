package com.example.rangiffler.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
@Setter
@Entity
@Table(name = "user")
@IdClass(CountryId.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, columnDefinition = "UUID default gen_random_uuid()")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = true)
    private String firstname;

    @Column(nullable = true)
    private String lastname;

    @Column(name = "avatar", columnDefinition = "bytea")
    private byte[] photo;

    @Id
    @ManyToOne
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private CountryEntity country;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendsEntity> friends = new ArrayList<>();

    @OneToMany(mappedBy = "friend", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendsEntity> invites = new ArrayList<>();


//    public void addFriends(boolean pending, UserEntity... friends) {
//        List<FriendsEntity> friendsEntities = Stream.of(friends)
//                .map(f -> {
//                    FriendsEntity fe = new FriendsEntity();
//                    fe.setUser(this);
//                    fe.setFriend(f);
//                    fe.setPending(pending);
//                    return fe;
//                }).toList();
//
//        this.friends.addAll(friendsEntities);
//    }
//
//    public void removeFriends(UserEntity... friends) {
//        for (UserEntity friend : friends) {
//            getFriends().removeIf(f -> f.getFriend().getId().equals(friend.getId()));
//        }
//    }
//
//    public void removeInvites(UserEntity... invitations) {
//        for (UserEntity invite : invitations) {
//            getInvites().removeIf(i -> i.getUser().getId().equals(invite.getId()));
//        }
//    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserEntity that = (UserEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
