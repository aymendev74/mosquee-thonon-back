package org.mosqueethonon.repository.specifications;

import jakarta.persistence.criteria.JoinType;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.v1.criterias.UserCriteria;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<UtilisateurEntity> withCriteria(UserCriteria userCriteria) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();
            
            if (userCriteria.getNom() != null && !userCriteria.getNom().isBlank()) {
                predicates = criteriaBuilder.and(
                    predicates,
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nom")), 
                        "%" + userCriteria.getNom().toLowerCase() + "%"
                    )
                );
            }
            
            if (userCriteria.getPrenom() != null && !userCriteria.getPrenom().isBlank()) {
                predicates = criteriaBuilder.and(
                    predicates,
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("prenom")),
                        "%" + userCriteria.getPrenom().toLowerCase() + "%"
                    )
                );
            }
            
            if (userCriteria.getEmail() != null && !userCriteria.getEmail().isBlank()) {
                predicates = criteriaBuilder.and(
                    predicates,
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + userCriteria.getEmail().toLowerCase() + "%"
                    )
                );
            }
            
            if (userCriteria.getRole() != null && !userCriteria.getRole().isBlank()) {
                var rolesJoin = root.join("roles", JoinType.INNER);
                predicates = criteriaBuilder.and(
                    predicates,
                    criteriaBuilder.equal(
                        criteriaBuilder.lower(rolesJoin.get("role")),
                        userCriteria.getRole().toLowerCase()
                    )
                );
            }
            
            return predicates;
        };
    }
}
