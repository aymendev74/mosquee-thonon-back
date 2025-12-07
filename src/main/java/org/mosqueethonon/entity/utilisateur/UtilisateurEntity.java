package org.mosqueethonon.entity.utilisateur;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.List;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "utilisateur", schema = "moth")
@Getter
@Setter
public class UtilisateurEntity implements UserDetails, Auditable {

    @Id
    @Column(name = "idutil")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "txutiluser")
    private String username;
    @Column(name = "txutilpassword")
    private String password;
    @Column(name = "txutilnom")
    private String nom;
    @Column(name = "txutilprenom")
    private String prenom;
    @Column(name = "txutilemail")
    private String email;
    @Column(name = "txutilmobile")
    private String mobile;
    @Column(name = "loutilenabled")
    private boolean enabled;
    @Column(name = "loutillocked")
    private boolean locked;
    @Embedded
    private Signature signature;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "txutiluser", referencedColumnName = "txutiluser")
    private List<UtilisateurRoleEntity> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
    }
}
