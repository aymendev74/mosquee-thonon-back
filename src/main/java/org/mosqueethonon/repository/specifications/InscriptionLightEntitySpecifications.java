package org.mosqueethonon.repository.specifications;

import org.mosqueethonon.entity.InscriptionEntity;
import org.mosqueethonon.entity.InscriptionLightEntity;
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

public class InscriptionLightEntitySpecifications {


    public static Specification<InscriptionLightEntity> withCriteria(InscriptionCriteria criteria) {
        return (Root<InscriptionLightEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
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
                predicates.add(builder.or(builder.like(builder.lower(root.get("mobile")), "%" + criteria.getTelephone().toLowerCase() + "%")));
            }

            if (criteria.getStatut() != null) {
                statutPredicate = builder.equal(root.get("statut"), criteria.getStatut());
            }

            if(criteria.getDateInscription() != null) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.FRANCE);
                LocalDate dateInscription = LocalDate.parse(criteria.getDateInscription(), df);
                predicates.add(builder.greaterThanOrEqualTo(root.get("dateInscription"), dateInscription));
            }

            if(criteria.getNiveau() != null) {
                predicates.add(builder.equal(root.get("niveau"), criteria.getNiveau()));
            }

            if (criteria.getNbDerniersJours() != null) {
                LocalDate fromDate = LocalDate.now().minusDays(criteria.getNbDerniersJours());
                predicates.add(builder.greaterThanOrEqualTo(root.get("dateInscription"), fromDate));
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
