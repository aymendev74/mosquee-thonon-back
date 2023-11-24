package org.mosqueethonon.repository.specifications;

import org.mosqueethonon.entity.TarifEntity;
import org.mosqueethonon.service.criteria.TarifCriteria;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class TarifEntitySpecifications {


    public static Specification<TarifEntity> withCriteria(TarifCriteria criteria) {
        return (Root<TarifEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getApplication() != null) {
                predicates.add(builder.equal(root.get("application"), criteria.getApplication()));
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

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

}
