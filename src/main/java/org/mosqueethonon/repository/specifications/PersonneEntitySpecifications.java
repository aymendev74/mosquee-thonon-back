package org.mosqueethonon.repository.specifications;

import org.mosqueethonon.entity.PersonneEntity;
import org.mosqueethonon.service.criteria.PersonneCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class PersonneEntitySpecifications {

    public static Specification<PersonneEntity> withCriteria(PersonneCriteria criteria) {
        return (Root<PersonneEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Predicate statutPredicate = null;

            if (StringUtils.hasText(criteria.getNom())) {
                predicates.add(builder.or(builder.like(builder.lower(root.get("nom")), "%" + criteria.getNom().toLowerCase() + "%")));
            }

            if (StringUtils.hasText(criteria.getPrenom())) {
                predicates.add(builder.or(builder.like(builder.lower(root.get("prenom")), "%" + criteria.getPrenom().toLowerCase() + "%")));
            }

            if (StringUtils.hasText(criteria.getTelephone())) {
                predicates.add(builder.or(builder.like(builder.lower(root.get("telephone")), "%" + criteria.getTelephone().toLowerCase() + "%")));
            }

            if (criteria.getStatut() != null) {
                statutPredicate = builder.equal(root.get("statut"), criteria.getStatut());
            }

            if(predicates.isEmpty()) {
                predicates.add(builder.or(builder.isTrue(builder.literal(true))));
            }

            final Predicate finalPredicateOR = builder.or(predicates.toArray(new Predicate[predicates.size()]));

            return statutPredicate!=null ? builder.and(finalPredicateOR, statutPredicate) : finalPredicateOR;
        };
    }

}
