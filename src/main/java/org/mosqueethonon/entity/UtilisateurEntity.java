package org.mosqueethonon.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.List;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "utilisateur", schema = "moth")
@Data
public class UtilisateurEntity implements UserDetails, Auditable {

    @Id
    @Column(name = "idutil")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "txutiluser")
    private String username;
    @Column(name = "txutilpassword")
    private String password;
    @Embedded
    private Signature signature;
    @OneToMany
    @JoinColumn(name = "txutiluser", referencedColumnName = "txutiluser", updatable = false, insertable = false)
    private List<UtilisateurRoleEntity> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
