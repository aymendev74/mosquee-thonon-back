package org.mosqueethonon.v1.dto.bulletin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.enums.MatiereEnum;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Data
public class BulletinDto {

    private Long id;
    private Long idEleve;
    private String appreciation;
    private Integer nbAbsences;
    private Integer mois;
    private Integer annee;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateBulletin;
    private List<BulletinMatiereDto> bulletinMatieres;
    private Long idDocument;
    private Boolean complet;

    /**
     * Calcule si ce bulletin remplit toutes les conditions pour être considéré comme complet :
     * appréciation renseignée, absences renseignées, mois/année/date renseignés, et une note
     * pour chacune des matières passées en paramètre.
     */
    public boolean calculerCompletude(Collection<MatiereEnum> codesMatieresRequises) {
        if (this.appreciation == null || this.appreciation.trim().isEmpty()) return false;
        if (this.nbAbsences == null) return false;
        if (this.mois == null || this.annee == null) return false;
        if (this.dateBulletin == null) return false;
        if (CollectionUtils.isEmpty(codesMatieresRequises)) return false;
        return codesMatieresRequises.stream().allMatch(code ->
                this.bulletinMatieres != null && this.bulletinMatieres.stream()
                        .anyMatch(bm -> bm.getCode().equals(code) && bm.getNote() != null)
        );
    }

}
