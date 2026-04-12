package org.mosqueethonon.service.impl.document;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.entity.bulletin.BulletinMatiereEntity;
import org.mosqueethonon.entity.classe.ClasseEntity;
import org.mosqueethonon.entity.document.DocumentMetadataEntity;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.ClasseRepository;
import org.mosqueethonon.repository.EleveRepository;
import org.mosqueethonon.service.document.DocumentGenerator;
import org.mosqueethonon.service.referentiel.TraductionService;
import org.mosqueethonon.utils.HashUtils;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class BulletinDocumentGenerator implements DocumentGenerator<BulletinEntity> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] NOMS_MOIS = {
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    };

    private final EleveRepository eleveRepository;
    private final ClasseRepository classeRepository;
    private final TraductionService traductionService;

    @Override
    public String getCode() {
        return "BULLETIN-001";
    }

    @Override
    public String getPath() {
        return "BULLETIN";
    }

    @Override
    public String getTemplateName() {
        return "documents/bulletin-001";
    }

    @Override
    public String generateFileName(BulletinEntity entity) {
        return "bulletin-" + entity.getId() + ".pdf";
    }

    @Override
    public String getAnnee(BulletinEntity entity) {
        if (entity.getAnnee() == null) {
            return null;
        }
        return entity.getAnnee().toString();
    }

    @Override
    public Long getIdUtilisateur(BulletinEntity entity) {
        return null;
    }

    @Override
    public Map<String, Object> buildTemplateVariables(BulletinEntity entity) {
        Map<String, Object> variables = new HashMap<>();

        EleveEntity eleve = this.eleveRepository.findById(entity.getIdEleve())
                .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé pour le bulletin, idEleve = " + entity.getIdEleve()));

        variables.put("nomPrenomEleve", eleve.getNom() + " " + eleve.getPrenom());
        variables.put("moisAnnee", buildMoisAnnee(entity));
        variables.put("nomsEnseignants", buildNomsEnseignants(eleve));
        variables.put("lignesMatieres", buildLignesMatieres(entity));
        variables.put("nbAbsences", entity.getNbAbsences() != null ? entity.getNbAbsences() : 0);
        variables.put("dateBulletin", entity.getDateBulletin() != null ? entity.getDateBulletin().format(DATE_FORMATTER) : "");
        variables.put("appreciation", entity.getAppreciation() != null ? entity.getAppreciation() : "");

        return variables;
    }

    @Override
    public String computeHash(BulletinEntity entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getId());
        sb.append("|").append(entity.getIdEleve());
        sb.append("|").append(entity.getMois());
        sb.append("|").append(entity.getAnnee());
        sb.append("|").append(entity.getNbAbsences());
        sb.append("|").append(entity.getDateBulletin());
        sb.append("|").append(entity.getAppreciation());

        if (entity.getBulletinMatieres() != null) {
            entity.getBulletinMatieres().stream()
                    .sorted(Comparator.comparing(bm -> bm.getMatiere() != null ? bm.getMatiere().getCode().name() : ""))
                    .forEach(bm -> {
                        sb.append("|").append(bm.getMatiere() != null ? bm.getMatiere().getCode() : "");
                        sb.append("|").append(bm.getNote());
                        sb.append("|").append(bm.getRemarque());
                    });
        }

        return HashUtils.sha256(sb.toString());
    }

    @Override
    public List<DocumentMetadataEntity> buildMetadata(BulletinEntity entity) {
        List<DocumentMetadataEntity> metadata = new ArrayList<>();
        metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.ID_BULLETIN, String.valueOf(entity.getId())));
        EleveEntity eleve = this.eleveRepository.findById(entity.getIdEleve()).orElse(null);
        if (eleve != null) {
            metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.NOM, eleve.getNom()));
            metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.PRENOM, eleve.getPrenom()));
        }
        return metadata;
    }

    private String buildMoisAnnee(BulletinEntity entity) {
        if (entity.getMois() == null || entity.getAnnee() == null) {
            return "";
        }
        int moisIndex = entity.getMois() - 1;
        String nomMois = (moisIndex >= 0 && moisIndex < NOMS_MOIS.length) ? NOMS_MOIS[moisIndex] : "";
        return nomMois + " " + entity.getAnnee();
    }

    private String buildNomsEnseignants(EleveEntity eleve) {
        if (eleve.getClasseId() == null) {
            return "";
        }
        ClasseEntity classe = this.classeRepository.findById(eleve.getClasseId()).orElse(null);
        if (classe == null || classe.getLiensClasseEnseignants() == null) {
            return "";
        }
        List<String> noms = classe.getLiensClasseEnseignants().stream()
                .filter(lien -> lien.getEnseignant() != null)
                .map(lien -> lien.getEnseignant().getNom() + " " + lien.getEnseignant().getPrenom())
                .toList();
        return String.join(", ", noms);
    }

    private List<Map<String, String>> buildLignesMatieres(BulletinEntity entity) {
        List<Map<String, String>> lignes = new ArrayList<>();
        if (entity.getBulletinMatieres() == null) {
            return lignes;
        }
        for (BulletinMatiereEntity bm : entity.getBulletinMatieres()) {
            if (bm.getMatiere() == null) {
                continue;
            }
            Map<String, String> ligne = new HashMap<>();
            String libelleMatiere = resolveLibelleMatiere(bm.getMatiere().getCode().name());
            ligne.put("matiere", libelleMatiere);
            ligne.put("note", bm.getNote() != null ? bm.getNote().name() : "");
            ligne.put("remarque", bm.getRemarque() != null ? bm.getRemarque() : "");
            lignes.add(ligne);
        }
        return lignes;
    }

    private String resolveLibelleMatiere(String codeMatiere) {
        try {
            return this.traductionService.findTraductionByCleAndValeur("cdmaticode", codeMatiere).getFr();
        } catch (ResourceNotFoundException e) {
            return codeMatiere;
        }
    }

}
