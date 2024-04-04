package org.mosqueethonon.repository.specifications;

import org.mosqueethonon.entity.InscriptionLightEntity;
import org.mosqueethonon.v1.criterias.InscriptionCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InscriptionLightEntitySpecifications {


    public static Specification<InscriptionLightEntity> withCriteria(InscriptionCriteria criteria) {
        return (Root<InscriptionLightEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            // Always order by dateInscription desc
            query.orderBy(builder.desc(root.get("dateInscription")));

            List<Predicate> predicatesOR = new ArrayList<>();
            List<Predicate> predicatesAND = new ArrayList<>();
            Predicate finalPredicateOR = null;
            Predicate finalPredicateAND = null;

            if (StringUtils.hasText(criteria.getNom())) {
                predicatesOR.add(builder.like(builder.lower(root.get("nom")), "%" + criteria.getNom().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(criteria.getPrenom())) {
                predicatesOR.add(builder.like(builder.lower(root.get("prenom")), "%" + criteria.getPrenom().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(criteria.getTelephone())) {
                predicatesOR.add(builder.like(builder.lower(root.get("telephone")), "%" + criteria.getTelephone().toLowerCase() + "%"));
                predicatesOR.add(builder.like(builder.lower(root.get("mobile")), "%" + criteria.getTelephone().toLowerCase() + "%"));
            }

            if(criteria.getDateInscription() != null) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.FRANCE);
                predicatesOR.add(builder.greaterThanOrEqualTo(root.get("dateInscription"), criteria.getDateInscription()));
            }

            if(!CollectionUtils.isEmpty(criteria.getNiveaux())) {
                predicatesOR.add(builder.isTrue(root.get("niveau").in(criteria.getNiveaux())));
            }

            if(!CollectionUtils.isEmpty(criteria.getNiveauxInternes())) {
                predicatesOR.add(builder.isTrue(root.get("niveauInterne").in(criteria.getNiveauxInternes())));
            }

            if(StringUtils.hasText(criteria.getNoInscription())) {
                predicatesOR.add(builder.like(root.get("noInscription"), "%" + criteria.getNoInscription() + "%"));
            }

            if (criteria.getNbDerniersJours() != null) {
                LocalDate fromDate = LocalDate.now().minusDays(criteria.getNbDerniersJours());
                predicatesOR.add(builder.greaterThanOrEqualTo(root.get("dateInscription"), fromDate));
            }

            if(criteria.getIdPeriode() != null) {
                predicatesAND.add(builder.equal(root.get("idPeriode"), criteria.getIdPeriode()));
            }

            if (criteria.getStatut() != null) {
                predicatesAND.add(builder.equal(root.get("statut"), criteria.getStatut()));
            }

            if(!predicatesOR.isEmpty()) {
                finalPredicateOR = builder.or(predicatesOR.toArray(new Predicate[predicatesOR.size()]));
            }

            if(!predicatesAND.isEmpty()) {
                finalPredicateAND = builder.and(predicatesAND.toArray(new Predicate[predicatesAND.size()]));
            }

            if(finalPredicateOR == null && finalPredicateAND == null) {
                return builder.or(builder.isTrue(builder.literal(true)));
            }

            if(finalPredicateOR != null && finalPredicateAND != null) {
                return builder.and(finalPredicateOR, finalPredicateAND);
            } else if (finalPredicateOR != null) {
                return finalPredicateOR;
            } else {
                return finalPredicateAND;
            }
        };
    }


}
