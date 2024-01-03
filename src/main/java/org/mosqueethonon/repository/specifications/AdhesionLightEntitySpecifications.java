package org.mosqueethonon.repository.specifications;

import org.mosqueethonon.entity.AdhesionLightEntity;
import org.mosqueethonon.entity.InscriptionLightEntity;
import org.mosqueethonon.service.criteria.AdhesionCriteria;
import org.mosqueethonon.service.criteria.InscriptionCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdhesionLightEntitySpecifications {

    public static Specification<AdhesionLightEntity> withCriteria(AdhesionCriteria criteria) {
        return (Root<AdhesionLightEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Predicate statutPredicate = null;

            if (StringUtils.hasText(criteria.getNom())) {
                predicates.add(builder.or(builder.like(builder.lower(root.get("nom")), "%" + criteria.getNom().toLowerCase() + "%")));
            }

            if (StringUtils.hasText(criteria.getPrenom())) {
                predicates.add(builder.or(builder.like(builder.lower(root.get("prenom")), "%" + criteria.getPrenom().toLowerCase() + "%")));
            }

            if (criteria.getMontant() != null) {
                predicates.add(builder.or(builder.greaterThanOrEqualTo(root.get("montant"), criteria.getMontant())));
            }

            if (criteria.getStatut() != null) {
                statutPredicate = builder.equal(root.get("statut"), criteria.getStatut());
            }

            if(predicates.isEmpty()) {
                predicates.add(builder.or(builder.isTrue(builder.literal(true))));
            }

            final Predicate finalPredicateOR = builder.or(predicates.toArray(new Predicate[predicates.size()]));

            // Always order by dateCreation desc
            query.orderBy(builder.desc(root.get("dateInscription")));

            return statutPredicate!=null ? builder.and(finalPredicateOR, statutPredicate) : finalPredicateOR;
        };
    }

}
