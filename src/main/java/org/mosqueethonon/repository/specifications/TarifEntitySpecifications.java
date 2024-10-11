package org.mosqueethonon.repository.specifications;

import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.service.criteria.TarifCriteria;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class TarifEntitySpecifications {


    public static Specification<TarifEntity> withCriteria(TarifCriteria criteria) {
        return (Root<TarifEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getApplication() != null) {
                Join<TarifEntity, PeriodeEntity> joinPeriode = root.join("periode");
                predicates.add(builder.equal(joinPeriode.get("application"), criteria.getApplication()));
            }

            if (criteria.getType() != null) {
                predicates.add(builder.equal(root.get("type"), criteria.getType()));
            }

            if (criteria.getAdherent() != null) {
                predicates.add(builder.equal(root.get("adherent"), criteria.getAdherent()));
            }

            if (criteria.getNbEnfant() != null) {
                predicates.add(builder.equal(root.get("nbEnfant"), criteria.getNbEnfant()));
            }

            if (criteria.getAtDate() != null) {
                Join<TarifEntity, PeriodeEntity> joinPeriode = root.join("periode");
                predicates.add(builder.lessThanOrEqualTo(joinPeriode.get("dateDebut"), criteria.getAtDate()));
                predicates.add(builder.greaterThanOrEqualTo(joinPeriode.get("dateFin"), criteria.getAtDate()));
            }

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

}
